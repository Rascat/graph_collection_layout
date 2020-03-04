package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Point;
import org.rascat.gcl.layout.model.VertexType;

public class TransferPosition implements JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge> {

  private VertexType type;

  public TransferPosition(VertexType type) {
    this.type = type;
  }

  @Override
  public EPGMEdge join(EPGMEdge edge, EPGMVertex vertex) {
    return Point.fromEPGMElement(vertex)
      .addPositionPropertyToElement(edge, type.getKeyX(), type.getKeyY());
  }

}
