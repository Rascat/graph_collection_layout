package org.rascat.gcl.functions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.model.Force;

public class ApplyForces implements JoinFunction<EPGMVertex, Force, EPGMVertex> {

    private double t;
    private int width;
    private int height;

    public ApplyForces(double t, int width, int height) {
        this.t = t;
        this.width = width;
        this.height = height;
    }

    @Override
    public EPGMVertex join(EPGMVertex vertex, Force force) throws Exception {
        int x = vertex.getPropertyValue("X").getInt();
        int y = vertex.getPropertyValue("Y").getInt();

        Vector2D vPos = new Vector2D(x, y);
        Vector2D vDisp = force.getVector();

        vPos = vPos.add(vDisp.scalarMultiply(1 / vDisp.getNorm()).scalarMultiply(Math.min(vDisp.getNorm(), t)));
        double vPosX = vPos.getX();
        double vPosY = vPos.getY();

        vPosX = Math.min(width, Math.max(0, vPosX));
        vPosY = Math.min(height, Math.max(0, vPosY));

        vertex.setProperty("X", vPosX);
        vertex.setProperty("Y", vPosY);

        return vertex;
    }
}
