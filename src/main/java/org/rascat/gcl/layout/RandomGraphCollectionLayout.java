package org.rascat.gcl.layout;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.functions.RandomPlacement;

public class RandomGraphCollectionLayout {
    private int width;
    private int height;

    public RandomGraphCollectionLayout(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public GraphCollection execute(GraphCollection collection) {
        DataSet<EPGMVertex> vertices = collection.getVertices();
        DataSet<EPGMEdge> edges = collection.getEdges();
        DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

        DataSet<EPGMVertex> mappedVertices = vertices.map(new RandomPlacement(this.width, this.height));

//        DataSet<EPGMEdge> mappedEdges = this.prepareEdges(mappedVertices, edges);

        return collection.getFactory().fromDataSets(graphHeads, mappedVertices, edges);
    }

//    /**
//     * Prepare the given edges for drawing. Assign them start- and end-coordinates from their
//     * vertices.
//     *
//     * @param vertices The vertices to take the edge-coordinates from
//     * @param edges    The raw edges
//     * @return The prepared edges
//     */
//    private DataSet<EPGMEdge> prepareEdges(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
//        edges = edges.join(vertices).where("sourceId").equalTo("id")
//          .with(new JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge>() {
//              public EPGMEdge join(EPGMEdge first, EPGMVertex second) throws Exception {
//                  first.setProperty("source_x", second.getPropertyValue("X"));
//                  first.setProperty("source_y", second.getPropertyValue("Y"));
//                  return first;
//              }
//          }).join(vertices).where("targetId").equalTo("id")
//          .with(new JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge>() {
//              public EPGMEdge join(EPGMEdge first, EPGMVertex second) throws Exception {
//                  first.setProperty("target_x", second.getPropertyValue("X"));
//                  first.setProperty("target_y", second.getPropertyValue("Y"));
//                  return first;
//              }
//          });
//        return edges;
//    }
}
