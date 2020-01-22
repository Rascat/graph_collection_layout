package org.rascat.gcl.layout.api;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.model.Force;

public interface AttractingForces {
  DataSet<Force> compute(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges);
}
