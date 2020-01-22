package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.Property;

import java.util.concurrent.ThreadLocalRandom;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class RandomPlacement implements MapFunction<EPGMVertex, EPGMVertex> {

    private int limitX;
    private int limitY;

    public RandomPlacement(int limitX, int limitY) {
        this.limitX = limitX;
        this.limitY = limitY;
    }

    @Override
    public EPGMVertex map(EPGMVertex vertex) {
        double x = ThreadLocalRandom.current().nextDouble(this.limitX);
        double y = ThreadLocalRandom.current().nextDouble(this.limitY);
        vertex.setProperty(Property.create(KEY_X_COORD, x));
        vertex.setProperty(Property.create(KEY_Y_COORD, y));
        return vertex;
    }
}
