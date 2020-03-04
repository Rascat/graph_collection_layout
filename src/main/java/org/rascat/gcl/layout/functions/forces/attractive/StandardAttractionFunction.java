package org.rascat.gcl.layout.functions.forces.attractive;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;

import static org.rascat.gcl.layout.model.VertexType.*;

public class StandardAttractionFunction implements MapFunction<EPGMEdge, Force>, FlatMapFunction<EPGMEdge, Force> {

  private double k;

  public StandardAttractionFunction(double k) {
    if (k <= 0) {
      throw new IllegalArgumentException("K must be greater than zero: " + k);
    }

    this.k = k;
  }

  @Override
  public Force map(EPGMEdge edge) {
    Vector2D displacement = calculateDisplacement(edge);

    return new Force(edge.getSourceId(), displacement);
  }

  @Override
  public void flatMap(EPGMEdge edge, Collector<Force> out) {
    Vector2D displacement = calculateDisplacement(edge);

    out.collect(new Force(edge.getSourceId(), displacement));
    out.collect(new Force(edge.getTargetId(), displacement.scalarMultiply(-1)));
  }

  private Vector2D calculateDisplacement(EPGMEdge edge) {
    checkEdge(edge);

    Point vPosition = Point.fromEPGMElement(edge, TAIL.getKeyX(), TAIL.getKeyY());
    Point uPosition = Point.fromEPGMElement(edge, HEAD.getKeyX(), HEAD.getKeyY());

    Vector2D delta = vPosition.subtract(uPosition);

    Vector2D result;
    try {
      result = delta.normalize().scalarMultiply(attraction(delta.getNorm()) * -1);
    } catch (MathArithmeticException e) {
      // we probably tried to normalize a zero vector
      result = Vector2D.ZERO;
    }

    return result;
  }

  public double attraction(double distance) {
    return (distance * distance) / this.k;
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
