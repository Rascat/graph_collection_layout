package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMElement;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class TransferCenterPosition<E extends EPGMElement> implements JoinFunction<E, E, E> {

  public static final String KEY_CENTER_X_COORD = "CENTER_X";
  public static final String KEY_CENTER_Y_COORD = "CENTER_Y";

  @Override
  public E join(E first, E second) {
    double centerX = first.getPropertyValue(KEY_X_COORD).getInt();
    double centerY = first.getPropertyValue(KEY_Y_COORD).getInt();

    second.setProperty(KEY_CENTER_X_COORD, centerX);
    second.setProperty(KEY_CENTER_Y_COORD, centerY);

    return second;
  }
}
