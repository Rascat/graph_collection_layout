package org.rascat.gcl.functions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.rascat.gcl.functions.forces.AttractionFunction;
import org.rascat.gcl.model.Force;

import static org.rascat.gcl.functions.TransferPosition.Position.*;

public class ComputeAttractingForces implements MapFunction<EPGMEdge, Force> {

  private double k;
  private AttractionFunction function;

  public ComputeAttractingForces(double k, AttractionFunction function) {
    this.k = k;
    this.function = function;
  }

  @Override
  public Force map(EPGMEdge edge) {
    checkEdge(edge);

    Vector2D vPos = new Vector2D(edge.getPropertyValue(SOURCE.getKeyX()).getDouble(), edge.getPropertyValue(SOURCE.getKeyY()).getDouble());
    Vector2D uPos = new Vector2D(edge.getPropertyValue(TARGET.getKeyX()).getDouble(), edge.getPropertyValue(TARGET.getKeyY()).getDouble());
    Vector2D delta = vPos.subtract(uPos);

    Vector2D result = delta.normalize().scalarMultiply(function.attraction(delta.getNorm(), k));

    return new Force(edge.getSourceId(), result);
  }

  private static void checkEdge(EPGMEdge edge) {
    if (!edge.hasProperty(SOURCE.getKeyX()) || !edge.hasProperty(SOURCE.getKeyY())) {
      throw new IllegalArgumentException("Provided edge did not contain position of source vertex.");
    }
    if (!edge.hasProperty(TARGET.getKeyX()) || !edge.hasProperty(TARGET.getKeyY())) {
      throw new IllegalArgumentException("Provided edge did  not contain position of target vertex.");
    }
  }
}
