package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.flink.model.impl.layouts.transactional.tuples.GraphTransaction;

public class TransactionalRandomPlacement implements MapFunction<GraphTransaction, GraphTransaction> {

  RandomPlacement<EPGMGraphHead> randomPlacement;

  public TransactionalRandomPlacement(int width, int height) {
    this.randomPlacement = new RandomPlacement<>(width,height);
  }

  @Override
  public GraphTransaction map(GraphTransaction transaction) throws Exception {
    EPGMGraphHead head = transaction.getGraphHead();
    head = randomPlacement.map(head);
    transaction.setGraphHead(head);
    return transaction;
  }
}
