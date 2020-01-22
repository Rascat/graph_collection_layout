package org.rascat.gcl.layout.api;

import java.io.Serializable;

public interface CoolingSchedule extends Serializable {
  double computeTemperature(int currentIteration);
}
