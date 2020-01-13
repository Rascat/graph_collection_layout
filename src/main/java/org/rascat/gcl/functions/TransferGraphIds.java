package org.rascat.gcl.functions;

import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.PropertyValue;

import java.util.ArrayList;
import java.util.List;

public class TransferGraphIds implements JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge> {

    private VertexType type;

    public TransferGraphIds(VertexType type) {
        this.type = type;
    }

    @Override
    public EPGMEdge join(EPGMEdge edge, EPGMVertex vertex) {
        GradoopIdSet idSet = vertex.getGraphIds();
        List<PropertyValue> ids = new ArrayList<>();

        for (GradoopId id: idSet) {
            ids.add(PropertyValue.create(id));
        }

        edge.setProperty(type.getKeyGraphIds(), PropertyValue.create(ids));
        return edge;
    }
}
