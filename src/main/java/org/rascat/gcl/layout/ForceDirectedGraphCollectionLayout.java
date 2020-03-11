package org.rascat.gcl.layout;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.operators.IterativeDataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.layout.api.AttractiveForces;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.api.RepulsiveForces;
import org.rascat.gcl.layout.functions.cooling.ExponentialSimulatedAnnealing;
import org.rascat.gcl.layout.functions.forces.*;

import org.rascat.gcl.layout.functions.forces.attractive.StandardAttractiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.NaiveRepulsiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.StandardRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.RandomPlacement;

import org.rascat.gcl.layout.model.Force;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Class for the computation of a 2D-embedding for a graph collection with a modified Fruchterman-Reingold approach.
 * The computation of the forces that produce the layout generally follows FR's proposal, where repulsive forces are
 * simulated between all vertices (or all vertices within a certain distance) and attractive forces are simulated for
 * all vertices that are connected by an edge. However, this implementation utilizes the fact that every vertex belongs
 * to one ore more logical graphs to modify the computed forces. By doing this, it is possible to make graph structures
 * in regard to logical graphs more visible in the resulting layout.
 */
public class ForceDirectedGraphCollectionLayout extends AbstractGraphCollectionLayout {

  private final double k;
  private final int iterations;
  private final boolean isIntermediaryLayout;
  private final MapFunction<EPGMVertex, EPGMVertex> initialLayout;
  private final RepulsiveForces repulsiveForces;
  private final AttractiveForces attractiveForces;

  /**
   * Private constructor used by the nested {@link Builder} to create a ForceDirectedGraphCollectionLayout object.
   *
   * @param builder the builder which contains the values used to create the object
   */
  private ForceDirectedGraphCollectionLayout(Builder builder) {
    super(builder.width, builder.height);
    this.k = builder.k;
    this.iterations = builder.iterations;
    this.isIntermediaryLayout = builder.isIntermediary;
    this.initialLayout = builder.initialLayout;
    this.repulsiveForces = builder.repulsiveForces;
    this.attractiveForces = builder.attractiveForces;
  }

  /**
   * Static method used to retrieve a {@link Builder} object.
   *
   * @param width    the total width of the layout space in px
   * @param height   the total height of the layout space in px
   * @param vertices the number of vertices in the graph collection
   * @return a new instance of Builder.
   */
  public static Builder builder(int width, int height, int vertices) {
    return new Builder(width, height, vertices);
  }

  @Override
  public GraphCollection execute(GraphCollection collection) {
    DataSet<EPGMEdge> edges = collection.getEdges();
    DataSet<EPGMVertex> vertices = collection.getVertices();
    DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

    if (!isIntermediaryLayout) {
      vertices = vertices.map(initialLayout);
    } else {
      // make sure int values get cast to double
      vertices = vertices.map(vertex -> {
        if (vertex.getPropertyValue(KEY_X_COORD).isInt()) {
          vertex.setProperty(KEY_X_COORD, (double) vertex.getPropertyValue(KEY_X_COORD).getInt());
        }
        if (vertex.getPropertyValue(KEY_Y_COORD).isInt()) {
          vertex.setProperty(KEY_Y_COORD, (double) vertex.getPropertyValue(KEY_Y_COORD).getInt());
        }
        return vertex;
      });
    }

    IterativeDataSet<EPGMVertex> loop = vertices.iterate(iterations);

    DataSet<Force> repulsiveForces = computeRepulsiveForces(loop);

    DataSet<Force> attractiveForces = computeAttractiveForces(loop, edges);

    DataSet<Force> forces = repulsiveForces.union(attractiveForces)
      .groupBy(Force.ID_POSITION)
      .reduce((firstForce, secondForce) -> {
        firstForce.setVector(firstForce.getVector().add(secondForce.getVector()));
        return firstForce;
      });

    CoolingSchedule schedule = new ExponentialSimulatedAnnealing(this.width, this.height, this.k, this.iterations);
    DataSet<EPGMVertex> pVertices = loop.closeWith(
      loop.join(forces)
        .where("id").equalTo("f0")
        .with(new ApplyForces(width, height, schedule)));

    return collection.getFactory().fromDataSets(graphHeads, pVertices, edges);
  }

  /**
   * Computes the attractive forces between two vertices that are connected by an edge.
   *
   * @param vertices the input set of vertices
   * @param edges    the input set of edges
   * @return the resulting set of forces
   */
  private DataSet<Force> computeAttractiveForces(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
    return this.attractiveForces.compute(vertices, edges, this.k);
  }

  /**
   * Computes the repulsive forces between vertices that belong to the same logical graph.
   *
   * @param vertices the input set of vertices
   * @return the resulting set of forces
   */
  private DataSet<Force> computeRepulsiveForces(DataSet<EPGMVertex> vertices) {
    return this.repulsiveForces.compute(vertices, this.k);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ForceDirectedGraphCollectionLayout.class.getSimpleName() + "[", "]")
      .add("width=" + width)
      .add("height=" + height)
      .add("k=" + k)
      .add("iterations=" + iterations)
      .add("isIntermediaryLayout=" + isIntermediaryLayout)
      .add("initialLayout=" + initialLayout)
      .add("repulsiveForces=" + repulsiveForces.getClass().getSimpleName())
      .add("attractiveForces=" + attractiveForces.getClass().getSimpleName())
      .toString();
  }

  /**
   * A nested builder class to create {@link ForceDirectedGraphCollectionLayout} instances using descriptive methods.
   * <p>
   * Example usage:
   * <pre>
   *   ForceDirectedGraphCollectionLayout layout = ForceDirectedGraphCollectionLayout.builder(100, 100, 20)
   *     .iterations(10)
   *     .build();
   * </pre>
   */
  public static final class Builder {

    // required
    private final int width;
    private final int height;
    private final int vertices;

    // optional
    private double k = -1;
    private int iterations = 1;
    private boolean isIntermediary = false;
    private MapFunction<EPGMVertex, EPGMVertex> initialLayout; // we initialize this during build()
    private RepulsiveForces repulsiveForces = new NaiveRepulsiveForces(new StandardRepulsionFunction());
    private AttractiveForces attractiveForces = new StandardAttractiveForces();

    /**
     * Private constructor used by the parent class to create a {@link Builder} object.
     *
     * @param width    the total width of the layout space
     * @param height   the total height of the layout space
     * @param vertices the amount of vertices in the graph collection
     */
    private Builder(int width, int height, int vertices) {
      this.width = width;
      this.height = height;
      this.vertices = vertices;
    }

    /**
     * Sets the constant {@code k}.
     *
     * @param k the value for {@code k}
     * @return this Builder
     * @throws IllegalArgumentException if the value of k is <= 0
     */
    public Builder k(double k) throws IllegalArgumentException {
      if (k <= 0) {
        throw new IllegalArgumentException("K needs to be > 0.");
      }

      this.k = k;
      return this;
    }

    /**
     * Sets the number of iterations of the layout process.
     *
     * @param iterations the number of iterations
     * @return this Builder
     * @throws IllegalArgumentException if the number of iterations is smaller than one
     */
    public Builder iterations(int iterations) throws IllegalArgumentException {
      if (iterations < 1) {
        throw new IllegalArgumentException("The number of iterations needs to be >= 1.");
      }

      this.iterations = iterations;
      return this;
    }

    /**
     * Flag to determine whether the graph collection to be processed contains layout information or not
     *
     * @param isIntermediary specifies whether the graph collection to be processed already contains layout information
     * @return this Builder
     */
    public Builder isIntermediary(boolean isIntermediary) {
      this.isIntermediary = isIntermediary;
      return this;
    }

    /**
     * Sets the {@link MapFunction} that is responsible for the initial positioning of the vertices.
     *
     * @param function the MapFunction that positions every vertex on a 2D plane
     * @return this Builder
     * @throws NullPointerException if the value of {@code function} is {@code null}
     */
    public Builder initialLayout(MapFunction<EPGMVertex, EPGMVertex> function) throws NullPointerException {
      this.initialLayout = Objects.requireNonNull(function, "MapFunction must not be null.");
      return this;
    }

    /**
     * Sets the {@link RepulsiveForces} object.
     *
     * @param repulsiveForces the object responsible for the computation of repulsive forces
     * @return this Builder
     * @throws NullPointerException if the value of {@code repulsiveForces} is {@code null}
     */
    public Builder repulsiveForces(RepulsiveForces repulsiveForces) throws NullPointerException {
      this.repulsiveForces = Objects.requireNonNull(repulsiveForces, "RepulsiveForces must not be null");
      return this;
    }

    /**
     * Sets the {@link AttractiveForces} object.
     *
     * @param attractiveForces the object responsible for the computation of attractive forces.
     * @return this Builder
     * @throws NullPointerException if the value of {@code attractiveForces} is {@code null}
     */
    public Builder attractiveForces(AttractiveForces attractiveForces) throws NullPointerException {
      this.attractiveForces = Objects.requireNonNull(attractiveForces);
      return this;
    }

    /**
     * Constructs a {@link ForceDirectedGraphCollectionLayout} with the values declared by this {@link Builder}.
     *
     * @return the new {@link ForceDirectedGraphCollectionLayout}
     */
    public ForceDirectedGraphCollectionLayout build() {
      if (this.initialLayout == null) {
        this.initialLayout = new RandomPlacement<>(this.width, this.height);
      }
      if (k == -1) {
        k = Math.sqrt((width * height) / (double) vertices);
      }

      return new ForceDirectedGraphCollectionLayout(this);
    }
  }
}
