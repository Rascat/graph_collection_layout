package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.functions.KeySelector;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.RepulsiveForces;
import org.rascat.gcl.layout.functions.grid.NeighborType;
import org.rascat.gcl.layout.functions.grid.SquareIdMapper;
import org.rascat.gcl.layout.functions.grid.SquareIdSelector;
import org.rascat.gcl.layout.model.Force;

public class GridRepulsiveForces implements RepulsiveForces {

  private RepulsionFunction repulsionFunction;

  public GridRepulsiveForces(RepulsionFunction repulsionFunction){
    this.repulsionFunction = repulsionFunction;
  }

  @Override
  public DataSet<Force> compute(DataSet<EPGMVertex> vertices, double k) {
    repulsionFunction.setK(k);

    vertices = vertices.map(new SquareIdMapper((int) k * 2));

    KeySelector<EPGMVertex, Integer> selfSelector = new SquareIdSelector(NeighborType.SELF);
    KeySelector<EPGMVertex, Integer> upSelector = new SquareIdSelector(NeighborType.UP);
    KeySelector<EPGMVertex, Integer> upRightSelector = new SquareIdSelector(NeighborType.UPRIGHT);
    KeySelector<EPGMVertex, Integer> upLeftSelector = new SquareIdSelector(NeighborType.UPLEFT);
    KeySelector<EPGMVertex, Integer> leftSelector = new SquareIdSelector(NeighborType.LEFT);

    DataSet<Force> directNeighbors = vertices.join(vertices)
      .where(selfSelector).equalTo(selfSelector)
      .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) repulsionFunction);

    DataSet<Force> upNeighbors = vertices.join(vertices)
      .where(upSelector).equalTo(selfSelector)
      .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) repulsionFunction);

    DataSet<Force> upRightNeighbors = vertices.join(vertices)
      .where(upRightSelector).equalTo(selfSelector)
      .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) repulsionFunction);

    DataSet<Force> upLeftNeighbors = vertices.join(vertices)
      .where(upLeftSelector).equalTo(selfSelector)
      .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) repulsionFunction);

    DataSet<Force> leftNeighbors = vertices.join(vertices)
      .where(leftSelector).equalTo(selfSelector)
      .with((FlatJoinFunction<EPGMVertex, EPGMVertex, Force>) repulsionFunction);

    return directNeighbors
      .union(upNeighbors)
      .union(upRightNeighbors)
      .union(upLeftNeighbors)
      .union(leftNeighbors);
  }
}
