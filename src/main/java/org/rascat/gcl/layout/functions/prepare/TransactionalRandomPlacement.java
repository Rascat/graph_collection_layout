package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.layouts.transactional.tuples.GraphTransaction;
import org.rascat.gcl.layout.AbstractGraphCollectionLayout;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TransactionalRandomPlacement implements MapFunction<GraphTransaction, GraphTransaction> {

  RandomPlacement<EPGMGraphHead> randomPlacement;
  int width;
  int height;
  double k;

  public TransactionalRandomPlacement(int width, int height, double k) {
    this.randomPlacement = new RandomPlacement<>(width,height);
    this.width = width;
    this.height = height;
    this.k = k;
  }

  @Override
  public GraphTransaction map(GraphTransaction transaction) {

    double centerX = ThreadLocalRandom.current().nextDouble(k, width - k);
    double centerY = ThreadLocalRandom.current().nextDouble(k, height - k);

    Set<EPGMVertex> vertices = transaction.getVertices();

    for (EPGMVertex vertex : vertices) {
      double xCoord = ThreadLocalRandom.current().nextDouble(centerX - k, centerX + k);
      double yCoord = ThreadLocalRandom.current().nextDouble(centerY - k, centerY + k);
      vertex.setProperty(AbstractGraphCollectionLayout.KEY_X_COORD, xCoord);
      vertex.setProperty(AbstractGraphCollectionLayout.KEY_Y_COORD, yCoord);
    }
    transaction.setVertices(vertices);
    return transaction;
  }
}
