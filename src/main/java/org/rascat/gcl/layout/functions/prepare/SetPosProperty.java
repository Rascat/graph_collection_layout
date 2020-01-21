package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.AbstractGraphCollectionLayout;

public class SetPosProperty implements MapFunction<EPGMVertex, EPGMVertex> {
  @Override
  public EPGMVertex map(EPGMVertex value) throws Exception {
    double x = value.getPropertyValue(AbstractGraphCollectionLayout.KEY_X_COORD).getDouble();
    double y = value.getPropertyValue(AbstractGraphCollectionLayout.KEY_Y_COORD).getDouble();
    String posString = String.format("%d,%d", (int) x,(int) y);
    value.setProperty("pos", posString);
    return value;
  }
}
