package org.rascat.gcl.layout.functions.forces.attractive;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.rascat.gcl.layout.model.Force;
import org.rascat.gcl.layout.model.Point;

import java.util.List;

import static org.rascat.gcl.layout.model.VertexType.*;

public class WeightedAttractionFunction implements MapFunction<EPGMEdge, Force>, FlatMapFunction<EPGMEdge, Force> {

    private double k;
    private double sameGraphFactor;
    private double differentGraphFactor;

    public WeightedAttractionFunction(double k, double sameGraphFactor, double differentGraphFactor) {
        if (k <= 0) {
            throw new IllegalArgumentException("K must be greater than zero: " + k);
        }

        this.k = k;
        this.sameGraphFactor = sameGraphFactor;
        this.differentGraphFactor = differentGraphFactor;
    }

    @Override
    public Force map(EPGMEdge edge) {
        checkEdge(edge);

        Point vPos = Point.fromEPGMElement(edge, TAIL.getKeyX(), TAIL.getKeyY());
        Point uPos = Point.fromEPGMElement(edge, HEAD.getKeyX(), HEAD.getKeyY());

        GradoopIdSet tailIds = unwrapGradoopIdSet(edge.getPropertyValue(TAIL.getKeyGraphIds()).getList());
        GradoopIdSet headIds = unwrapGradoopIdSet(edge.getPropertyValue(HEAD.getKeyGraphIds()).getList());

        boolean sameGraph = tailIds.containsAny(headIds);
        Vector2D delta = vPos.subtract(uPos);

        Vector2D result;
        try {
            result = delta.normalize().scalarMultiply(weightedAttraction(delta.getNorm(), sameGraph) * -1);
        } catch (MathArithmeticException e) {
            result = new Vector2D(0,0);
        }
        return new Force(edge.getSourceId(), result);
    }

    @Override
    public void flatMap(EPGMEdge edge, Collector<Force> out) {
        checkEdge(edge);

        Point vPos = Point.fromEPGMElement(edge, TAIL.getKeyX(), TAIL.getKeyY());
        Point uPos = Point.fromEPGMElement(edge, HEAD.getKeyX(), HEAD.getKeyY());

        GradoopIdSet tailIds = unwrapGradoopIdSet(edge.getPropertyValue(TAIL.getKeyGraphIds()).getList());
        GradoopIdSet headIds = unwrapGradoopIdSet(edge.getPropertyValue(HEAD.getKeyGraphIds()).getList());

        boolean sameGraph = tailIds.containsAny(headIds);
        Vector2D delta = vPos.subtract(uPos);

        Vector2D result;
        try {
            result = delta.normalize().scalarMultiply(weightedAttraction(delta.getNorm(), sameGraph) * -1);
        } catch (MathArithmeticException e) {
            result = new Vector2D(0,0);
        }
        out.collect(new Force(edge.getSourceId(), result));
        out.collect(new Force(edge.getTargetId(), result.scalarMultiply(-1)));
    }

    private double weightedAttraction(double distance, boolean sameGraph) {
        double factor = sameGraph ? sameGraphFactor : differentGraphFactor;
        return ((distance*distance) / this.k) * factor;
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
