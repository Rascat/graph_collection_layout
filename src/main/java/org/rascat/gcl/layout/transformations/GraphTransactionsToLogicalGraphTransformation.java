package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.epgm.LogicalGraphFactory;
import org.gradoop.flink.model.impl.layouts.gve.GVEGraphLayoutFactory;
import org.gradoop.flink.model.impl.layouts.transactional.tuples.GraphTransaction;
import org.gradoop.flink.util.GradoopFlinkConfig;

import java.io.Serializable;

public class GraphTransactionsToLogicalGraphTransformation implements Serializable {

  LogicalGraphFactory graphFactory;

  public GraphTransactionsToLogicalGraphTransformation(GradoopFlinkConfig cfg) {
    GVEGraphLayoutFactory layoutFactory = new GVEGraphLayoutFactory();
    layoutFactory.setGradoopFlinkConfig(cfg);

    this.graphFactory = new LogicalGraphFactory(cfg);
    this.graphFactory.setLayoutFactory(layoutFactory);
  }

  public LogicalGraph transform(DataSet<GraphTransaction> transactions) {
    // each transaction is represented by one vertex
    DataSet<EPGMVertex> vertices = transactions.map(new TransactionToVertexMapper());

    DataSet<EPGMEdge> edges = transactions.cross(transactions).flatMap(new TransactionToEdgeMapper());

    return graphFactory.fromDataSets(vertices, edges);
  }
}
