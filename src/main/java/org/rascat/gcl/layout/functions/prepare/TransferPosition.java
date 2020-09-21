package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.rascat.gcl.layout.model.Point;
import org.rascat.gcl.layout.model.VertexType;

import java.util.ArrayList;
import java.util.List;

public class TransferPosition implements JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge> {

  private VertexType type;

  public TransferPosition(VertexType type) {
    this.type = type;
  }

  @Override
  public EPGMEdge join(EPGMEdge edge, EPGMVertex vertex) {
    // transfer vertex position
    edge = Point.fromEPGMElement(vertex)
      .addPositionPropertyToElement(edge, type.getKeyX(), type.getKeyY());

    // transfer vertex graph ids
    List<PropertyValue> graphIds = translateToPropertyValueList(vertex.getGraphIds());
    edge.setProperty(type.getKeyGraphIds(), PropertyValue.create(graphIds));

    return edge;
  }

  private static List<PropertyValue> translateToPropertyValueList(GradoopIdSet set) {
    List<PropertyValue> ids = new ArrayList<>();

    for (GradoopId id: set) {
      ids.add(PropertyValue.create(id));
    }

    return ids;
  }

}
