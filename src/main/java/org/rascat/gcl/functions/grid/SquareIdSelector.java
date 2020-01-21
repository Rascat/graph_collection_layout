package org.rascat.gcl.functions.grid;

import org.apache.flink.api.java.functions.KeySelector;
import org.gradoop.common.model.impl.pojo.EPGMVertex;

import static org.rascat.gcl.functions.grid.NeighborType.*;
import static org.rascat.gcl.functions.grid.SquareIdMapper.HALF_INTEGER_SIZE;

public class SquareIdSelector implements KeySelector<EPGMVertex, Integer> {

  private NeighborType type;

  public SquareIdSelector(NeighborType type) {
    this.type = type;
  }

  @Override
  public Integer getKey(EPGMVertex value) {
    int squareId = value.getPropertyValue(SquareIdMapper.KEY_SQUARE_ID).getInt();
    System.out.println(String.format("selected %d for %s", squareId, value.getLabel()));
    int squareX = squareId >> HALF_INTEGER_SIZE;
    int squareY = squareId & 0xFFFF;


    if (type == RIGHT || type == UPRIGHT || type == DOWNRIGHT) {
      squareX++;
    }
    if (type == LEFT || type == UPLEFT || type == DOWNLEFT) {
      squareX--;
    }
    if (type == UP || type == UPLEFT || type == UPRIGHT) {
      squareY--;
    }
    if (type == DOWN || type == DOWNLEFT || type == DOWNRIGHT) {
      squareY++;
    }

    return (squareX << HALF_INTEGER_SIZE) | squareY;
  }
}
