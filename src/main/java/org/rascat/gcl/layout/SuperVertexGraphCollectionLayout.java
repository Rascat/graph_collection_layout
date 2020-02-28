package org.rascat.gcl.layout;

import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.layouting.FRLayouter;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.functions.cooling.ExponentialSimulatedAnnealing;
import org.rascat.gcl.layout.functions.forces.ApplyForcesAroundCenter;
import org.rascat.gcl.layout.functions.forces.repulsive.StandardRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.RandomPlacementAroundCenter;
import org.rascat.gcl.layout.functions.prepare.TransferCenterPosition;
import org.rascat.gcl.layout.functions.select.SelectFirstGraphId;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.transformations.SuperVertexReduce;

public class SuperVertexGraphCollectionLayout extends AbstractGraphCollectionLayout{

  private SuperVertexReduce reduce;
  private FRLayouter superGraphLayout;
  private double k;
  private double superK;
  private int iterations = 1;

  public SuperVertexGraphCollectionLayout(int width, int height, GradoopFlinkConfig cfg) {
    super(width, height);
    this.reduce = new SuperVertexReduce(cfg);
  }

  @Override
  public GraphCollection execute(GraphCollection collection) throws Exception {
    this.k = (double) area() / collection.getVertices().count();
    // we start with the creation of a super-vertex-graph
    LogicalGraph superGraph = reduce.transform(collection);
    // create layout for super graph
    initSuperGraphLayout(superGraph);
    superGraph = superGraphLayout.execute(superGraph);

    DataSet<EPGMVertex> centeredVertices =
        superGraph.getVertices().join(collection.getVertices())
        .where("id").equalTo(new SelectFirstGraphId<>())
        .with(new TransferCenterPosition<>());


    DataSet<EPGMVertex> initVertices = centeredVertices.map(new RandomPlacementAroundCenter<>(superGraphLayout.getK()));

    DataSet<Force> repulsiveForces = computeRepulsiveForces(initVertices);

    CoolingSchedule schedule = new ExponentialSimulatedAnnealing(this.width, this.height, this.k, iterations);
    DataSet<EPGMVertex> pVertices = initVertices.join(repulsiveForces)
        .where("id").equalTo("f0")
        .with(new ApplyForcesAroundCenter(width, height, superK, schedule));
    
    return collection.getFactory().fromDataSets(collection.getGraphHeads(), pVertices, collection.getEdges());
  }

  private DataSet<Force> computeRepulsiveForces(DataSet<EPGMVertex> vertices) {
    StandardRepulsionFunction repulsionFunction = new StandardRepulsionFunction();
    System.out.println(k);
    repulsionFunction.setK(k);

    return vertices.join(vertices)
        .where(new SelectFirstGraphId<>()).equalTo(new SelectFirstGraphId<>())
        .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) repulsionFunction);
  }

  private DataSet<Force> computeAttractiveForces(DataSet<EPGMVertex>vertices, DataSet<EPGMEdge> edges){
    return null;
  }

  private void initSuperGraphLayout(LogicalGraph graph) throws Exception {
    long graphCount = graph.getVertices().count();
    this.superK = (double) area() / graphCount;

    this.superGraphLayout = new FRLayouter(5, (int) graphCount);
    this.superGraphLayout.k(superK);
  }
}
