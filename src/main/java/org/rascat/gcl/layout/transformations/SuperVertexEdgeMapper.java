package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMEdgeFactory;
import org.gradoop.common.model.impl.pojo.EPGMVertex;

public class SuperVertexEdgeMapper implements FlatJoinFunction<Tuple2<EPGMVertex, EPGMEdge>, EPGMVertex, EPGMEdge> {

  private EPGMEdgeFactory factory = new EPGMEdgeFactory();

  @Override
  public void join(Tuple2<EPGMVertex, EPGMEdge> sourceVertexAndEdge, EPGMVertex targetVertex, Collector<EPGMEdge> out) {
    EPGMVertex sourceVertex = sourceVertexAndEdge.f0;
    if (sourceVertex.getGraphIds().containsAny(targetVertex.getGraphIds())) {
      EPGMEdge result = factory.createEdge(
          sourceVertex.getGraphIds().iterator().next(),
          targetVertex.getGraphIds().iterator().next());
      out.collect(result);
    }
  }
}
