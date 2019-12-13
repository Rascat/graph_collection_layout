package org.rascat.gcl.functions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.CrossFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.model.Force;

public class ComputeRepulsiveForces implements CrossFunction<EPGMVertex, EPGMVertex, Force> {

  private double k;

  public ComputeRepulsiveForces(double k) {
    this.k = k;
  }

  @Override
  public Force cross(EPGMVertex v, EPGMVertex u) throws Exception {
    Vector2D vPos = new Vector2D(v.getPropertyValue("X").getInt(), v.getPropertyValue("Y").getInt());
    Vector2D uPos = new Vector2D(u.getPropertyValue("X").getInt(), u.getPropertyValue("Y").getInt());
    Vector2D delta = vPos.subtract(uPos);

    if (v.getId().equals(u.getId())) {
      return new Force(v.getId(), new Vector2D(0,0));
    }

    Vector2D displacement = delta.scalarMultiply(1 / delta.getNorm()).scalarMultiply(f(delta.getNorm()));

    Force force = new Force(v.getId(), displacement);
    System.out.println(force);
    return force;
  }

  private double f(double norm) {
    return Math.pow(k, 2) / norm;
  }
}
