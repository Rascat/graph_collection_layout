package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;

public class StandardRepulsionFunction extends RepulsionFunction {

  @Override
  public Force cross(EPGMVertex v, EPGMVertex u) {
    setPositionalValues(v, u);

    if (v.equals(u)) {
      return new Force(v.getId(), new Vector2D(0, 0));
    }

    return getForce(v, u);
  }

  @Override
  public Force join(EPGMVertex v, EPGMVertex u) throws Exception {
    setPositionalValues(v, u);

    if (v.equals(u) || distance > k) {
      return new Force(v.getId(), new Vector2D(0, 0));
    }

    if (distance == 0) {
      // @TODO separate the two vertices by placing one of the nearby, at a random location
    }
    return getForce(v, u);
  }

  private Force getForce(EPGMVertex v, EPGMVertex u) {
    Vector2D displacement;
    try {
      displacement = delta.normalize().scalarMultiply(repulsion(distance, k));
    } catch (MathArithmeticException e) {
      // we probably tried to normalize a vector with length 0, so we return a zero vector
      displacement = new Vector2D(0, 0);
    }

    return new Force(v.getId(), displacement);
  }

  private double repulsion(double distance, double optimalDistance) {
    return (optimalDistance * optimalDistance) / distance;
  }
}
