package org.rascat.gcl.layout.functions.forces;

import java.io.Serializable;

@FunctionalInterface
public interface RepulsionFunction extends Serializable {
    double repulsion(double distance, double optimalDistance);
}
