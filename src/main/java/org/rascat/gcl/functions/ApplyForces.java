package org.rascat.gcl.functions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.RichJoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.model.Force;

public class ApplyForces extends RichJoinFunction<EPGMVertex, Force, EPGMVertex> {

    private double t;
    private int width;
    private int height;

    public ApplyForces(int width, int height) {
        this((double) width / 10, width, height);
    }

    public ApplyForces(double t, int width, int height) {
        this.t = t;
        this.width = width;
        this.height = height;
    }

    @Override
    public EPGMVertex join(EPGMVertex vertex, Force force) throws Exception {
        double x = vertex.getPropertyValue("X").getDouble();
        double y = vertex.getPropertyValue("Y").getDouble();

        Vector2D vPos = new Vector2D(x, y);
        Vector2D vDisp = force.getVector();

        vPos = vPos.add(vDisp.scalarMultiply(1 / vDisp.getNorm()).scalarMultiply(Math.min(vDisp.getNorm(), t)));
        double vPosX = vPos.getX();
        double vPosY = vPos.getY();

        vPosX = Math.min(width, Math.max(0, vPosX));
        vPosY = Math.min(height, Math.max(0, vPosY));

        vertex.setProperty("X", vPosX);
        vertex.setProperty("Y", vPosY);

        System.out.println(String.format("[%s]: (%.0f, %.0f)-->(%.0f, %.0f)", vertex.getId().toString().substring(vertex.getId().toString().length() - 4), x, y, vPosX, vPosY));
        return vertex;
    }

    private void cool() {

    }
}
