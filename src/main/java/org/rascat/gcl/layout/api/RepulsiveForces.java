package org.rascat.gcl.layout.api;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;

public interface RepulsiveForces {
  DataSet<Force> compute(DataSet<EPGMVertex> vertices);
}
