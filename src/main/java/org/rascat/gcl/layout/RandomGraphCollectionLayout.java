package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.layout.functions.prepare.RandomPlacement;

public class RandomGraphCollectionLayout extends AbstractGraphCollectionLayout {

    public RandomGraphCollectionLayout(int width, int height) {
        super(width, height);
    }

    public GraphCollection execute(GraphCollection collection) {
        DataSet<EPGMVertex> vertices = collection.getVertices();
        DataSet<EPGMEdge> edges = collection.getEdges();
        DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

        DataSet<EPGMVertex> mappedVertices = vertices.map(new RandomPlacement<>(this.width, this.height));

        return collection.getFactory().fromDataSets(graphHeads, mappedVertices, edges);
    }
}
