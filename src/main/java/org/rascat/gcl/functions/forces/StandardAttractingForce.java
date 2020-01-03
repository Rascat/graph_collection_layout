package org.rascat.gcl.functions.forces;

public class StandardAttractingForce implements AttractionFunction {

    @Override
    public double attraction(double distance, double optimalDistance) {
        return (distance * distance) / optimalDistance;
    }
}
