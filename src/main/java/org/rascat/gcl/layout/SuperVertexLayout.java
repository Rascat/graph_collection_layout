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

/**
 * Class for the computation of a 2D-embedding of a graph collection with the super-vertex(TM) method.
 * <p>
 * The super-vertex method takes the idea of force-directed placement and applies it to a two-step layout process that
 * is applied to a EPGM graph collection. Generally speaking, the layout is computed as follows:
 *
 * <ol>
 *   <li>Create a logical graph from the graph collection, where each vertex in that graph represents a logical graph
 *   from the input graph collection</li>
 *   <li>Compute a layout for the new graph</li>
 *   <li>Transfer the position of each vertex to the set of vertices that belong to the graph that is represented by
 *   that vertex</li>
 *   <li>Compute layout for each vertex from the input graph collection, but restrict the layout space to the area
 *   around the previously computed center positions</li>
 * </ol>
 *
 * @author Lucas Schons
 */
public class SuperVertexLayout extends AbstractGraphCollectionLayout {

  private FRLayouter superGraphLayout;
  private double k;
  private double superK;
  private double superKFactor;
  private int numVertices;
  private int iterations;
  private int centerLayoutIterations;
  private StandardRepulsionFunction repulsionFunction;
  private StandardAttractionFunction attractionFunction;

  /**
   * Private constructor used by the nested {@link Builder} class.
   *
   * @param builder builder used to create this SuperVertexGraphCollectionLayout.
   */
  private SuperVertexLayout(Builder builder) {
    super(builder.width, builder.height);
    this.iterations = builder.iterations;
    this.centerLayoutIterations = builder.preLayoutIterations;
    this.k = builder.k;
    this.superK = builder.superK;
    this.superKFactor = builder.superKFactor;
    this.numVertices = builder.numVertices;
  }

  /**
   * Static method used to retrieve a {@link Builder} object.
   *
   * @param width  the total width of the layout space in px
   * @param height the total height of the layout space in px
   * @return a new instance of Builder.
   */
  public static Builder builder(int width, int height) {
    return new Builder(width, height);
  }

  @Override
  public GraphCollection execute(GraphCollection collection) throws Exception {
    DataSet<EPGMEdge> edges = collection.getEdges();
    DataSet<EPGMVertex> vertices = collection.getVertices();
    DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

    if (this.numVertices == 0) {
      this.numVertices = (int) vertices.count();
    }
    // compute k if not explicitly set to certain value
    if (this.k == -1) {
      this.k = computeK(this.numVertices);
    }

    // init repulsion/attraction function
    this.repulsionFunction = new StandardRepulsionFunction();
    this.repulsionFunction.setK(k);
    this.attractionFunction = new StandardAttractionFunction(k);

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

  /**
   * Computes the repulsive forces between vertices that belong to the same logical graph.
   *
   * @param vertices the input set of vertices
   * @return the resulting set of forces
   */
  private DataSet<Force> computeRepulsiveForces(DataSet<EPGMVertex> vertices) {
    return vertices.join(vertices)
      .where(new SelectFirstGraphId<>()).equalTo(new SelectFirstGraphId<>())
      .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) this.repulsionFunction);
  }

  /**
   * Computes the attractive forces between two vertices that are connected by an edge.
   *
   * @param vertices the input set of vertices
   * @param edges    the input set of edges
   * @return the resulting set of forces
   */
  private DataSet<Force> computeAttractiveForces(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
    DataSet<EPGMEdge> positionedEdges = edges
      .join(vertices).where("sourceId").equalTo("id").with(new TransferPosition(VertexType.TAIL))
      .join(vertices).where("targetId").equalTo("id").with(new TransferPosition(VertexType.HEAD));

    return positionedEdges.flatMap(this.attractionFunction);
  }

  /**
   * Initialize the FRLayouter object responsible for the computation of the center graph layout.
   *
   * @param graph the center graph
   * @throws Exception if something goes wrong
   */
  private void initSuperGraphLayout(LogicalGraph graph) throws Exception {
    long superGraphVertexCount = graph.getVertices().count();
    // compute superK if not set to certain value
    if (Double.compare(superK, -1) == 0) {
      this.superK = computeK((int) superGraphVertexCount) * superKFactor;
    }

    this.superGraphLayout = new FRLayouter(centerLayoutIterations, (int) superGraphVertexCount);
    this.superGraphLayout.k(superK);
    this.superGraphLayout.area(width, height);
  }

  /**
   * Compute the constant {@code k} for this layout space and a given number of vertices.
   * The definition of {@code k} is taken from 'Graph Drawing by Force-directed Placement' by Fruchterman & Reingold.
   *
   * @param vertexCount the number of vertices
   * @return the value of {@code k}
   */
  private double computeK(int vertexCount) {
    return Math.sqrt((double) (area() / vertexCount));
  }

  /**
   * A nested builder class to create {@link SuperVertexLayout} instances using descriptive methods.
   * <p>
   * Example usage:
   * <pre>
   *   SuperVertexGraphCollectionLayout layout = SuperVertexGraphCollectionLayout.builder(100, 100)
   *     .iterations(10)
   *     .preLayoutIterations(8)
   *     .build();
   * </pre>
   */
  public static final class Builder {

    // required
    private final int width;
    private final int height;

    //optional
    private double k = -1;
    private double superK = -1;
    private int iterations = 1;
    private int numVertices = 0;
    private int preLayoutIterations = 1;
    private double superKFactor = 1;

    /**
     * Private constructor used by {@link SuperVertexLayout#builder(int, int)}.
     *
     * @param width  the width of the total layout space
     * @param height the height of the total layout space
     */
    private Builder(int width, int height) {
      this.width = width;
      this.height = height;
    }

    /**
     * Sets the constant {@code k}.
     *
     * @param k the value for {@code k}
     * @return this Builder
     */
    public Builder k(double k) {
      if (k <= 0) {
        throw new IllegalArgumentException("K needs to be > 0.");
      }

      this.k = k;
      return this;
    }

    public Builder vertices(int vertices) {
      if (vertices <= 0) {
        throw new IllegalArgumentException("Number of vertices must be > 0.");
      }

      this.numVertices = vertices;
      return  this;
    }

    /**
     * Sets the constant {@code superK}.
     *
     * @param superK the value for {@code superK}
     * @return this Builder
     */
    public Builder superK(double superK) {
      if (superK <= 0) {
        throw new IllegalArgumentException("K needs to be > 0.");
      }

      this.superK = superK;
      return this;
    }

    /**
     * Sets the number of iterations of the main layout process.
     *
     * @param iterations the number of iterations
     * @return this Builder
     */
    public Builder iterations(int iterations) {
      if (iterations <= 0) {
        throw new IllegalArgumentException("Iterations needs to be a positive integer.");
      }

      this.iterations = iterations;
      return this;
    }

    /**
     * Sets the number of iterations of the center graph layout process.
     *
     * @param preLayoutIterations the number of iterations
     * @return this Builder
     */
    public Builder preLayoutIterations(int preLayoutIterations) {
      if (preLayoutIterations <= 0) {
        throw new IllegalArgumentException("Pre-Layout iterations needs to be a positive integer.");
      }

      this.preLayoutIterations = preLayoutIterations;
      return this;
    }

    /**
     * Sets the factor by which the constant {@code superK} is multiplied.
     *
     * @param superKFactor the value for the factor
     * @return this Builder
     */
    public Builder superKFactor(double superKFactor) {
      this.superKFactor = superKFactor;
      return this;
    }

    /**
     * Constructs a {@link SuperVertexLayout} with the values declared by this {@link Builder}.
     *
     * @return the new {@link SuperVertexLayout}
     */
    public SuperVertexLayout build() {
      return new SuperVertexLayout(this);
    }
  }
}
