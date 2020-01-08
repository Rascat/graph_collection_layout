package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
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
import org.rascat.gcl.model.Force;

import static org.rascat.gcl.functions.TransferPosition.Position.SOURCE;
import static org.rascat.gcl.functions.TransferPosition.Position.TARGET;

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

        DataSet<Force> repulsiveForces = repulsiveForces(loop);

        DataSet<Force> attractiveForces = attractiveForces(loop, edges);

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

    private DataSet<Force> repulsiveForces(DataSet<EPGMVertex> vertices) {
        return vertices.cross(vertices).with(new ComputeRepulsiveForces(k, new StandardRepulsingForce()));
    }

    private DataSet<Force> attractiveForces(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
        // first we need to add the position of the source/target vertex to the respective edge
        DataSet<EPGMEdge> positionedEdges = edges.join(vertices)
                .where("sourceId").equalTo("id").with(new TransferPosition(SOURCE))
                .join(vertices)
                .where("targetId").equalTo("id").with(new TransferPosition(TARGET));

        return positionedEdges.map(new ComputeAttractingForces(k, new StandardAttractingForce()));
    }
}
