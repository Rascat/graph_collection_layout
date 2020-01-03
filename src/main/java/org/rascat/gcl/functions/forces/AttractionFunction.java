package org.rascat.gcl.functions.forces;

import java.io.Serializable;

@FunctionalInterface
public interface AttractionFunction extends Serializable {
    double attraction(double distance, double optimalDistance);
}
