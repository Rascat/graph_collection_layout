package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.RichJoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.model.Force;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

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
    public EPGMVertex join(EPGMVertex vertex, Force force) throws Exception {
        double x = vertex.getPropertyValue(KEY_X_COORD).getDouble();
        double y = vertex.getPropertyValue(KEY_Y_COORD).getDouble();

        Vector2D vPos = new Vector2D(x, y);
        Vector2D vDisp = force.getVector();


        double temperature = schedule.computeTemperature(getIterationRuntimeContext().getSuperstepNumber());
        vPos = vPos.add(vDisp.normalize().scalarMultiply(Math.min(vDisp.getNorm(), temperature)));
        double vPosX = vPos.getX();
        double vPosY = vPos.getY();

        vPosX = Math.min(width, Math.max(0, vPosX));
        vPosY = Math.min(height, Math.max(0, vPosY));

        vertex.setProperty(KEY_X_COORD, vPosX);
        vertex.setProperty(KEY_Y_COORD, vPosY);

        System.out.println(String.format("[%s]: (%.0f, %.0f)-->(%.0f, %.0f)", vertex.getId().toString().substring(vertex.getId().toString().length() - 4), x, y, vPosX, vPosY));
        return vertex;
    }
}
