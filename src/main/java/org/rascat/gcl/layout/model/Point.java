package org.rascat.gcl.layout.model;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.pojo.EPGMElement;

import java.io.Serializable;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class Point extends Vector2D implements Serializable {

  public Point(double x, double y) {
    super(x, y);
  }

  public static <E extends EPGMElement> Point fromEPGMElement(E element) {
    return fromEPGMElement(element, KEY_X_COORD, KEY_Y_COORD);
  }

  public static <E extends EPGMElement> Point fromEPGMElement(E element, String keyXCoord, String keyYCoord) {
    double x = element.getPropertyValue(keyXCoord).getDouble();
    double y = element.getPropertyValue(keyYCoord).getDouble();

    return new Point(x, y);
  }

  public <E extends EPGMElement> E addPositionPropertyToElement(E element) {
    return addPositionPropertyToElement(element, KEY_X_COORD, KEY_Y_COORD);
  }

  public <E extends EPGMElement> E addPositionPropertyToElement(E element, String keyXCoord, String keyYCoord) {
    element.setProperty(keyXCoord, this.getX());
    element.setProperty(keyYCoord, this.getY());
    return element;
  }
}
