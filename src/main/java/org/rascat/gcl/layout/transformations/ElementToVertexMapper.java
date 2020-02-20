package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.api.entities.Element;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;

import java.io.Serializable;

public class ElementToVertexMapper<E extends Element> implements MapFunction<E, EPGMVertex>, Serializable {

  private EPGMVertexFactory factory;

  public ElementToVertexMapper() {
    this.factory = new EPGMVertexFactory();
  }

  @Override
  public EPGMVertex map(E element) {
    return factory.initVertex(element.getId());
  }
}
