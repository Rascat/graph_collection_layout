package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.CrossFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.functions.forces.RepulsionFunction;
import org.rascat.gcl.layout.model.Force;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class ComputeRepulsiveForces implements CrossFunction<EPGMVertex, EPGMVertex, Force>,
  JoinFunction<EPGMVertex, EPGMVertex, Force> {

  private double k;
  private RepulsionFunction function;

  private Vector2D delta;
  private double distance;

  public ComputeRepulsiveForces(double k, RepulsionFunction function) {
    this.k = k;
    this.function = function;
  }

  @Override
  public Force cross(EPGMVertex v, EPGMVertex u) {
    setPositionalValues(v, u);

    if (v.equals(u)) {
      return new Force(v.getId(), new Vector2D(0, 0));
    }

    Vector2D displacement;
    try {
      displacement = delta.normalize().scalarMultiply(function.repulsion(delta.getNorm(), k));
    } catch (MathArithmeticException e) {
      // we probably tried to normalize a vector with length 0, so we return a zero vector
      displacement = new Vector2D(0, 0);
    }

    return new Force(v.getId(), displacement);
  }

  @Override
  public Force join(EPGMVertex v, EPGMVertex u) throws Exception {
    setPositionalValues(v, u);

    if (v.equals(u) || distance > k) {
      return new Force(v.getId(), new Vector2D(0, 0));
    }

    Vector2D displacement;
    try {
      displacement = delta.normalize().scalarMultiply(function.repulsion(delta.getNorm(), k));
    } catch (MathArithmeticException e) {
      // we probably tried to normalize a vector with length 0, so we return a zero vector
      displacement = new Vector2D(0, 0);
    }

    return new Force(v.getId(), displacement);
  }

  private void setPositionalValues(EPGMVertex v, EPGMVertex u) {
    Vector2D vPos = new Vector2D(
      v.getPropertyValue(KEY_X_COORD).getDouble(),
      v.getPropertyValue(KEY_Y_COORD).getDouble());
    Vector2D uPos = new Vector2D(
      u.getPropertyValue(KEY_X_COORD).getDouble(),
      u.getPropertyValue(KEY_Y_COORD).getDouble());
    this.delta = vPos.subtract(uPos);
    this.distance = delta.getNorm();
  }
}
