package org.rascat.gcl.functions;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.Property;

import java.util.concurrent.ThreadLocalRandom;

public class RandomPlacement implements MapFunction<EPGMVertex, EPGMVertex> {

    private Integer limitX;
    private Integer limitY;

    public RandomPlacement(Integer limitX, Integer limitY) {
        this.limitX = limitX;
        this.limitY = limitY;
    }

    @Override
    public EPGMVertex map(EPGMVertex vertex) throws Exception {
        Integer x = ThreadLocalRandom.current().nextInt(this.limitX);
        Integer y = ThreadLocalRandom.current().nextInt(this.limitY);
        vertex.setProperty(Property.create("X", x));
        vertex.setProperty(Property.create("Y", y));
        return vertex;
    }
}
