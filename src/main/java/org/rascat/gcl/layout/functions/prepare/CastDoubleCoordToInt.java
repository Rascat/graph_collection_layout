package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.api.operators.UnaryBaseGraphCollectionToBaseGraphCollectionOperator;
import org.gradoop.flink.model.impl.epgm.GraphCollection;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_X_COORD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.KEY_Y_COORD;

public class CastDoubleCoordToInt implements MapFunction<EPGMVertex, EPGMVertex>,
  UnaryBaseGraphCollectionToBaseGraphCollectionOperator<GraphCollection> {

  @Override
  public EPGMVertex map(EPGMVertex vertex) {
    if (vertex.getPropertyValue(KEY_X_COORD).isDouble()) {
      vertex.setProperty(KEY_X_COORD, (int) vertex.getPropertyValue(KEY_X_COORD).getDouble());
    }

    if (vertex.getPropertyValue(KEY_Y_COORD).isDouble()) {
      vertex.setProperty(KEY_Y_COORD, (int) vertex.getPropertyValue(KEY_Y_COORD).getDouble());
    }

    return vertex;
  }

  @Override
  public GraphCollection execute(GraphCollection collection) {
    DataSet<EPGMVertex> vertices = collection.getVertices().map(this);
    return collection.getFactory().fromDataSets(collection.getGraphHeads(), vertices, collection.getEdges());
  }
}
