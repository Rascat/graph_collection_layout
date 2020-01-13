package org.rascat.gcl.functions.forces;

import java.io.Serializable;

public class WeightedAttractingForce implements Serializable {

    private final double sameGraphFactor = 2;
    private final double defaultFactor = 1;

    public double weightedAttraction(double distance, double optimalDistance, boolean sameGraph) {
        double factor = sameGraph ? sameGraphFactor : defaultFactor;
        return (distance*distance / optimalDistance) * factor;
    }
}
