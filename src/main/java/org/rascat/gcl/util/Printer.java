package org.rascat.gcl.util;

import org.gradoop.flink.model.impl.epgm.LogicalGraph;

public class Printer {
    public static void printGraphInfo(LogicalGraph graph, String label) throws Exception {
        System.out.println("[" + label + "] Amount edges: " + graph.getEdges().count());
        System.out.println("[" + label + "] Amount vertices: " + graph.getVertices().count());
        System.out.println("[" + label + "] Graph heads: " + graph.getGraphHead().collect());
    }
}
