package org.rascat.gcl.layout.functions.prepare;

import org.gradoop.common.model.api.entities.Element;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.AbstractGraphCollectionLayout;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RandomPlacementTest {

  private RandomPlacement<Element> randomPlacement;
  private int lowerBoundX;
  private int lowerBoundY;
  private int upperBoundX;
  private int upperBoundY;

  @BeforeClass
  public void setUp() {
    lowerBoundX = 5;
    lowerBoundY = 5;
    upperBoundX = 10;
    upperBoundY = 10;
    this.randomPlacement = new RandomPlacement<>(lowerBoundX, lowerBoundY, upperBoundX, upperBoundY);
  }

  @Test
  public void testMap() {
    Element actualElement = new EPGMVertex();
    actualElement = randomPlacement.map(actualElement);

    Double x = actualElement.getPropertyValue(AbstractGraphCollectionLayout.KEY_X_COORD).getDouble();
    Double y = actualElement.getPropertyValue(AbstractGraphCollectionLayout.KEY_Y_COORD).getDouble();
    assertNotNull(x);
    assertNotNull(y);
    assertNotEquals(x, y);
    assertTrue(lowerBoundX <= x && x < upperBoundX);
    assertTrue(lowerBoundY <= y && y < upperBoundY);
  }
}