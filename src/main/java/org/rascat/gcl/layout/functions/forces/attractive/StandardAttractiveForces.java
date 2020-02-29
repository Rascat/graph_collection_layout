package org.rascat.gcl.layout.functions.forces.attractive;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.AttractiveForces;
import org.rascat.gcl.layout.functions.prepare.TransferPosition;
import org.rascat.gcl.layout.model.Force;

import static org.rascat.gcl.layout.model.VertexType.HEAD;
import static org.rascat.gcl.layout.model.VertexType.TAIL;

public class StandardAttractiveForces implements AttractiveForces {

  @Override
  public DataSet<Force> compute(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges, double k) {
    // first we need to add the position of the source/target vertex to the respective edge
    DataSet<EPGMEdge> positionedEdges = edges
      .join(vertices).where("sourceId").equalTo("id").with(new TransferPosition(TAIL))
      .join(vertices).where("targetId").equalTo("id").with(new TransferPosition(HEAD));

    // return positionedEdges.map(new StandardAttractionFunction(k));
    return positionedEdges.flatMap(new StandardAttractionFunction(k));
  }
}
