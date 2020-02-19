package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMEdgeFactory;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.layouts.transactional.tuples.GraphTransaction;

import java.io.Serializable;
import java.util.Set;

public class TransactionToEdgeMapper implements FlatMapFunction<Tuple2<GraphTransaction, GraphTransaction>, EPGMEdge>,
    Serializable {

  private EPGMEdgeFactory factory;

  public TransactionToEdgeMapper() {
    this.factory = new EPGMEdgeFactory();
  }

  @Override
  public void flatMap(Tuple2<GraphTransaction, GraphTransaction> value, Collector<EPGMEdge> out) {
    if (value.f0.getGraphHead().getId().equals(value.f1.getGraphHead().getId()))
      return;

    Set<EPGMEdge> edgesT1 = value.f0.getEdges();
    Set<EPGMVertex> verticesT2 = value.f1.getVertices();

    for (EPGMEdge edgeT1 : edgesT1) {
      for (EPGMVertex vertexT2 : verticesT2) {
        if (edgeT1.getTargetId().equals(vertexT2.getId())) {
          System.out.println("[" + value.f0.getGraphHead().getId() + "]-->[" + value.f1.getGraphHead().getId() + "]");
          out.collect(factory.createEdge(value.f0.getGraphHead().getId(), value.f1.getGraphHead().getId()));
        }
      }
    }
  }
}
