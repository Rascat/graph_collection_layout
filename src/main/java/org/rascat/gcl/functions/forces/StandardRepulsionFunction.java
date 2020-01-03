package org.rascat.gcl.functions.forces;

public class StandardRepulsionFunction implements RepulsionFunction{

    @Override
    public double repulsion(double distance, double optimalDistance) {
        return (optimalDistance * optimalDistance) / distance;
    }
}
