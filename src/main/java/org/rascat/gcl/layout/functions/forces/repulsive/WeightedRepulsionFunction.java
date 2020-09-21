package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;

public class WeightedRepulsionFunction extends RepulsionFunction {

  private double sameGraphFactor;
  private double differentGraphFactor;

  public WeightedRepulsionFunction(double sameGraphFactor, double differentGraphFactor) {
    this.sameGraphFactor = sameGraphFactor;
    this.differentGraphFactor = differentGraphFactor;
  }

  @Override
  public Force cross(EPGMVertex v, EPGMVertex u) {
    setPositionalValues(v, u);

    if (v.equals(u)) {
      return new Force(v.getId(), Vector2D.ZERO);
    }

    if (distance == 0) {
      relocate(v);
      setPositionalValues(v, u);
    }


    return getWeightedForce(v, u);
  }

  @Override
  public Force join(EPGMVertex v, EPGMVertex u) {
    setPositionalValues(v, u);

    if (v.equals(u) || distance > maxDistance()) {
      return new Force(v.getId(), Vector2D.ZERO);
    }

    if (distance == 0) {
      relocate(v);
      setPositionalValues(v, u);
    }

    return getWeightedForce(v, u);
  }

  @Override
  public void join(EPGMVertex v, EPGMVertex u, Collector<Force> out) {
    setPositionalValues(v, u);

    if (v.equals(u) || distance > maxDistance()) {
      return;
    }

    if (distance == 0) {
      relocate(v);
      setPositionalValues(v, u);
    }

    Force force = getWeightedForce(v, u);
    out.collect(force);
    Vector2D reverseVector = force.getVector().scalarMultiply(-1);
    out.collect(new Force(u.getId(), reverseVector));
  }

  private Force getWeightedForce(EPGMVertex v, EPGMVertex u) {
    boolean sameGraph = u.getGraphIds().containsAny(v.getGraphIds());

    Vector2D displacement;
    try {
      displacement = delta.normalize().scalarMultiply(weightedRepulsion(distance, k, sameGraph));
    } catch (MathArithmeticException e) {
      // we probably tried to normalize a vector with length 0, so we return a zero vector
      displacement = new Vector2D(0, 0);
    }

    return new Force(v.getId(), displacement);
  }

  private double weightedRepulsion(double distance, double optimalDistance, boolean sameGraph) {
    double factor = sameGraph ? sameGraphFactor : differentGraphFactor;
    return ((optimalDistance * optimalDistance) / distance) * factor;
  }
}
