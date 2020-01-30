package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.gradoop.flink.model.api.operators.UnaryBaseGraphCollectionToBaseGraphCollectionOperator;
import org.gradoop.flink.model.impl.epgm.GraphCollection;

import java.util.ArrayList;
import java.util.List;

public class SetGraphIdsProperty implements MapFunction<EPGMVertex, EPGMVertex>,
  UnaryBaseGraphCollectionToBaseGraphCollectionOperator<GraphCollection> {

  public static final String keyGraphIds = "graphids";

  @Override
  public EPGMVertex map(EPGMVertex vertex) throws Exception {
    List<PropertyValue> ids = new ArrayList<>();
    for (GradoopId id: vertex.getGraphIds()){
      ids.add(PropertyValue.create(id));
    }
    vertex.setProperty(keyGraphIds, ids);

    return vertex;
  }

  @Override
  public GraphCollection execute(GraphCollection collection) {
    DataSet<EPGMVertex> vertices = collection.getVertices().map(this);
    return collection.getFactory().fromDataSets(collection.getGraphHeads(), vertices, collection.getEdges());
  }
}
