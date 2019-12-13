package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.functions.RandomPlacement;

public class ForceDirectedGraphCollectionLayout {

    private int width;
    private int height;
    private double k;
    private int iterations = 10;

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

        vertices = vertices.map(new RandomPlacement(width, height));

        for (int i = 0; i < iterations; i++) {
            vertices.
        }

        return null;
    }

    private int area() {
        return this.height * this.width;
    }
}
