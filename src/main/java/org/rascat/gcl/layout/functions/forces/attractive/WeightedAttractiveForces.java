package org.rascat.gcl.layout.functions.forces.attractive;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.rascat.gcl.layout.api.AttractiveForces;
import org.rascat.gcl.layout.functions.prepare.TransferGraphIds;
import org.rascat.gcl.layout.functions.prepare.TransferPosition;
import org.rascat.gcl.layout.model.Force;

import static org.rascat.gcl.layout.model.VertexType.HEAD;
import static org.rascat.gcl.layout.model.VertexType.TAIL;

public class WeightedAttractiveForces implements AttractiveForces {

  private double sameGraphFactor;
  private double differentGraphFactor = 1D;

  public WeightedAttractiveForces(double sameGraphFactor, double differentGraphFactor) {
    this.sameGraphFactor = sameGraphFactor;
    this.differentGraphFactor = differentGraphFactor;
  }

  public WeightedAttractiveForces(double sameGraphFactor) {
    this.sameGraphFactor = sameGraphFactor;
  }

  @Override
  public DataSet<Force> compute(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges, double k) {
    DataSet<EPGMEdge> positionedEdges = edges
      .join(vertices).where("sourceId").equalTo("id").with(new TransferPosition(TAIL))
      .join(vertices).where("targetId").equalTo("id").with(new TransferPosition(HEAD));

    positionedEdges = positionedEdges
      .join(vertices).where("sourceId").equalTo("id").with(new TransferGraphIds(TAIL))
      .join(vertices).where("targetId").equalTo("id").with(new TransferGraphIds(HEAD));

    return positionedEdges.flatMap(new WeightedAttractionFunction(k, sameGraphFactor, differentGraphFactor));
  }

  @Override
  public String toString() {
    return "WeightedAttractiveForces{" +
      "sameGraphFactor=" + sameGraphFactor +
      ", differentGraphFactor=" + differentGraphFactor +
      '}';
  }
}
