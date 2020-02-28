package org.rascat.gcl.layout.functions.select;

import org.apache.flink.api.java.functions.KeySelector;
import org.gradoop.common.model.api.entities.GraphElement;
import org.gradoop.common.model.impl.id.GradoopId;

import java.util.SortedSet;
import java.util.TreeSet;

public class SelectFirstGraphId<E extends GraphElement> implements KeySelector<E, GradoopId> {
  @Override
  public GradoopId getKey(E element) {
    SortedSet<GradoopId> sortedIdSet = new TreeSet<>(element.getGraphIds());
    return sortedIdSet.first();
  }
}
