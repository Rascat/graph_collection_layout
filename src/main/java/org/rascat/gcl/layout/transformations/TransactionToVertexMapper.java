package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;
import org.gradoop.flink.model.impl.layouts.transactional.tuples.GraphTransaction;

import java.io.Serializable;

public class TransactionToVertexMapper implements MapFunction<GraphTransaction, EPGMVertex>, Serializable {

  private EPGMVertexFactory factory;

  public TransactionToVertexMapper() {
    this.factory = new EPGMVertexFactory();
  }

  @Override
  public EPGMVertex map(GraphTransaction transaction) {
    return factory.initVertex(transaction.getGraphHead().getId());
  }
}
