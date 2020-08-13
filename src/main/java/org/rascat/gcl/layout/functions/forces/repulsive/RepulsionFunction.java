package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.CrossFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;

import java.util.concurrent.ThreadLocalRandom;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;


public abstract class RepulsionFunction implements JoinFunction<EPGMVertex, EPGMVertex, Force>,
  CrossFunction<EPGMVertex, EPGMVertex, Force> {

  protected double k;
  protected Vector2D delta;
  protected double distance;

  public void setK(double k) {
    this.k = k;
  }

  protected void setPositionalValues(EPGMVertex v, EPGMVertex u) {
    checkVertices(v, u);

    Point vPosition = Point.fromEPGMElement(v);
    Point uPosition = Point.fromEPGMElement(u);

    this.delta = vPosition.subtract(uPosition);
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

  /**
   * The max distance to which the repulsive forces between two vertices are computed. If the distance
   * between the two vertices is greater, we can ignore them as they do not play a significant role
   * for the layout anymore. The value is taken from Fruchterman & Reingold's grid variant of their
   * force-directed placement algorithm.
   *
   * @return 2 * k
   */
  protected double maxDistance() {
    return 2 * k;
  }


  /**
   * Generate a random integer between 0 and 7 and move the vertex in a direction as illustrated below.
   *  <pre>
   *    7  0  1
   *     \ | /
   *      \|/
   * 6 –––( )––– 2
   *      /|\
   *     / | \
   *    5  4  3
   * </pre>
   *
   * @param  vertex The vertex to be relocated.
   */
  protected void relocate(EPGMVertex vertex) {
    int direction = ThreadLocalRandom.current().nextInt(8);
    Point position = Point.fromEPGMElement(vertex);
    double x =  position.getX();
    double y =  position.getY();

    int step = 1;

    switch(direction) {
      case 0: y -= step; break;
      case 1: x += step; y -= step; break;
      case 2: y += step; break;
      case 3: x += step; y += step; break;
      case 4: x += step; break;
      case 5: x -= step; y += step; break;
      case 6: x -= step; break;
      case 7: x -= step; y -= step; break;
    }

    Point newPosition = new Point(x, y);
    newPosition.addPositionPropertyToElement(vertex);
  }
}
