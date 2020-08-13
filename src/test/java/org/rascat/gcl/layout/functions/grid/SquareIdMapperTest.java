package org.rascat.gcl.layout.functions.grid;

import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;
import org.rascat.gcl.layout.TestBase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;
import static org.testng.Assert.*;

public class SquareIdMapperTest extends TestBase {

  SquareIdMapper mapper = new SquareIdMapper(10);

  @Test(dataProvider = "testMapProvider")
  public void testMap(EPGMVertex actualVertex, int expectedId) {
    assertEquals(getSquareId(mapper.map(actualVertex)), expectedId);
  }

  @DataProvider
  private Object[][] testMapProvider() {
    return new Object[][] {
      {createVertex(5, 6), 0},
      {createVertex(90, 90), 589833},
      {createVertex(95, 92), 589833},
      {createVertex(33, 50), 196613},
      {createVertex(39.5, 55), 196613}
    };
  }

  private int getSquareId (EPGMVertex vertex) {
    return vertex.getPropertyValue(SquareIdMapper.KEY_SQUARE_ID).getInt();
  }
}