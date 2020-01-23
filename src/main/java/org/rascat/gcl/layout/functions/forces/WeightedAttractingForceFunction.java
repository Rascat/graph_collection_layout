package org.rascat.gcl.layout.functions.forces;

import java.io.Serializable;

public class WeightedAttractingForceFunction implements Serializable {

    private double sameGraphFactor = 3;
    private double differentGraphFactor = 1;

    public WeightedAttractingForceFunction() {}

    public WeightedAttractingForceFunction(double sameGraphFactor, double differentGraphFactor) {
        this.sameGraphFactor = sameGraphFactor;
        this.differentGraphFactor = differentGraphFactor;
    }

    public double weightedAttraction(double distance, double optimalDistance, boolean sameGraph) {
        double factor = sameGraph ? sameGraphFactor : differentGraphFactor;
        return (distance*distance / optimalDistance) * factor;
    }
}
