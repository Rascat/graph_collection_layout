package org.rascat.gcl.layout.functions.forces.attractive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.rascat.gcl.layout.functions.prepare.TransferGraphIds;
import org.rascat.gcl.layout.model.Force;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.rascat.gcl.layout.model.VertexType.*;
import static org.testng.Assert.*;

public class WeightedAttractionFunctionTest {

  private WeightedAttractionFunction function;
  private final GradoopId sourceId = GradoopId.fromString("912345678910111213141516");
  private final GradoopId targetId = GradoopId.fromString("1AB363914FD1325CC43790AB");
  private final GradoopId graphAId = GradoopId.fromString("AAAAAAAAAAAAAAAAAAAAAAAA");
  private final GradoopId graphBId = GradoopId.fromString("BBBBBBBBBBBBBBBBBBBBBBBB");
  private final double delta = 0.00000001D;

  @BeforeClass
  public void setUp() {
    this.function = new WeightedAttractionFunction(7.5, 2, 1);
  }

  @Test(dataProvider = "testMapProvider")
  public void testMap(EPGMEdge edge, Force expectedForce) {
    Force actualForce = function.map(edge);

    assertEquals(actualForce.getId(), expectedForce.getId());
    assertEquals(actualForce.getVector().getX(), expectedForce.getVector().getX(), delta);
    assertEquals(actualForce.getVector().getY(), expectedForce.getVector().getY(), delta);
  }

  @DataProvider
  private Object[][] testMapProvider() {
    GradoopIdSet ids1 = GradoopIdSet.fromExisting(graphAId);
    GradoopIdSet ids2 = GradoopIdSet.fromExisting(graphBId);
    EPGMEdge edge1 = createEdge(10 ,10, 3, 3, ids1, ids2);
    EPGMEdge edge2 = createEdge(10, 10, 3, 3, ids1, ids1);
    EPGMEdge edge3 = createEdge(3, 3, 3, 3, ids1, ids2);

    Force force1 = new Force(sourceId, new Vector2D(-18.47905721500844, -18.47905721500844));
    Force force2 = new Force (sourceId, new Vector2D(-9.2395286075,-9.2395286075));
    Force force3 = new Force(sourceId, Vector2D.ZERO);

    return new Object[][] {
        {edge1, force2},
        {edge2, force1},
        {edge3, force3}
    };
  }

  private EPGMEdge createEdge(double sourceX, double sourceY, double targetX, double targetY,
                              GradoopIdSet sourceIds, GradoopIdSet targetIds) {
    EPGMEdge edge = new EPGMEdge();
    edge.setSourceId(sourceId);
    edge.setTargetId(targetId);
    edge.setProperty(TAIL.getKeyGraphIds(), TransferGraphIds.translateToPropertyValueList(sourceIds));
    edge.setProperty(HEAD.getKeyGraphIds(), TransferGraphIds.translateToPropertyValueList(targetIds));
    edge.setProperty(TAIL.getKeyX(), sourceX);
    edge.setProperty(TAIL.getKeyY(), sourceY);
    edge.setProperty(HEAD.getKeyX(), targetX);
    edge.setProperty(HEAD.getKeyY(), targetY);

    return edge;
  }
}