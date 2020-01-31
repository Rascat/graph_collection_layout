package org.rascat.gcl.layout.functions.forces.attractive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.rascat.gcl.layout.model.Force;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.rascat.gcl.layout.model.VertexType.HEAD;
import static org.rascat.gcl.layout.model.VertexType.TAIL;
import static org.testng.Assert.*;

public class StandardAttractionFunctionTest {

  private StandardAttractionFunction function;
  private final double delta = 0.0000000001;

  @BeforeClass
  public void setUp() {
    this.function = new StandardAttractionFunction(7.5D);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMissingCoordFailsWithException() {
    EPGMEdge edge = new EPGMEdge();

    this.function.map(edge);
  }

  @Test
  public void testMap() {
    EPGMEdge edge = new EPGMEdge();
    GradoopId sourceId = GradoopId.fromString("912345678910111213141516");
    GradoopId targetId = GradoopId.fromString("1AB363914FD1325CC43790AB");
    edge.setSourceId(sourceId);
    edge.setTargetId(targetId);
    edge.setProperty(TAIL.getKeyX(), 10D);
    edge.setProperty(TAIL.getKeyY(), 10D);
    edge.setProperty(HEAD.getKeyX(), 3D);
    edge.setProperty(HEAD.getKeyY(), 3D);

    Force expectedForce = new Force();
    expectedForce.setId(sourceId);
    expectedForce.setVector(new Vector2D(-9.2395286075D,-9.2395286075D));

    Force actualForce = function.map(edge);
    assertEquals(actualForce.getId(), expectedForce.getId());
    assertEquals(actualForce.getVector().getX(), expectedForce.getVector().getX(), delta);
    assertEquals(actualForce.getVector().getY(), expectedForce.getVector().getY(), delta);
  }

}