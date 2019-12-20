package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.operators.IterativeDataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.functions.*;
import org.rascat.gcl.model.Force;

import static org.rascat.gcl.functions.TransferPosition.Position.SOURCE;
import static org.rascat.gcl.functions.TransferPosition.Position.TARGET;

public class ForceDirectedGraphCollectionLayout extends AbstractGraphCollectionLayout {

    private int width;
    private int height;
    private double k;
    private int iterations = 1;

    public ForceDirectedGraphCollectionLayout(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public void setIterations(int i) {
        this.iterations = i;
    }

    public GraphCollection execute(GraphCollection collection) {
        return execute(collection, 20);
    }

    public GraphCollection execute(GraphCollection collection, int vertexCount) {
        this.k = Math.sqrt((double) area() / vertexCount);
        DataSet<EPGMEdge> edges = collection.getEdges();
        DataSet<EPGMVertex> vertices = collection.getVertices();
        DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

        vertices = vertices.map(new RandomPlacement(width, height));

        IterativeDataSet<EPGMVertex> loop = vertices.iterate(iterations);

        DataSet<Force> repulsiveForcesLoop = loop.cross(loop)
                .with(new ComputeRepulsiveForces(k))
                .groupBy("f0")
                .reduce(new SumForces());

        DataSet<EPGMEdge> positionedEdgesLoop = edges.join(loop)
                .where("sourceId").equalTo("id").with(new TransferPosition(SOURCE))
                .join(vertices)
                .where("targetId").equalTo("id").with(new TransferPosition(TARGET));

        DataSet<Force> attractingForcesLoop = positionedEdgesLoop
                .map(new ComputeAttractingForces(k))
                .groupBy("f0")
                .reduce(new SubtractForces());

        DataSet<Force> resultingForcesLoop = repulsiveForcesLoop.union(attractingForcesLoop)
          .groupBy("f0")
          .reduce(new SubtractForces());

        CoolingSchedule schedule = new LinearSimulatedAnnealing((double) width / 10);
        DataSet<EPGMVertex> pVertices = loop.closeWith(
          loop.join(resultingForcesLoop)
            .where("id").equalTo("f0")
            .with(new ApplyForces(width, height, schedule)));

        return collection.getFactory().fromDataSets(graphHeads, pVertices, edges);
    }

    private int area() {
        return this.height * this.width;
    }
}
