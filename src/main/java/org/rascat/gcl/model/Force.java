package org.rascat.gcl.model;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.id.GradoopId;

public class Force extends Tuple2<GradoopId, Vector2D> {

  public final static String ID_POSITION = "f0";
  public final static String VECTOR_POSITION = "f1";

  public Force(GradoopId id, Vector2D vector) {
    super(id, vector);
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

  public void setId(GradoopId id) {
    this.f0 = id;
  }

  public void setVector(Vector2D vector) {
    this.f1 = vector;
  }

  @Override
  public String toString() {
    return String.format("[%s]: (%f, %f)", getId(), getVector().getX(), getVector().getY());
  }
}
