package org.rascat.gcl.functions.grid;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class SquareIdMapper implements MapFunction<EPGMVertex, EPGMVertex> {

  private int squareSize;

  public static final String KEY_SQUARE_ID = "square_id";
  public static final int HALF_INTEGER_SIZE = 16;

  public SquareIdMapper(int squareSize) {
    this.squareSize = squareSize;
  }

  @Override
  public EPGMVertex map(EPGMVertex value) {
    double posX = value.getPropertyValue(KEY_X_COORD).getDouble();
    double posY = value.getPropertyValue(KEY_Y_COORD).getDouble();

    int squareX = ((int) posX) / squareSize;
    int squareY = ((int) posY) / squareSize;

    int squareId = (squareX << HALF_INTEGER_SIZE) | squareY;

    value.setProperty(KEY_SQUARE_ID, squareId);
    return value;
  }
}
