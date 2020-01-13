package org.rascat.gcl.functions;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.rascat.gcl.functions.forces.WeightedAttractingForce;
import org.rascat.gcl.model.Force;

import java.util.List;

import static org.rascat.gcl.functions.VertexType.*;

public class ComputeWeightedAttractingForces implements MapFunction<EPGMEdge, Force> {

    private double k;
    private WeightedAttractingForce function;

    public ComputeWeightedAttractingForces(double k, WeightedAttractingForce function) {
        this.k = k;
        this.function = function;
    }

    @Override
    public Force map(EPGMEdge edge) throws Exception {
        checkEdge(edge);

        Vector2D vPos = new Vector2D(edge.getPropertyValue(TAIL.getKeyX()).getDouble(), edge.getPropertyValue(TAIL.getKeyY()).getDouble());
        Vector2D uPos = new Vector2D(edge.getPropertyValue(HEAD.getKeyX()).getDouble(), edge.getPropertyValue(HEAD.getKeyY()).getDouble());

        GradoopIdSet tailIds = unwrapGradoopIdSet(edge.getPropertyValue(TAIL.getKeyGraphIds()).getList());
        GradoopIdSet headIds = unwrapGradoopIdSet(edge.getPropertyValue(HEAD.getKeyGraphIds()).getList());

        boolean sameGraph = tailIds.containsAny(headIds);
        Vector2D delta = vPos.subtract(uPos);

        Vector2D result;
        try {
            result = delta.normalize().scalarMultiply(function.weightedAttraction(delta.getNorm(), k, sameGraph) * -1);
        } catch (MathArithmeticException e) {
            result = new Vector2D(0,0);
        }
        return new Force(edge.getSourceId(), result);
    }

    private GradoopIdSet unwrapGradoopIdSet(List<PropertyValue> wrappedGradoopIdSet) {
        GradoopIdSet idSet = new GradoopIdSet();
        wrappedGradoopIdSet.forEach(x -> idSet.add(x.getGradoopId()));
        return idSet;
    }

    private void checkEdge(EPGMEdge edge) {
        if (!edge.hasProperty(TAIL.getKeyX()) || !edge.hasProperty(TAIL.getKeyY())) {
            throw new IllegalArgumentException("Provided edge did not contain position of source vertex.");
        }
        if (!edge.hasProperty(HEAD.getKeyX()) || !edge.hasProperty(HEAD.getKeyY())) {
            throw new IllegalArgumentException("Provided edge did  not contain position of target vertex.");
        }
        if (!edge.hasProperty(TAIL.getKeyGraphIds()) || !edge.hasProperty(HEAD.getKeyGraphIds())) {
            throw new IllegalArgumentException("Provided edge " + edge + " did not contain the ids of the parent graphs of its vertices");
        }
    }
}