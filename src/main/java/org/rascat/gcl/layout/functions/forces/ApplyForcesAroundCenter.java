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

    double temp = schedule.computeTemperature(getIterationRuntimeContext().getSuperstepNumber());

    Vector2D newPosition;
    try {
      newPosition = vPosition.add(vDisp.normalize().scalarMultiply(Math.min(vDisp.getNorm(), temp)));
    } catch (MathArithmeticException e) {
      newPosition = vPosition.add(Vector2D.ZERO);
    }

    double newX = newPosition.getX();
    double newY = newPosition.getY();
    double centerX = vCenter.getX();
    double centerY = vCenter.getY();

    // Restrict position to super vertex layout space
    newX = Math.min(centerX + radius, Math.max(centerX - radius, newX));
    newY = Math.min(centerY + radius, Math.max(centerY - radius, newY));

    // Restrict position to total layout space
    newX = Math.min(width, Math.max(0, newX));
    newY = Math.min(height, Math.max(0, newY));

    return new Point(newX, newY).addPositionPropertyToElement(vertex);
  }
}
