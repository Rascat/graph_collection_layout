package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.jetbrains.annotations.NotNull;
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
      // @TODO separate the two vertices by placing one of the nearby, at a random location
    }


    return getWeightedForce(v, u);
  }

  @Override
  public Force join(EPGMVertex v, EPGMVertex u) {
    setPositionalValues(v, u);

    if (v.equals(u) || distance > k) {
      return new Force(v.getId(), Vector2D.ZERO);
    }

    if (distance == 0) {
      // @TODO separate the two vertices by placing one of the nearby, at a random location
    }

    return getWeightedForce(v, u);
  }

  @NotNull
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
