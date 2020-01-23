package org.rascat.gcl.layout.functions.forces.repulsive;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.RepulsiveForces;
import org.rascat.gcl.layout.model.Force;

public class NaiveRepulsiveForces implements RepulsiveForces {

  private RepulsionFunction repulsionFunction;

  public NaiveRepulsiveForces(RepulsionFunction repulsionFunction) {
    this.repulsionFunction = repulsionFunction;
  }

  @Override
  public DataSet<Force> compute(DataSet<EPGMVertex> vertices, double k) {
    repulsionFunction.setK(k);
    return vertices.cross(vertices).with(repulsionFunction);
  }
}
