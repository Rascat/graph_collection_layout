package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;
import static org.testng.Assert.*;

public class StandardRepulsionFunctionTest {

  private StandardRepulsionFunction function;
  private List<Force> resultList;
  private Collector<Force> collector;
  private final GradoopId vId = GradoopId.fromString("912345678910111213141516");
  private final GradoopId uId = GradoopId.fromString("1AB363914FD1325CC43790AB");

  public StandardRepulsionFunctionTest() {
  }

  @BeforeTest
  public void setUp() {
    this.function = new StandardRepulsionFunction();
    this.function.setK(7.5D);
  }

  @BeforeMethod
  public void setUpCollector() {
    this.resultList = new ArrayList<>();
    this.collector = new ListCollector<>(resultList);
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

  @Test
  public void testFlatJoin() {
    EPGMVertex v = createVertex(vId, 0, 0);
    EPGMVertex u = createVertex(uId, 3, 3);

    function.join(v, u, collector);

    assertEquals(resultList.size(), 1);
    Force actualForce = resultList.get(0);
    assertEquals(actualForce.getVector().getNorm(), 13.25, 0.1);
  }

  @Test
  public void testFlatJoinOutOfRange() {
    // the two vertices are too far apart (distance > k) an should therefore not induce a force on.
    EPGMVertex v = createVertex(vId, 0, 0);
    EPGMVertex u = createVertex(uId, 20, 20);

    function.join(v, u, collector);

    assertEquals(resultList.size(), 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMissingCoordFailsWithException() {
    EPGMVertex v = new EPGMVertex();
    v.setId(vId);
    EPGMVertex u = new EPGMVertex();
    u.setId(uId);

    function.cross(v, u);
  }

  @Test
  public void testJoinWithRelocation() {
    EPGMVertex v = createVertex(vId, 1,1);
    EPGMVertex u = createVertex(uId, 1,1);

    Force result = function.join(v, u);
    Point vPos = Point.fromEPGMElement(v);
    Point uPos = Point.fromEPGMElement(u);

    assertNotEquals(vPos, uPos);
    assertTrue(result.getVector().getNorm() > 0);
  }

  @Test
  public void testFlatJoinWithRelocation() {
    EPGMVertex v = createVertex(vId, 1,1);
    EPGMVertex u = createVertex(uId, 1,1);

    function.join(v, u, collector);
    Point vPos = Point.fromEPGMElement(v);
    Point uPos = Point.fromEPGMElement(u);

    assertNotEquals(vPos, uPos);
    assertTrue(resultList.get(0).getVector().getNorm() > 0);
  }

  private EPGMVertex createVertex(GradoopId id, double x, double y) {
    EPGMVertex vertex = new EPGMVertex();
    vertex.setId(id);
    vertex.setProperty(KEY_X_COORD, x);
    vertex.setProperty(KEY_Y_COORD, y);

    return vertex;
  }
}