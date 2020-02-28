package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMEdgeFactory;
import org.gradoop.common.model.impl.pojo.EPGMVertex;

import java.util.TreeSet;

public class SuperVertexEdgeMapper implements FlatJoinFunction<Tuple2<EPGMVertex, EPGMEdge>, EPGMVertex, EPGMEdge> {

  private EPGMEdgeFactory factory = new EPGMEdgeFactory();

  /**
   * If two vertices from the originating graph that belong to different logical graphs are connected by an edge,
   * then, in the resulting super-vertex graph, the two vertices that represent those logical graphs should also be
   * connected by an edge.
   *
   * @param sourceAndEdge Tuple2 containing the source vertex and the edge.
   * @param target The target vertex.
   * @param out Collector.
   */
  @Override
  public void join(Tuple2<EPGMVertex, EPGMEdge> sourceAndEdge, EPGMVertex target, Collector<EPGMEdge> out) {
    EPGMVertex source = sourceAndEdge.f0;
    GradoopId sourceVertexGraphId = new TreeSet<>(source.getGraphIds()).first();
    GradoopId targetVertexGraphId = new TreeSet<>(target.getGraphIds()).first();

    if (!sourceVertexGraphId.equals(targetVertexGraphId)) {
      EPGMEdge result = factory.createEdge(sourceVertexGraphId, targetVertexGraphId);
      out.collect(result);
    }
  }
}
