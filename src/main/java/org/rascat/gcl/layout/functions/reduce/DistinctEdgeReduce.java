package org.rascat.gcl.layout.functions.reduce;

import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMEdgeFactory;

import java.util.HashSet;
import java.util.Set;

public class DistinctEdgeReduce implements GroupReduceFunction<EPGMEdge, EPGMEdge> {

  private EPGMEdgeFactory factory = new EPGMEdgeFactory();

  @Override
  public void reduce(Iterable<EPGMEdge> edges, Collector<EPGMEdge> out)  {
    Set<GradoopId> uniqueTargetIds = new HashSet<>();
    GradoopId key = null;

    for (EPGMEdge edge : edges) {
      key = edge.getSourceId();
      uniqueTargetIds.add(edge.getTargetId());
    }

    for (GradoopId id : uniqueTargetIds) {
      out.collect(factory.createEdge(key, id));
    }
  }
}
