package org.rascat.gcl.layout;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.layouting.FRLayouter;
import org.gradoop.flink.model.impl.operators.layouting.LayoutingAlgorithm;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.functions.select.SelectFirstGraphId;
import org.rascat.gcl.layout.transformations.SuperVertexReduce;

public class SuperVertexGraphCollectionLayout extends AbstractGraphCollectionLayout{

  private SuperVertexReduce reduce;
  private LayoutingAlgorithm superGraphLayout;

  public SuperVertexGraphCollectionLayout(int width, int height, GradoopFlinkConfig cfg) {
    super(width, height);
    this.reduce = new SuperVertexReduce(cfg);
    this.superGraphLayout = new FRLayouter(5, 5);
  }

  @Override
  public GraphCollection execute(GraphCollection collection) {
    // we start with the creation of a super-vertex-graph
    LogicalGraph superGraph = reduce.transform(collection);
    // create layout for super graph
    superGraph = superGraphLayout.execute(superGraph);

    DataSet<EPGMVertex> centeredVertices = superGraph.getVertices()
        .join(collection.getVertices())
        .where("id").equalTo(new SelectFirstGraphId())
        .with(new JoinFunction<Object, Object, Object>() {
          @Override
          public Object join(Object first, Object second) throws Exception {
            return null;
          }
        });
    return null;
  }
}
