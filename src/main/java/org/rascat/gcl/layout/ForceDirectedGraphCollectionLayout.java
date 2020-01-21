package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.operators.IterativeDataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.functions.*;
import org.rascat.gcl.functions.cooling.CoolingSchedule;
import org.rascat.gcl.functions.cooling.ExponentialSimulatedAnnealing;
import org.rascat.gcl.functions.forces.StandardAttractingForce;
import org.rascat.gcl.functions.forces.StandardRepulsingForce;
import org.rascat.gcl.functions.forces.WeightedAttractingForce;
import org.rascat.gcl.functions.grid.NeighborType;
import org.rascat.gcl.functions.grid.SquareIdMapper;
import org.rascat.gcl.functions.grid.SquareIdSelector;
import org.rascat.gcl.model.Force;

import static org.rascat.gcl.functions.VertexType.TAIL;
import static org.rascat.gcl.functions.VertexType.HEAD;

public class ForceDirectedGraphCollectionLayout extends AbstractGraphCollectionLayout {

    private int width;
    private int height;
    private double k;
    private int iterations = 1;
    private boolean isIntermediaryLayout = false;

    public ForceDirectedGraphCollectionLayout(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public void setIterations(int i) {
        this.iterations = i;
    }

    public void setIsIntermediaryLayout(boolean isIntermediaryLayout) {
        this.isIntermediaryLayout = isIntermediaryLayout;
    }

    public GraphCollection execute(GraphCollection collection) {
        return execute(collection, 20);
    }

    public GraphCollection execute(GraphCollection collection, int vertexCount) {
        this.k = Math.sqrt((double) area() / vertexCount);
        DataSet<EPGMEdge> edges = collection.getEdges();
        DataSet<EPGMVertex> vertices = collection.getVertices();
        DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

        if(!isIntermediaryLayout) {
            vertices = vertices.map(new RandomPlacement(width, height));
        } else {
            // make sure int values get cast to double
            vertices = vertices.map( vertex -> {
                vertex.setProperty(KEY_X_COORD, (double) vertex.getPropertyValue(KEY_X_COORD).getInt());
                vertex.setProperty(KEY_Y_COORD, (double) vertex.getPropertyValue(KEY_Y_COORD).getInt());
                return vertex;
            });
        }

        IterativeDataSet<EPGMVertex> loop = vertices.iterate(iterations);

        DataSet<Force> repulsiveForces = gridRepulsiveForces(loop);

        DataSet<Force> attractiveForces = weightedAttractiveForces(loop, edges);

        DataSet<Force> forces = repulsiveForces.union(attractiveForces)
                .groupBy(Force.ID_POSITION)
                .reduce((firstForce, secondForce) -> {
                    firstForce.setVector(firstForce.getVector().add(secondForce.getVector()));
                    return firstForce;
                });

        CoolingSchedule schedule = new ExponentialSimulatedAnnealing(this.width, this.height, this.k, this.iterations);
        DataSet<EPGMVertex> pVertices = loop.closeWith(
                loop.join(forces)
                        .where("id").equalTo("f0")
                        .with(new ApplyForces(width, height, schedule)));

        return collection.getFactory().fromDataSets(graphHeads, pVertices, edges);
    }

    private int area() {
        return this.height * this.width;
    }

    private DataSet<Force> naiveRepulsiveForces(DataSet<EPGMVertex> vertices) {
        return vertices.cross(vertices).with(new ComputeRepulsiveForces(k, new StandardRepulsingForce()));
    }

    private DataSet<Force> gridRepulsiveForces(DataSet<EPGMVertex> vertices) {
        vertices = vertices.map(new SquareIdMapper((int) k * 2));

        KeySelector<EPGMVertex, Integer> selfSelector = new SquareIdSelector(NeighborType.SELF);
        KeySelector<EPGMVertex, Integer> upSelector = new SquareIdSelector(NeighborType.UP);
        KeySelector<EPGMVertex, Integer> upRightSelector = new SquareIdSelector(NeighborType.UPRIGHT);
        KeySelector<EPGMVertex, Integer> upLeftSelector = new SquareIdSelector(NeighborType.UPLEFT);
        KeySelector<EPGMVertex, Integer> leftSelector = new SquareIdSelector(NeighborType.LEFT);

        ComputeRepulsiveForces repulsionFunction = new ComputeRepulsiveForces(k, new StandardRepulsingForce());

        DataSet<Force> directNeighbors = vertices.join(vertices)
          .where(selfSelector).equalTo(selfSelector)
          .with(repulsionFunction);

        DataSet<Force> upNeighbors = vertices.join(vertices)
          .where(upSelector).equalTo(selfSelector)
          .with(repulsionFunction);

        DataSet<Force> upRightNeighbors = vertices.join(vertices)
          .where(upRightSelector).equalTo(selfSelector)
          .with(repulsionFunction);

        DataSet<Force> upLeftNeighbors = vertices.join(vertices)
          .where(upLeftSelector).equalTo(selfSelector)
          .with(repulsionFunction);

        DataSet<Force> leftNeighbors = vertices.join(vertices)
          .where(leftSelector).equalTo(selfSelector)
          .with(repulsionFunction);

        return directNeighbors
          .union(upNeighbors)
          .union(upRightNeighbors)
          .union(upLeftNeighbors)
          .union(leftNeighbors);
    }

    private DataSet<Force> attractiveForces(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
        // first we need to add the position of the source/target vertex to the respective edge
        DataSet<EPGMEdge> positionedEdges = edges
                .join(vertices).where("sourceId").equalTo("id").with(new TransferPosition(TAIL))
                .join(vertices).where("targetId").equalTo("id").with(new TransferPosition(HEAD));

        return positionedEdges.map(new ComputeAttractingForces(k, new StandardAttractingForce()));
    }

    private DataSet<Force> weightedAttractiveForces(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
        DataSet<EPGMEdge> positionedEdges = edges
                .join(vertices).where("sourceId").equalTo("id").with(new TransferPosition(TAIL))
                .join(vertices).where("targetId").equalTo("id").with(new TransferPosition(HEAD));

        positionedEdges = positionedEdges
                .join(vertices).where("sourceId").equalTo("id").with(new TransferGraphIds(TAIL))
                .join(vertices).where("targetId").equalTo("id").with(new TransferGraphIds(HEAD));

        return positionedEdges.map(new ComputeWeightedAttractingForces(k, new WeightedAttractingForce()));
    }
}
