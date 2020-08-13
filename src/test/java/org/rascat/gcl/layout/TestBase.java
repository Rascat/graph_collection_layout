package org.rascat.gcl.layout;

import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;
import org.testng.annotations.BeforeTest;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;

public abstract class TestBase {
  protected EPGMVertexFactory vertexFactory;

  @BeforeTest
  public void setUp () {
    this.vertexFactory = new EPGMVertexFactory();
  }

  protected  EPGMVertex createVertex(double x, double y) {
    EPGMVertex vertex = vertexFactory.createVertex();
    vertex.setProperty(KEY_X_COORD, x);
    vertex.setProperty(KEY_Y_COORD, y);

    return vertex;
  }
}
