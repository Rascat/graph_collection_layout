package org.rascat.gcl.layout.api;

import java.io.Serializable;

@FunctionalInterface
public interface AttractionFunction extends Serializable {
    double attraction(double distance, double optimalDistance);
}
