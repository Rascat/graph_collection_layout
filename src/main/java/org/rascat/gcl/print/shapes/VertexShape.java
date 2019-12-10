package org.rascat.gcl.print.shapes;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VertexShape extends Ellipse2D {

    private double x;
    private double y;
    private double r;

    public VertexShape(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public VertexShape(Point2D point, int r) {
        this.x = point.getX();
        this.y = point.getY();
        this.r = r;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getWidth() {
        return this.r;
    }

    @Override
    public double getHeight() {
        return this.r;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void setFrame(double v, double v1, double v2, double v3) {
        this.x = v;
        this.y = v1;
        this.r = v2;
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(x,y,r,r);
    }
}
