package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.JoinFunction;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.rascat.gcl.layout.model.VertexType;

import java.util.ArrayList;
import java.util.List;

public class TransferGraphIds implements JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge> {

    private VertexType type;

    public TransferGraphIds(VertexType type) {
        this.type = type;
    }

    @Override
    public EPGMEdge join(EPGMEdge edge, EPGMVertex vertex) {
        List<PropertyValue> graphIds = translateToPropertyValueList(vertex.getGraphIds());
        edge.setProperty(type.getKeyGraphIds(), PropertyValue.create(graphIds));
        return edge;
    }

    public static List<PropertyValue> translateToPropertyValueList(GradoopIdSet set) {
        List<PropertyValue> ids = new ArrayList<>();

        for (GradoopId id: set) {
            ids.add(PropertyValue.create(id));
        }

        return ids;
    }
}
