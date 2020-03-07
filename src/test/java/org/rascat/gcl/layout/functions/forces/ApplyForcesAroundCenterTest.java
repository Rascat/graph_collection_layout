package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.functions.cooling.ExponentialSimulatedAnnealing;
import org.rascat.gcl.layout.functions.prepare.TransferCenterPosition;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;
import static org.rascat.gcl.layout.functions.prepare.TransferCenterPosition.*;
import static org.testng.Assert.*;

public class ApplyForcesAroundCenterTest {

  CoolingSchedule scheduleMock;
  EPGMVertexFactory vertexFactory;

  @BeforeTest
  public void setUp() {
    this.scheduleMock = mock(ExponentialSimulatedAnnealing.class);
    when(this.scheduleMock.computeTemperature(anyInt())).thenReturn(1D);
    this.vertexFactory = new EPGMVertexFactory();
  }

  @Test
  public void testJoin() {
    ApplyForcesAroundCenter function = new ApplyForcesAroundCenter(10, 10, 3D, this.scheduleMock);

    EPGMVertex vertex = createVertexWithCenter(2, 3, 4, 4);

    Force force = new Force(vertex.getId(), new Vector2D(2, 0));

    EPGMVertex actualVertex = function.join(vertex, force);
    Point actualPoint = Point.fromEPGMElement(actualVertex);

    assertEquals(actualPoint, new Point(3,3));
  }

  @Test
  public void testJoinOffLayoutSpace() {
    ApplyForcesAroundCenter function = new ApplyForcesAroundCenter(10, 10, 3D, this.scheduleMock);

    EPGMVertex vertex = createVertexWithCenter(0, 1, 0 , 0);

    Force force = new Force(vertex.getId(), new Vector2D(-2, 0));

    EPGMVertex actualVertex = function.join(vertex, force);
    Point actualPoint = Point.fromEPGMElement(actualVertex);

    assertEquals(actualPoint, new Point(0,1));
  }

  @Test
  public void testJoinOffCenterArea() {
    ApplyForcesAroundCenter function = new ApplyForcesAroundCenter(10, 10, 3D, this.scheduleMock);

    EPGMVertex vertex = createVertexWithCenter(2, 4, 5 , 5);

    Force force = new Force(vertex.getId(), new Vector2D(-2, 0));

    EPGMVertex actualVertex = function.join(vertex, force);
    Point actualPoint = Point.fromEPGMElement(actualVertex);

    assertEquals(actualPoint, new Point(2,4));
  }

  private EPGMVertex createVertexWithCenter(double x, double y, double cx, double cy) {
    EPGMVertex vertex = vertexFactory.createVertex();
    vertex.setProperty(KEY_CENTER_X_COORD, cx);
    vertex.setProperty(KEY_CENTER_Y_COORD, cy);
    vertex.setProperty(KEY_X_COORD, x);
    vertex.setProperty(KEY_Y_COORD, y);

    return vertex;
  }
}