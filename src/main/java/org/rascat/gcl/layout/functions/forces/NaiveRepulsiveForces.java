package org.rascat.gcl.layout.functions.forces;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.RepulsiveForces;
import org.rascat.gcl.layout.model.Force;

public class NaiveRepulsiveForces implements RepulsiveForces {

  @Override
  public DataSet<Force> compute(DataSet<EPGMVertex> vertices, double k) {
    return vertices.cross(vertices).with(new ComputeRepulsiveForces(k, new StandardRepulsingForceFunction()));
  }
}
