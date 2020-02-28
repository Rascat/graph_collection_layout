package org.rascat.gcl.layout.functions.prepare;

import org.gradoop.common.model.impl.pojo.EPGMElement;
import org.rascat.gcl.layout.model.Point;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RandomPlacementAroundCenterTest {

  @Test
  public void testGenerateRandomPointAroundCenter() {
    double radius = 10;
    RandomPlacementAroundCenter<EPGMElement> placement = new RandomPlacementAroundCenter<>(radius);

    Point center = new Point(0, 0);
    Point point = placement.generateRandomPointAroundCenter(center);

    assertTrue(pointIsWithinCircle(point, center, radius),
        "Point " + point + " is not within circle around " + center + " with radius " + radius);
  }

  private boolean pointIsWithinCircle(Point point, Point center, double radius) {
    double distanceSquared = Math.pow(point.getX() - center.getX(), 2) + Math.pow(point.getY() - center.getY(), 2);
    return distanceSquared <= radius * radius;
  }
}