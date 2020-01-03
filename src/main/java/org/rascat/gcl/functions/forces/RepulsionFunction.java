package org.rascat.gcl.functions.forces;

import java.io.Serializable;

@FunctionalInterface
public interface RepulsionFunction extends Serializable {
    double repulsion(double distance, double optimalDistance);
}
