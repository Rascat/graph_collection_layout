package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.api.entities.GraphHead;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.functions.ComputeRepulsiveForces;
import org.rascat.gcl.functions.RandomPlacement;
import org.rascat.gcl.functions.SumForces;
import org.rascat.gcl.model.Force;

public class ForceDirectedGraphCollectionLayout {

    private int width;
    private int height;
    private double k;
    private int iterations = 1;

    public ForceDirectedGraphCollectionLayout(int width, int height) {
        this.height = height;
        this.width = width;
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

        DataSet<Force> displacements =
          vertices.cross(vertices).with(new ComputeRepulsiveForces(k));

        DataSet<Force> displacementByVertex = displacements.groupBy("f0").reduce(new SumForces());

        displacements.writeAsText("out/displacements");
        displacementByVertex.writeAsText("out/dispByVertex");

        return collection.getFactory().fromDataSets(graphHeads, vertices, edges);
    }

    private int area() {
        return this.height * this.width;
    }
}
