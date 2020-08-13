package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.TestBase;
import org.rascat.gcl.layout.model.Force;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class WeightedRepulsionFunctionTest extends TestBase {

  private WeightedRepulsionFunction function;

  @BeforeTest
  public void setUpWeightedRepulsionFunction() {
    this.function = new WeightedRepulsionFunction(1, 25);
    this.function.setK(100);
  }

  @Test(dataProvider = "testCrossProvider")
  public void testCross(EPGMVertex v, EPGMVertex u, Force expectedForce) {
    Force actualForce = function.cross(v, u);

    assertEquals(actualForce.getId(), expectedForce.getId());
    assertEquals(actualForce.getVector().getX(), expectedForce.getVector().getX(), 0.1);
    assertEquals(actualForce.getVector().getY(), expectedForce.getVector().getY(), 0.1);
  }

  @DataProvider
  public Object[][] testCrossProvider() {
    EPGMVertex v = createVertex(vId, graphIdSetA, 0, 0);
    EPGMVertex u = createVertex(uId, graphIdSetA,3, 4);
    EPGMVertex w = createVertex(uId, graphIdSetB, 3, 4);
    Force zeroForce = new Force(vId, Vector2D.ZERO);

    return new Object[][] {
      {v, v, zeroForce},
      {v, u, new Force(vId, new Vector2D(-1200, -1600))},
      {v, w, new Force(vId, new Vector2D(-30000, -40000))}
    };
  }

  @Test(dataProvider = "testJoinProvider")
  public void testJoin(EPGMVertex v, EPGMVertex u, Force expectedForce) {
    Force actualForce = function.join(v, u);

    assertEquals(actualForce.getId(), expectedForce.getId());
    assertEquals(actualForce.getVector().getX(), expectedForce.getVector().getX(), 0.1);
    assertEquals(actualForce.getVector().getY(), expectedForce.getVector().getY(), 0.1);
  }

  @DataProvider
  public Object[][] testJoinProvider() {
    EPGMVertex v = createVertex(vId, graphIdSetA, 0, 0);
    EPGMVertex u = createVertex(uId, graphIdSetA, 3, 4);
    EPGMVertex w = createVertex(uId, graphIdSetB, 3, 4);
    EPGMVertex x = createVertex(uId, graphIdSetA, 101, 200);
    EPGMVertex z = createVertex(uId, graphIdSetAB, 3, 4);
    Force zeroForce = new Force(vId, Vector2D.ZERO);

    return new Object[][]{
      {v, v, zeroForce}, // Force between same vertex should be zero
      {v, u, new Force(vId, new Vector2D(-1200, -1600))}, // Force between vertices in same graph should not be amplified
      {v, w, new Force(vId, new Vector2D(-30000, -40000))}, // Force between vertices from different graphs should be amplified
      {v, x, zeroForce}, // vertices with distance > 2 * k
      {v, z, new Force(vId, new Vector2D(-1200, -1600))}
    };
  }
}