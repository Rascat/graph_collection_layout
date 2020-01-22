package org.rascat.gcl.layout.functions.forces;

import org.rascat.gcl.layout.api.AttractionFunction;

public class StandardAttractingForce implements AttractionFunction {

    @Override
    public double attraction(double distance, double optimalDistance) {
        return (distance * distance) / optimalDistance;
    }
}
