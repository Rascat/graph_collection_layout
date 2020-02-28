package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMElement;
import org.rascat.gcl.layout.model.Point;

import java.util.concurrent.ThreadLocalRandom;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;
import static org.rascat.gcl.layout.functions.prepare.TransferCenterPosition.*;

public class RandomPlacementAroundCenter<E extends EPGMElement> implements MapFunction<E, E> {

  private double radius;

  public RandomPlacementAroundCenter(double radius) {
    this.radius = radius;
  }

  @Override
  public E map(E element) {
    Point center = Point.fromEPGMElement(element, KEY_CENTER_X_COORD, KEY_CENTER_Y_COORD);
    Point point = generateRandomPointAroundCenter(center);

    element.setProperty(KEY_X_COORD, point.getX());
    element.setProperty(KEY_Y_COORD, point.getY());
    return element;
  }

  Point generateRandomPointAroundCenter(Point center) {
    double r = radius * Math.sqrt(ThreadLocalRandom.current().nextDouble());
    double theta = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;

    double x = center.getX() + r * Math.cos(theta);
    double y = center.getY() + r * Math.sin(theta);

    return new Point(x,y);
  }
}
