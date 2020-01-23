package org.rascat.gcl.layout.functions.forces;

import org.testng.annotations.Test;
import org.testng.Assert;


public class WeightedAttractingForceFunctionTest {

  @Test
  public void testDefaultWeightedAttraction() {
    WeightedAttractingForceFunction force = new WeightedAttractingForceFunction();
    double distance = 10D;
    double optimalDistance = 7D;
    boolean sameGraph = false;

    double expected = 6;

    Assert.assertEquals(force.weightedAttraction(distance, optimalDistance, sameGraph), expected);
  }
}