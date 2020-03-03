package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.RichJoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.functions.prepare.TransferCenterPosition;
import org.rascat.gcl.layout.model.Force;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;

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
    double x = vertex.getPropertyValue(KEY_X_COORD).getDouble();
    double y = vertex.getPropertyValue(KEY_Y_COORD).getDouble();
    double centerX = vertex.getPropertyValue(TransferCenterPosition.KEY_CENTER_X_COORD).getDouble();
    double centerY = vertex.getPropertyValue(TransferCenterPosition.KEY_CENTER_Y_COORD).getDouble();

    Vector2D vPos = new Vector2D(x, y);
    Vector2D vDisp = force.getVector();


    double temperature = schedule.computeTemperature(getIterationRuntimeContext().getSuperstepNumber());
    try {
      vPos = vPos.add(vDisp.normalize().scalarMultiply(Math.min(vDisp.getNorm(), temperature)));
    } catch (MathArithmeticException e) {
      vPos = vPos.add(new Vector2D(0,0));
    }
    double vPosX = vPos.getX();
    double vPosY = vPos.getY();

    // Restrict position to super vertex layout space
    vPosX = Math.min(centerX + radius, Math.max(centerX - radius, vPosX));
    vPosY = Math.min(centerY + radius, Math.max(centerY - radius, vPosY));

    // Restrict position to total layout space
    vPosX = Math.min(width, Math.max(0, vPosX));
    vPosY = Math.min(height, Math.max(0, vPosY));

    vertex.setProperty(KEY_X_COORD, vPosX);
    vertex.setProperty(KEY_Y_COORD, vPosY);

    return vertex;
  }
}
