package org.rascat.gcl.functions.forces;

public class StandardRepulsingForce implements RepulsionFunction{

    @Override
    public double repulsion(double distance, double optimalDistance) {
        return (optimalDistance * optimalDistance) / distance;
    }
}
