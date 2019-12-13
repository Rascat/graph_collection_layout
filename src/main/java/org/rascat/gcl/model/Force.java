package org.rascat.gcl.model;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.id.GradoopId;

public class Force extends Tuple2<GradoopId, Vector2D> {

  public Force(GradoopId value0, Vector2D value1) {
    super(value0, value1);
  }

  public Force() {
    super();
  }

  public GradoopId getId() {
    return this.f0;
  }

  public Vector2D getVector() {
    return this.f1;
  }

  @Override
  public String toString() {
    return String.format("[%s]: (%f, %f)", getId(), getVector().getX(), getVector().getY());
  }
}
