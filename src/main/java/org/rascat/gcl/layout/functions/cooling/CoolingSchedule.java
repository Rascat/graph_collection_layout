package org.rascat.gcl.layout.functions.cooling;

import java.io.Serializable;

public interface CoolingSchedule extends Serializable {
  double computeTemperature(int currentIteration);
}
