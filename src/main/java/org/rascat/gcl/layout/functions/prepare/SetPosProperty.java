package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.api.operators.UnaryBaseGraphCollectionToBaseGraphCollectionOperator;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.layout.AbstractGraphCollectionLayout;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class SetPosProperty implements MapFunction<EPGMVertex, EPGMVertex>,
  UnaryBaseGraphCollectionToBaseGraphCollectionOperator<GraphCollection> {

  public static final String keyPos = "pos";

  @Override
  public EPGMVertex map(EPGMVertex value) {
    Number x = value.getPropertyValue(KEY_X_COORD).isDouble()
      ? value.getPropertyValue(KEY_X_COORD).getDouble()
      : value.getPropertyValue(KEY_X_COORD).getInt();
    Number y = value.getPropertyValue(KEY_Y_COORD).isDouble()
      ? value.getPropertyValue(KEY_Y_COORD).getDouble()
      : value.getPropertyValue(KEY_Y_COORD).getInt();

    String posString = String.format("%d,%d", x.intValue(), y.intValue());
    value.setProperty(keyPos, posString);
    return value;
  }

  @Override
  public GraphCollection execute(GraphCollection collection) {
    DataSet<EPGMVertex> vertices = collection.getVertices().map(this);
    return collection.getFactory().fromDataSets(collection.getGraphHeads(), vertices, collection.getEdges());
  }
}
