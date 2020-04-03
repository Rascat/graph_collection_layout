package org.rascat.gcl.layout.functions.prepare;

import org.apache.flink.api.common.functions.MapFunction;
import org.gradoop.common.model.api.entities.Element;
import org.gradoop.common.model.impl.properties.Property;

import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class RandomPlacement<T extends Element> implements MapFunction<T, T> {

    private int lowerBoundX = 0;
    private int lowerBoundY = 0;
    private int upperBoundX;
    private int upperBoundY;

    public RandomPlacement() {}

    public RandomPlacement(int upperBoundX, int upperBoundY) {
        this.upperBoundX = upperBoundX;
        this.upperBoundY = upperBoundY;
    }

    public RandomPlacement(int lowerBoundX, int lowerBoundY, int upperBoundX, int upperBoundY) {
        this(upperBoundX, upperBoundY);
        this.lowerBoundX = lowerBoundX;
        this.lowerBoundY = lowerBoundY;
    }

    public int getLowerBoundX() {
        return lowerBoundX;
    }

    public void setLowerBoundX(int lowerBoundX) {
        this.lowerBoundX = lowerBoundX;
    }

    public int getLowerBoundY() {
        return lowerBoundY;
    }

    public void setLowerBoundY(int lowerBoundY) {
        this.lowerBoundY = lowerBoundY;
    }

    public int getUpperBoundX() {
        return upperBoundX;
    }

    public void setUpperBoundX(int upperBoundX) {
        this.upperBoundX = upperBoundX;
    }

    public int getUpperBoundY() {
        return upperBoundY;
    }

    public void setUpperBoundY(int upperBoundY) {
        this.upperBoundY = upperBoundY;
    }

    @Override
    public T map(T value) {
        double x = ThreadLocalRandom.current().nextDouble(this.lowerBoundX, this.upperBoundX);
        double y = ThreadLocalRandom.current().nextDouble(this.lowerBoundY, this.upperBoundY);
        value.setProperty(Property.create(KEY_X_COORD, x));
        value.setProperty(Property.create(KEY_Y_COORD, y));
        return value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RandomPlacement.class.getSimpleName() + "[", "]")
          .add("limitX=" + upperBoundX)
          .add("limitY=" + upperBoundY)
          .toString();
    }
}
