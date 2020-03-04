package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.RichJoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;

public class ApplyForces extends RichJoinFunction<EPGMVertex, Force, EPGMVertex> {

    private int width;
    private int height;

    private CoolingSchedule schedule;

    public ApplyForces(int width, int height, CoolingSchedule schedule) {
        this.width = width;
        this.height = height;
        this.schedule = schedule;
    }

    public void setSchedule(CoolingSchedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public EPGMVertex join(EPGMVertex vertex, Force force) {
        Point vPosition = Point.fromEPGMElement(vertex);
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

        newX = Math.min(width, Math.max(0, newX));
        newY = Math.min(height, Math.max(0, newY));

        return new Point(newX, newY).addPositionPropertyToElement(vertex);
    }
}
