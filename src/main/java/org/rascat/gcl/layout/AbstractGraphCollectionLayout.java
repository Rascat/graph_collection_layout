package org.rascat.gcl.layout;

import org.gradoop.flink.model.impl.epgm.GraphCollection;

public abstract class AbstractGraphCollectionLayout {
  public static String KEY_X_COORD = "X";
  public static String KEY_Y_COORD = "Y";

  public abstract GraphCollection execute(GraphCollection collection);
}
