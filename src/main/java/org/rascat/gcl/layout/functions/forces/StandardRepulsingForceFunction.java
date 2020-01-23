package org.rascat.gcl.layout.functions.forces;

import org.rascat.gcl.layout.api.RepulsionFunction;

public class StandardRepulsingForceFunction implements RepulsionFunction {

    @Override
    public double repulsion(double distance, double optimalDistance) {
        return (optimalDistance * optimalDistance) / distance;
    }
}
