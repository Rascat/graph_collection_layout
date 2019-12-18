package org.rascat.gcl.functions;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.Property;

import java.util.concurrent.ThreadLocalRandom;

public class RandomPlacement implements MapFunction<EPGMVertex, EPGMVertex> {

    private double limitX;
    private double limitY;

    public RandomPlacement(double limitX, double limitY) {
        this.limitX = limitX;
        this.limitY = limitY;
    }

    @Override
    public EPGMVertex map(EPGMVertex vertex) throws Exception {
        double x = ThreadLocalRandom.current().nextDouble(this.limitX);
        double y = ThreadLocalRandom.current().nextDouble(this.limitY);
        vertex.setProperty(Property.create("X", x));
        vertex.setProperty(Property.create("Y", y));
        return vertex;
    }
}
