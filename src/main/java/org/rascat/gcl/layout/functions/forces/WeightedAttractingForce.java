package org.rascat.gcl.layout.functions.forces;

import java.io.Serializable;

public class WeightedAttractingForce implements Serializable {

    private double sameGraphFactor = 3;
    private double defaultFactor = 1;

    public WeightedAttractingForce() {}

    public WeightedAttractingForce(double sameGraphFactor, double defaultFactor) {
        this.sameGraphFactor = sameGraphFactor;
        this.defaultFactor = defaultFactor;
    }

    public double weightedAttraction(double distance, double optimalDistance, boolean sameGraph) {
        double factor = sameGraph ? sameGraphFactor : defaultFactor;
        return (distance*distance / optimalDistance) * factor;
    }
}
