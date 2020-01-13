package org.rascat.gcl.functions;

import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class TransferPosition implements JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge> {

  private VertexType type;

  public TransferPosition(VertexType type) {
    this.type = type;
  }

  @Override
  public EPGMEdge join(EPGMEdge edge, EPGMVertex vertex) {
    edge.setProperty(type.getKeyX(), vertex.getPropertyValue(KEY_X_COORD));
    edge.setProperty(type.getKeyY(), vertex.getPropertyValue(KEY_Y_COORD));
    return edge;
  }

}
