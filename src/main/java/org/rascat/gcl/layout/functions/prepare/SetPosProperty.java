package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.AbstractGraphCollectionLayout;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class SetPosProperty implements MapFunction<EPGMVertex, EPGMVertex> {
  @Override
  public EPGMVertex map(EPGMVertex value) throws Exception {
    Number x = value.getPropertyValue(KEY_X_COORD).isDouble()
      ? value.getPropertyValue(KEY_X_COORD).getDouble()
      : value.getPropertyValue(KEY_X_COORD).getInt();
    Number y = value.getPropertyValue(KEY_Y_COORD).isDouble()
      ? value.getPropertyValue(KEY_Y_COORD).getDouble()
      : value.getPropertyValue(KEY_Y_COORD).getInt();

    String posString = String.format("%d,%d", x.intValue(), y.intValue());
    value.setProperty("pos", posString);
    return value;
  }
}
