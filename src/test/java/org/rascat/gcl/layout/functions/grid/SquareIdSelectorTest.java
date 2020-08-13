package org.rascat.gcl.layout.functions.grid;

import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.TestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <pre>
 *   0        10      20      30
 *    __________________________  X&rarr;
 *   |        |        |        |
 *   |   0    | 65536  | 131072 |
 * 10 ________|________|________|
 *   |        |        |        |
 *   |   1    | 65537  | 131073 |
 * 20 ________|________|________|
 *   |        |        |        |
 *   |   2    | 65538  | 131074 |
 * 30 ________|________|________
 *   Y
 *   &darr;
 * </pre>
 */
public class SquareIdSelectorTest extends TestBase {

  private final SquareIdSelector selfSelector = new SquareIdSelector(NeighborType.SELF);
  private final SquareIdSelector leftSelector = new SquareIdSelector(NeighborType.LEFT);
  private final SquareIdSelector rightSelector = new SquareIdSelector(NeighborType.RIGHT);
  private final SquareIdSelector upSelector = new SquareIdSelector(NeighborType.UP);
  private final SquareIdSelector downSelector = new SquareIdSelector(NeighborType.DOWN);
  private final SquareIdSelector upleftSelector = new SquareIdSelector(NeighborType.UPLEFT);
  private final SquareIdSelector uprightSelector = new SquareIdSelector(NeighborType.UPRIGHT);
  private final SquareIdSelector downleftSelector = new SquareIdSelector(NeighborType.DOWNLEFT);
  private final SquareIdSelector downRightSelector = new SquareIdSelector(NeighborType.DOWNRIGHT);

  @Test(dataProvider = "testSelfSelectorProvider")
  public void testSelfSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) selfSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testSelfSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(0), 0},
      {createVertexWithGridId(1), 1},
      {createVertexWithGridId(2), 2},
      {createVertexWithGridId(65536), 65536},
      {createVertexWithGridId(65537), 65537},
      {createVertexWithGridId(65538), 65538},
      {createVertexWithGridId(131072), 131072},
      {createVertexWithGridId(131073), 131073},
      {createVertexWithGridId(131074), 131074},
    };
  }

  @Test(dataProvider = "testLeftSelectorProvider")
  public void testLeftSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) leftSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testLeftSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(65536), 0},
      {createVertexWithGridId(65537), 1},
      {createVertexWithGridId(65538), 2},
      {createVertexWithGridId(131072), 65536},
      {createVertexWithGridId(131073), 65537},
      {createVertexWithGridId(131074), 65538},
    };
  }

  @Test(dataProvider = "testRightSelectorProvider")
  public void testRightSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) rightSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testRightSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(0), 65536},
      {createVertexWithGridId(1), 65537},
      {createVertexWithGridId(2), 65538},
      {createVertexWithGridId(65536), 131072},
      {createVertexWithGridId(65537), 131073},
      {createVertexWithGridId(65538), 131074},
    };
  }

  @Test(dataProvider = "testUpSelectorProvider")
  public void testUpSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) upSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testUpSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(1), 0},
      {createVertexWithGridId(2), 1},
      {createVertexWithGridId(131073), 131072},
      {createVertexWithGridId(131074), 131073},
      {createVertexWithGridId(65537), 65536},
      {createVertexWithGridId(65538), 65537},
    };
  }

  @Test(dataProvider = "testDownSelectorProvider")
  public void testDownSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) downSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testDownSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(0), 1},
      {createVertexWithGridId(1), 2},
      {createVertexWithGridId(131072), 131073},
      {createVertexWithGridId(131073), 131074},
      {createVertexWithGridId(65536), 65537},
      {createVertexWithGridId(65537), 65538}
    };
  }

  @Test(dataProvider = "testUpLeftSelectorProvider")
  public void testUpLeftSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) upleftSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testUpLeftSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(65537), 0},
      {createVertexWithGridId(131074), 65537},
      {createVertexWithGridId(131073), 65536},
      {createVertexWithGridId(65538), 1}
    };
  }

  @Test(dataProvider = "testUpRightSelectorProvider")
  public void testUpRightSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) uprightSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testUpRightSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(1), 65536},
      {createVertexWithGridId(2), 65537},
      {createVertexWithGridId(65537), 131072},
      {createVertexWithGridId(65538), 131073}
    };
  }

  @Test(dataProvider = "testDownLeftSelectorProvider")
  public void testDownLeftSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) downleftSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testDownLeftSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(65536), 1},
      {createVertexWithGridId(65537), 2},
      {createVertexWithGridId(131072), 65537},
      {createVertexWithGridId(131073), 65538}
    };
  }

  @Test(dataProvider = "testDownRightSelectorProvider")
  public void testDownRightSelector(EPGMVertex actualVertex, int expectedKey) {
    assertEquals((int) downRightSelector.getKey(actualVertex), expectedKey);
  }

  @DataProvider
  private Object[][] testDownRightSelectorProvider() {
    return new Object[][] {
      {createVertexWithGridId(0), 65537},
      {createVertexWithGridId(1), 65538},
      {createVertexWithGridId(65537), 131074},
      {createVertexWithGridId(65536), 131073}
    };
  }

  private EPGMVertex createVertexWithGridId(int gridId) {
    EPGMVertex vertex = createVertex(0, 0);
    vertex.setProperty(SquareIdMapper.KEY_SQUARE_ID, gridId);

    return  vertex;
  }
}