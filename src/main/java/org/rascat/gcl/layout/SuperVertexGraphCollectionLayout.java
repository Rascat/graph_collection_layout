package org.rascat.gcl.layout;

import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.operators.IterativeDataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.layouting.FRLayouter;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.functions.cooling.ExponentialSimulatedAnnealing;
import org.rascat.gcl.layout.functions.forces.ApplyForcesAroundCenter;
import org.rascat.gcl.layout.functions.forces.attractive.StandardAttractionFunction;
import org.rascat.gcl.layout.functions.forces.repulsive.StandardRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.RandomPlacementAroundCenter;
import org.rascat.gcl.layout.functions.prepare.TransferCenterPosition;
import org.rascat.gcl.layout.functions.prepare.TransferPosition;
import org.rascat.gcl.layout.functions.select.SelectFirstGraphId;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.VertexType;
import org.rascat.gcl.layout.transformations.SuperVertexReduce;

public class SuperVertexGraphCollectionLayout extends AbstractGraphCollectionLayout{

  private FRLayouter superGraphLayout;
  private double k;
  private double superK;
  private int iterations;
  private int centerLayoutIterations;

  public SuperVertexGraphCollectionLayout(Builder builder) {
    super(builder.width, builder.height);
    this.iterations = builder.iterations;
    this.centerLayoutIterations = builder.preLayoutIterations;
    this.k = builder.k;
    this.superK = builder.superK;
  }

  public static Builder builder(int width, int height) {
    return new Builder(width, height);
  }

  @Override
  public GraphCollection execute(GraphCollection collection) throws Exception {
    DataSet<EPGMEdge> edges = collection.getEdges();
    DataSet<EPGMVertex> vertices = collection.getVertices();
    DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

    // compute k if not explicitly set to certain value
    if (this.k == -1) {
      this.k = computeK((int) vertices.count());
    }

    // we start with the creation of a super-vertex-graph
    SuperVertexReduce reduce = new SuperVertexReduce(collection.getConfig());
    LogicalGraph superGraph = reduce.transform(collection);

    // create layout for super graph
    initSuperGraphLayout(superGraph);
    superGraph = superGraphLayout.execute(superGraph);

    DataSet<EPGMVertex> centeredVertices =
        superGraph.getVertices().join(vertices)
        .where("id").equalTo(new SelectFirstGraphId<>())
        .with(new TransferCenterPosition<>());

    DataSet<EPGMVertex> initVertices = centeredVertices.map(new RandomPlacementAroundCenter<>(superGraphLayout.getK()));

    IterativeDataSet<EPGMVertex> verticesLoop = initVertices.iterate(iterations);

    DataSet<Force> repulsiveForces = computeRepulsiveForces(verticesLoop);

    DataSet<Force> attractiveForces = computeAttractiveForces(verticesLoop, edges);

    DataSet<Force> forces = repulsiveForces.union(attractiveForces)
        .groupBy(Force.ID_POSITION)
        .reduce((firstForce, secondForce) -> {
          firstForce.setVector(firstForce.getVector().add(secondForce.getVector()));
          return firstForce;
        });

    CoolingSchedule schedule = new ExponentialSimulatedAnnealing(this.width, this.height, this.k, this.iterations);
    DataSet<EPGMVertex> positionedVertices = verticesLoop.closeWith(verticesLoop.join(forces)
        .where("id").equalTo(Force.ID_POSITION)
        .with(new ApplyForcesAroundCenter(width, height, superK, schedule)));

    return collection.getFactory().fromDataSets(graphHeads, positionedVertices, edges);
  }

  private DataSet<Force> computeRepulsiveForces(DataSet<EPGMVertex> vertices) {
    StandardRepulsionFunction repulsionFunction = new StandardRepulsionFunction();
    repulsionFunction.setK(k);

    return vertices.join(vertices)
        .where(new SelectFirstGraphId<>()).equalTo(new SelectFirstGraphId<>())
        .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) repulsionFunction);
  }


  private DataSet<Force> computeAttractiveForces(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges){
    DataSet<EPGMEdge> positionedEdges = edges
        .join(vertices).where("sourceId").equalTo("id").with(new TransferPosition(VertexType.TAIL))
        .join(vertices).where("targetId").equalTo("id").with(new TransferPosition(VertexType.HEAD));

    return positionedEdges.flatMap(new StandardAttractionFunction(this.k));
  }

  private void initSuperGraphLayout(LogicalGraph graph) throws Exception {
    long superGraphVertexCount = graph.getVertices().count();
    // compute superK if not set to certain value
    if (Double.compare(superK, -1) == 0) {
      this.superK = computeK((int) superGraphVertexCount);
    }

    this.superGraphLayout = new FRLayouter(centerLayoutIterations, (int) superGraphVertexCount);
    this.superGraphLayout.k(superK);
    this.superGraphLayout.area(width, height);
  }

  private double computeK (int vertexCount) {
    return Math.sqrt((double) (area() / vertexCount));
  }

  public static final class Builder {

    // required
    private final int width;
    private final int height;

    //optional
    private double k = -1;
    private double superK = -1;
    private int iterations = 1;
    private int preLayoutIterations = 1;

    private Builder(int width, int height) {
      this.width = width;
      this.height = height;
    }

    public Builder k(double k){
      if (k <= 0) {
        throw new IllegalArgumentException("K needs to be > 0.");
      }

      this.k = k;
      return this;
    }

    public Builder superK(double superK) {
      if (superK <= 0) {
        throw new IllegalArgumentException("K needs to be > 0.");
      }

      this.superK = superK;
      return this;
    }

    public Builder iterations(int iterations) {
      if (iterations <= 0) {
        throw new IllegalArgumentException("Iterations needs to be a positive integer.");
      }

      this.iterations = iterations;
      return this;
    }

    public Builder preLayoutIterations(int preLayoutIterations) {
      if (preLayoutIterations <= 0) {
        throw new IllegalArgumentException("Pre-Layout iterations needs to be a positive integer.");
      }

      this.preLayoutIterations = preLayoutIterations;
      return this;
    }

    public SuperVertexGraphCollectionLayout build() {
      return new SuperVertexGraphCollectionLayout(this);
    }
  }
}
