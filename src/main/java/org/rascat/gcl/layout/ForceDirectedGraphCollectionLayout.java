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

import java.util.StringJoiner;

public class ForceDirectedGraphCollectionLayout extends AbstractGraphCollectionLayout {

  private final int width;
  private final int height;
  private final double k;
  private final int iterations;
  private final boolean isIntermediaryLayout;
  private final MapFunction<EPGMVertex, EPGMVertex> initialLayout;
  private final RepulsiveForces repulsiveForces;
  private final AttractiveForces attractiveForces;

  public ForceDirectedGraphCollectionLayout(Builder builder) {
    this.width = builder.width;
    this.height = builder.height;
    this.k = builder.k;
    this.iterations = builder.iterations;
    this.isIntermediaryLayout = builder.isIntermediary;
    this.initialLayout = builder.initialLayout;
    this.repulsiveForces = builder.repulsiveForces;
    this.attractiveForces = builder.attractiveForces;
  }

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

  private int area() {
    return this.height * this.width;
  }

  private DataSet<Force> computeAttractiveForces(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
    return this.attractiveForces.compute(vertices, edges, this.k);
  }

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

  public static class Builder {

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

    public Builder(int width, int height, int vertices) {
      this.width = width;
      this.height = height;
      this.vertices = vertices;
    }

    public Builder k(double k) {
      if (k <= 0) {
        throw new IllegalArgumentException("K needs to be > 0.");
      }

      this.k = k;
      return this;
    }

    public Builder iterations(int iterations) {
      this.iterations = iterations;
      return this;
    }

    public Builder isIntermediary(boolean isIntermediary) {
      this.isIntermediary = isIntermediary;
      return this;
    }

    public Builder initialLayout(MapFunction<EPGMVertex, EPGMVertex> function) {
      this.initialLayout = function;
      return this;
    }

    public Builder repulsiveForces(RepulsiveForces repulsiveForces) {
      this.repulsiveForces = repulsiveForces;
      return this;
    }

    public Builder attractiveForces(AttractiveForces attractiveForces) {
      this.attractiveForces = attractiveForces;
      return this;
    }

    public ForceDirectedGraphCollectionLayout build() {
      if (this.initialLayout == null) {
        this.initialLayout = new RandomPlacement(this.width, this.height);
      }
      if (k == -1) {
        k = Math.sqrt((width * height) / (double) vertices);
      }

      return new ForceDirectedGraphCollectionLayout(this);
    }
  }
}
