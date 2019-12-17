package org.rascat.gcl.functions;

import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;

public class TransferPosition implements JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge> {

  private Position position;

  public TransferPosition(Position position) {
    this.position = position;
  }

  @Override
  public EPGMEdge join(EPGMEdge edge, EPGMVertex vertex) throws Exception {
    edge.setProperty(position.getKeyX(), vertex.getPropertyValue("X"));
    edge.setProperty(position.getKeyY(), vertex.getPropertyValue("Y"));
    return edge;
  }

  public enum Position {
    SOURCE("source_x", "source_y"),
    TARGET("target_x", "target_y");

    private String keyX;
    private String keyY;

    Position(String keyX, String keyY) {
      this.keyX = keyX;
      this.keyY = keyY;
    }

    public String getKeyX() {
      return keyX;
    }

    public String getKeyY() {
      return keyY;
    }
  }
}
