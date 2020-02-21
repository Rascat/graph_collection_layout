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

import java.io.Serializable;

public class SuperVertexReduce implements Serializable {

  private LogicalGraphFactory graphFactory;

  public SuperVertexReduce(GradoopFlinkConfig cfg) {
    this.graphFactory = new LogicalGraphFactory(cfg);
    this.graphFactory.setLayoutFactory(new GVEGraphLayoutFactory());
  }

  public LogicalGraph transform(GraphCollection collection) {
    DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();
    DataSet<EPGMVertex> vertices = graphHeads.map(new ElementToVertexMapper<>());

    DataSet<EPGMEdge> edges = collection.getVertices()
        .join(collection.getEdges()).where("id").equalTo("sourceId")
        .join(collection.getVertices()).where("f1.targetId").equalTo("id")
        .with(new SuperVertexEdgeMapper());

    return graphFactory.fromDataSets(vertices, edges);
  }
}
