package org.rascat.gcl.layout;

import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;

import java.util.Arrays;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;

public abstract class TestBase {

  protected final GradoopId vId = GradoopId.fromString("912345678910111213141516");
  protected final GradoopId uId = GradoopId.fromString("1AB363914FD1325CC43790AB");
  protected final GradoopId gId1 = GradoopId.fromString("111113914FD1325CC43790AB");
  protected final GradoopId gId2 = GradoopId.fromString("222223914FD1325CC43790AB");
  protected final GradoopId gId3 = GradoopId.fromString("333333914FD1325CC43790AB");
  protected final GradoopId gId4 = GradoopId.fromString("444443914FD1325CC43790AB");
  protected final GradoopIdSet graphIdSetA = GradoopIdSet.fromExisting(Arrays.asList(gId1, gId2));
  protected final GradoopIdSet graphIdSetB = GradoopIdSet.fromExisting(Arrays.asList(gId3, gId4));
  protected final GradoopIdSet graphIdSetAB = GradoopIdSet.fromExisting(Arrays.asList(gId2, gId3));


  protected  EPGMVertex createVertex(double x, double y) {
    return createVertex(vId, x, y);
  }

  protected EPGMVertex createVertex(GradoopId id, double x, double y) {
    return createVertex(id, graphIdSetA, x, y);
  }

  protected EPGMVertex createVertex(GradoopId id, GradoopIdSet graphIds, double x, double y) {
    EPGMVertex vertex = new EPGMVertex();
    vertex.setId(id);
    vertex.setGraphIds(graphIds);
    vertex.setProperty(KEY_X_COORD, x);
    vertex.setProperty(KEY_Y_COORD, y);

    return vertex;
  }
}
