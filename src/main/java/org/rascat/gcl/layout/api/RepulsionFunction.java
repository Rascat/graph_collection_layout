package org.rascat.gcl.layout.api;

import java.io.Serializable;

@FunctionalInterface
public interface RepulsionFunction extends Serializable {
    double repulsion(double distance, double optimalDistance);
}
