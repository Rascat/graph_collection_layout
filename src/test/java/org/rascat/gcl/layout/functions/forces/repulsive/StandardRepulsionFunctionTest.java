package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;
import static org.testng.Assert.*;

public class StandardRepulsionFunctionTest {

  private StandardRepulsionFunction function;
  private final GradoopId vId = GradoopId.fromString("912345678910111213141516");
  private final GradoopId uId = GradoopId.fromString("1AB363914FD1325CC43790AB");

  @BeforeClass
  public void setUp() {
    this.function = new StandardRepulsionFunction();
    this.function.setK(7.5D);
  }

  @Test(dataProvider = "testCrossProvider")
  public void testCross(EPGMVertex v, EPGMVertex u, Force expectedForce) {
    Force actualForce = function.cross(v, u);

    assertEquals(actualForce, expectedForce);
  }

  @DataProvider
  public Object[][] testCrossProvider() {
    EPGMVertex v = createVertex(vId, 0, 0);
    Force zeroForce = new Force(vId, new Vector2D(0, 0));

    return new Object[][] {
      {v, v, zeroForce}
    };
  }

  @Test(dataProvider = "testJoinProvider")
  public void testJoin(EPGMVertex v, EPGMVertex u, Force expectedForce) {
    Force actualForce = function.join(v, u);

    assertEquals(actualForce, expectedForce);
  }

  @DataProvider
  public Object[][] testJoinProvider() {
    EPGMVertex v = createVertex(vId, 0, 0);
    EPGMVertex u = createVertex(uId, 20, 20);
    Force zeroForce = new Force(vId, new Vector2D(0, 0));

    return new Object[][] {
      {v, v, zeroForce},
      {v, u, zeroForce}
    };
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMissingCoordFailsWithException() {
    EPGMVertex v = new EPGMVertex();
    v.setId(vId);
    EPGMVertex u = new EPGMVertex();
    u.setId(uId);

    function.cross(v, u);
  }

  private EPGMVertex createVertex(GradoopId id, double x, double y) {
    EPGMVertex vertex = new EPGMVertex();
    vertex.setId(id);
    vertex.setProperty(KEY_X_COORD, x);
    vertex.setProperty(KEY_Y_COORD, y);

    return vertex;
  }
}