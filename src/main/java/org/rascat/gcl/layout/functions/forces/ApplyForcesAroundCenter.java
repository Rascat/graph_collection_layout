package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.RichJoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;

import static org.rascat.gcl.layout.functions.prepare.TransferCenterPosition.*;

public class ApplyForcesAroundCenter extends RichJoinFunction<EPGMVertex, Force, EPGMVertex> {
  private int width;
  private int height;
  private double radius;

  private CoolingSchedule schedule;

  public ApplyForcesAroundCenter(int width, int height, double radius, CoolingSchedule schedule) {
    this.width = width;
    this.height = height;
    this.radius = radius;
    this.schedule = schedule;
  }

  @Override
  public EPGMVertex join(EPGMVertex vertex, Force force) {
    Point vPosition = Point.fromEPGMElement(vertex);
    Point vCenter = Point.fromEPGMElement(vertex, KEY_CENTER_X_COORD, KEY_CENTER_Y_COORD);

    Vector2D vDisp = force.getVector();

    int superstepNumber;
    try {
      superstepNumber = getIterationRuntimeContext().getSuperstepNumber();
    } catch (IllegalStateException e) {
      // if join is not executed in an iteration runtime context, we assign it a const
      // in order to make it more stable and testable
      superstepNumber = 1;
    }
    double temp = schedule.computeTemperature(superstepNumber);

    Vector2D newPosition;
    try {
      newPosition = vPosition.add(vDisp.normalize().scalarMultiply(Math.min(vDisp.getNorm(), temp)));
    } catch (MathArithmeticException e) {
      newPosition = vPosition.add(Vector2D.ZERO);
    }

    // Restrict position to super vertex layout space
    Double distance = newPosition.distance(vCenter);
    if (distance > radius) {
      newPosition = newPosition.normalize().scalarMultiply(radius);
    }

    double newX = newPosition.getX();
    double newY = newPosition.getY();

    // Restrict position to total layout space
    newX = Math.min(width, Math.max(0, newX));
    newY = Math.min(height, Math.max(0, newY));

    return new Point(newX, newY).addPositionPropertyToElement(vertex);
  }
}
