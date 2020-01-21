package org.rascat.gcl.layout.functions.forces;

import java.io.Serializable;

@FunctionalInterface
public interface AttractionFunction extends Serializable {
    double attraction(double distance, double optimalDistance);
}
