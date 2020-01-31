package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.CrossFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;
import static org.rascat.gcl.layout.model.VertexType.HEAD;
import static org.rascat.gcl.layout.model.VertexType.TAIL;

public abstract class RepulsionFunction implements JoinFunction<EPGMVertex, EPGMVertex, Force>,
  CrossFunction<EPGMVertex, EPGMVertex, Force> {

  protected double k;
  protected Vector2D delta;
  protected double distance;

  protected void setK(double k) {
    this.k = k;
  }

  protected void setPositionalValues(EPGMVertex v, EPGMVertex u) {
    checkVertices(v, u);

    Vector2D vPos = new Vector2D(
      v.getPropertyValue(KEY_X_COORD).getDouble(),
      v.getPropertyValue(KEY_Y_COORD).getDouble());
    Vector2D uPos = new Vector2D(
      u.getPropertyValue(KEY_X_COORD).getDouble(),
      u.getPropertyValue(KEY_Y_COORD).getDouble());
    this.delta = vPos.subtract(uPos);
    this.distance = delta.getNorm();
  }

  private static void checkVertices(EPGMVertex v, EPGMVertex u) {
    if (!v.hasProperty(KEY_X_COORD) || !v.hasProperty(KEY_Y_COORD)) {
      throw new IllegalArgumentException("Provided vertex " + v + " did not contain coordinates.");
    }
    if (!u.hasProperty(KEY_X_COORD) || !u.hasProperty(KEY_Y_COORD)) {
      throw new IllegalArgumentException("Provided vertex " + u + " did  not contain coordinates.");
    }
  }
}
