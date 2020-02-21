package org.rascat.gcl.layout.functions.select;

import org.apache.flink.api.java.functions.KeySelector;
import org.gradoop.common.model.api.entities.GraphElement;
import org.gradoop.common.model.impl.id.GradoopId;

public class SelectFirstGraphId<E extends GraphElement> implements KeySelector<E, GradoopId> {
  @Override
  public GradoopId getKey(E element) {
    return element.getGraphIds().iterator().next();
  }
}
