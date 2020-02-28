package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.epgm.LogicalGraphFactory;
import org.gradoop.flink.model.impl.layouts.gve.GVEGraphLayoutFactory;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.functions.reduce.DistinctEdgeReduce;

import java.io.Serializable;

public class SuperVertexReduce implements Serializable {

  private LogicalGraphFactory graphFactory;

  public SuperVertexReduce(GradoopFlinkConfig cfg) {
    this.graphFactory = new LogicalGraphFactory(cfg);
    this.graphFactory.setLayoutFactory(new GVEGraphLayoutFactory());
  }

  public LogicalGraph transform(GraphCollection collection) {
    // create a vertex for each graph head. Both have the same GradoopId
    DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();
    DataSet<EPGMVertex> vertices = graphHeads.map(new VertexWithSameGraphIdMapper<>());

    // create an edge for each pair of distinct vertices, where two vertices in the graphs they represent are connected
    // by an edge
    DataSet<EPGMEdge> edges = collection.getVertices().join(collection.getEdges())
        .where("id").equalTo("sourceId")
        .join(collection.getVertices())
        .where("f1.targetId").equalTo("id")
        .with(new SuperVertexEdgeMapper());

    // filter out duplicated edges
    edges = edges.groupBy("sourceId").reduceGroup(new DistinctEdgeReduce());

    return graphFactory.fromDataSets(vertices, edges);
  }
}
