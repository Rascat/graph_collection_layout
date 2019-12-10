package org.rascat.gcl.layout;

import org.gradoop.flink.model.impl.epgm.GraphCollection;

public class ForceDirectedGraphCollectionLayout {

    private int width;
    private int height;
    private double k;

    public ForceDirectedGraphCollectionLayout(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public GraphCollection execute(GraphCollection collection) {
        return null;
    }

    public GraphCollection execute(GraphCollection collection, int vertexCount) {
        this.k = Math.sqrt((double) area() / vertexCount);
        return null;
    }

    private int area() {
        return this.height * this.width;
    }
}
