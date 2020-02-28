package org.rascat.gcl.layout.model;

import org.gradoop.common.model.impl.pojo.EPGMElement;

import java.io.Serializable;
import java.util.Objects;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class Point implements Serializable {

  private double x;
  private double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public static Point fromEPGMElement(EPGMElement element) {
    return fromEPGMElement(element, KEY_X_COORD, KEY_Y_COORD);
  }

  public static Point fromEPGMElement(EPGMElement element, String keyXCoord, String keyYCoord) {
    double x = element.getPropertyValue(keyXCoord).getDouble();
    double y = element.getPropertyValue(keyYCoord).getDouble();

    return new Point(x, y);
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Point point = (Point) o;
    return Double.compare(point.getX(), getX()) == 0 &&
        Double.compare(point.getY(), getY()) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getX(), getY());
  }

  @Override
  public String toString() {
    return "Point{x=" + x + ", y=" + y + '}';
  }
}
