package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.AbstractGraphCollectionLayout;
import org.rascat.gcl.layout.model.Force;

import java.util.concurrent.ThreadLocalRandom;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class StandardRepulsionFunction extends RepulsionFunction implements FlatJoinFunction<EPGMVertex, EPGMVertex, Force> {

  @Override
  public Force cross(EPGMVertex v, EPGMVertex u) {
    setPositionalValues(v, u);

    if (v.equals(u)) {
      return new Force(v.getId(), new Vector2D(0, 0));
    }

    return getForce(v.getId());
  }

  @Override
  public Force join(EPGMVertex v, EPGMVertex u) {
    setPositionalValues(v, u);

    if (v.equals(u) || distance > k) {
      return new Force(v.getId(), new Vector2D(0, 0));
    }

    if (distance == 0) {
      relocate(v);
      setPositionalValues(v, u);
    }

    return getForce(v.getId());
  }

  @Override
  public void join(EPGMVertex v, EPGMVertex u, Collector<Force> out) {
    setPositionalValues(v, u);

    if (v.equals(u)) {
      return;
    }

    if (distance == 0) {
      relocate(v);
      setPositionalValues(v, u);
    }

    out.collect(getForce(v.getId()));
  }

  private Force getForce(GradoopId id) {
    Vector2D displacement;
    try {
      displacement = delta.normalize().scalarMultiply(repulsion(distance, k));
    } catch (MathArithmeticException e) {
      // we probably tried to normalize a vector with length 0, so we return a zero vector
      displacement = new Vector2D(0, 0);
    }

    return new Force(id, displacement);
  }

  private double repulsion(double distance, double optimalDistance) {
    return (optimalDistance * optimalDistance) / distance;
  }

  /**
   * Generate a random integer between 0 and 7 and move the vertex in a direction as illustrated below.
   *
   *    7  0  1
   *     \ | /
   *      \|/
   * 6 –––( )––– 2
   *      /|\
   *     / | \
   *    5  4  3
   *
   * @param  vertex The vertex to be relocated.
   */
  private void relocate(EPGMVertex vertex) {
    int direction = ThreadLocalRandom.current().nextInt(8);
    int x = (int) vertex.getPropertyValue(KEY_X_COORD).getDouble();
    int y = (int) vertex.getPropertyValue(KEY_Y_COORD).getDouble();

    switch(direction) {
      case 0: y -= 1; break;
      case 1: x += 1; y -= 1; break;
      case 2: y += 1; break;
      case 3: x += 1; y += 1; break;
      case 4: x += 1; break;
      case 5: x -= 1; y += 1; break;
      case 6: x -= 1; break;
      case 7: x -= 1; y -= 1; break;
    }
    vertex.setProperty(KEY_X_COORD, (double) x);
    vertex.setProperty(KEY_Y_COORD, (double) y);
  }
}
