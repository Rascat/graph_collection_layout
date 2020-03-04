package org.rascat.gcl.layout.functions.prepare;

import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMEdgeFactory;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;
import org.rascat.gcl.layout.model.VertexType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;
import static org.testng.Assert.*;

public class TransferPositionTest {

  private TransferPosition transfer;
  private EPGMVertexFactory vertexFactory;
  private EPGMEdgeFactory edgeFactory;
  private EPGMEdge edge;

  private static final GradoopId vertexIdA = GradoopId.fromString("912345678910111213141516");
  private static final GradoopId vertexIdB = GradoopId.fromString("1AB363914FD1325CC43790AB");

  @BeforeTest
  public void setUp() {
    this.vertexFactory = new EPGMVertexFactory();
    this.edgeFactory = new EPGMEdgeFactory();
  }

  @BeforeMethod
  public void setUpEdge() {
    this.edge = edgeFactory.createEdge(vertexIdA, vertexIdB);
  }

  @Test
  public void testJoinWithVertexTypeTail() {
    VertexType type = VertexType.TAIL;
    this.transfer = new TransferPosition(type);

    double x = 2D;
    double y = 3D;
    EPGMVertex tailVertex = createVertex(x, y);

    EPGMEdge actualEdge = transfer.join(edge, tailVertex);

    assertEquals(actualEdge.getPropertyValue(type.getKeyX()).getDouble(), x);
    assertEquals(actualEdge.getPropertyValue(type.getKeyY()).getDouble(), y);
  }

  @Test
  public void testJoinWithVertexTypeHead() {
    VertexType type = VertexType.HEAD;
    this.transfer = new TransferPosition(type);

    double x = 20D;
    double y = 13D;
    EPGMVertex headVertex = createVertex(x, y);

    EPGMEdge actualEdge = transfer.join(edge, headVertex);

    assertEquals(actualEdge.getPropertyValue(type.getKeyX()).getDouble(), x);
    assertEquals(actualEdge.getPropertyValue(type.getKeyY()).getDouble(), y);
  }

  private EPGMVertex createVertex(double x, double y) {
    EPGMVertex vertex = vertexFactory.createVertex();
    vertex.setProperty(KEY_X_COORD, x);
    vertex.setProperty(KEY_Y_COORD, y);

    return vertex;
  }
}