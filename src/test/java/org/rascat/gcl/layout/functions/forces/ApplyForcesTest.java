package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;
import org.rascat.gcl.layout.api.CoolingSchedule;
import org.rascat.gcl.layout.functions.cooling.ExponentialSimulatedAnnealing;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

public class ApplyForcesTest {

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
    ApplyForces function = new ApplyForces(10, 10, this.scheduleMock);

    EPGMVertex vertex = createVertex(2, 3);

    Force force = new Force(vertex.getId(), new Vector2D(2, 0));

    EPGMVertex actualVertex = function.join(vertex, force);
    Point actualPoint = Point.fromEPGMElement(actualVertex);

    assertEquals(actualPoint, new Point(3,3));
  }

  @Test
  public void testJoinOffLayoutSpace() {
    ApplyForces function = new ApplyForces(10, 10, this.scheduleMock);

    EPGMVertex vertex = createVertex(0, 1);

    Force force = new Force(vertex.getId(), new Vector2D(-2, 0));

    EPGMVertex actualVertex = function.join(vertex, force);
    Point actualPoint = Point.fromEPGMElement(actualVertex);

    assertEquals(actualPoint, new Point(0,1));
  }

  private EPGMVertex createVertex(double x, double y) {
    EPGMVertex vertex = vertexFactory.createVertex();
    vertex.setProperty(KEY_X_COORD, x);
    vertex.setProperty(KEY_Y_COORD, y);

    return vertex;
  }
}