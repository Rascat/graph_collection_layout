package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.layouts.transactional.tuples.GraphTransaction;
import org.rascat.gcl.layout.functions.prepare.TransactionalRandomPlacement;

public class TransactionalGraphCollectionLayout extends AbstractGraphCollectionLayout {

  public TransactionalGraphCollectionLayout(int width, int height) {
    super(width, height);
  }

  @Override
  public GraphCollection execute(GraphCollection collection) throws Exception {
    DataSet<GraphTransaction> transactions = collection.getGraphTransactions();

    double k = Math.sqrt(area() / collection.getVertices().count());

    transactions = transactions.map(new TransactionalRandomPlacement(width, height, k));

    return collection.getFactory().fromTransactions(transactions);
  }
}
