package org.rascat.gcl.layout.functions.forces;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.rascat.gcl.layout.api.AttractionFunction;
import org.rascat.gcl.layout.model.Force;

import static org.rascat.gcl.layout.model.VertexType.*;

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

    Vector2D vPos = new Vector2D(edge.getPropertyValue(TAIL.getKeyX()).getDouble(), edge.getPropertyValue(TAIL.getKeyY()).getDouble());
    Vector2D uPos = new Vector2D(edge.getPropertyValue(HEAD.getKeyX()).getDouble(), edge.getPropertyValue(HEAD.getKeyY()).getDouble());
    Vector2D delta = vPos.subtract(uPos);

    Vector2D result;
    try {
      result = delta.normalize().scalarMultiply(function.attraction(delta.getNorm(), k) * -1);
    } catch (MathArithmeticException e) {
      // we probably tried to normalize a zero vector
      result = new Vector2D(0, 0);
    }

    return new Force(edge.getSourceId(), result);
  }

  private static void checkEdge(EPGMEdge edge) {
    if (!edge.hasProperty(TAIL.getKeyX()) || !edge.hasProperty(TAIL.getKeyY())) {
      throw new IllegalArgumentException("Provided edge did not contain position of source vertex.");
    }
    if (!edge.hasProperty(HEAD.getKeyX()) || !edge.hasProperty(HEAD.getKeyY())) {
      throw new IllegalArgumentException("Provided edge did  not contain position of target vertex.");
    }
  }
}
