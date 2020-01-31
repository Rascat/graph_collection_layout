package org.rascat.gcl.layout.functions.forces.attractive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.rascat.gcl.layout.model.Force;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.rascat.gcl.layout.model.VertexType.HEAD;
import static org.rascat.gcl.layout.model.VertexType.TAIL;
import static org.testng.Assert.*;

public class StandardAttractionFunctionTest {

  private StandardAttractionFunction function;

  private final double delta = 0.0000000001;
  private final GradoopId sourceId = GradoopId.fromString("912345678910111213141516");
  private final GradoopId targetId = GradoopId.fromString("1AB363914FD1325CC43790AB");

  @BeforeClass
  public void setUp() {
    this.function = new StandardAttractionFunction(7.5D);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMissingCoordFailsWithException() {
    EPGMEdge edge = new EPGMEdge();

    this.function.map(edge);
  }

  @Test(dataProvider = "testMapProvider")
  public void testMap(EPGMEdge edge, Force expectedForce) {
    Force actualForce = function.map(edge);

    assertEquals(actualForce.getId(), expectedForce.getId());
    assertEquals(actualForce.getVector().getX(), expectedForce.getVector().getX(), delta);
    assertEquals(actualForce.getVector().getY(), expectedForce.getVector().getY(), delta);
  }

  @DataProvider
  public Object[][] testMapProvider() {
    EPGMEdge edge1 = createEdge(10, 10, 3, 3);
    Force expectedForce1 = new Force(sourceId, new Vector2D(-9.2395286075,-9.2395286075));
    EPGMEdge edge2 = createEdge(3, 3, 3, 3);
    Force expectedForce2 = new Force(sourceId, new Vector2D(0, 0));

    return new Object[][] {
      {edge1, expectedForce1},
      {edge2, expectedForce2}
    };
  }

  @Test
  public void testFlatMap() {
    EPGMEdge edge = createEdge(10, 10, 3, 3);

    Force expectedForceV = new Force(sourceId, new Vector2D(-9.2395286075,-9.2395286075));
    Force expectedForceU = new Force(targetId, new Vector2D(9.2395286075,9.2395286075));

    List<Force> actualForces = new ArrayList<>();
    Collector<Force> forceCollector = new ListCollector<>(actualForces);

    function.flatMap(edge, forceCollector);

    assertEquals(actualForces.size(), 2);
    assertEquals(actualForces.get(0).getId(), expectedForceV.getId());
    assertEquals(actualForces.get(0).getVector().getX(), expectedForceV.getVector().getX(), delta);
    assertEquals(actualForces.get(0).getVector().getY(), expectedForceV.getVector().getY(), delta);
    assertEquals(actualForces.get(1).getId(), expectedForceU.getId());
    assertEquals(actualForces.get(1).getVector().getX(), expectedForceU.getVector().getX(), delta);
    assertEquals(actualForces.get(1).getVector().getY(), expectedForceU.getVector().getY(), delta);
  }

  private EPGMEdge createEdge(double tailX, double tailY, double headX, double headY) {
    EPGMEdge edge = new EPGMEdge();

    edge.setSourceId(sourceId);
    edge.setTargetId(targetId);
    edge.setProperty(TAIL.getKeyX(), tailX);
    edge.setProperty(TAIL.getKeyY(), tailY);
    edge.setProperty(HEAD.getKeyX(), headX);
    edge.setProperty(HEAD.getKeyY(), headY);

    return edge;
  }
}
