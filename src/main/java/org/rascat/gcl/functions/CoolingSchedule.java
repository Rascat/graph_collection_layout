package org.rascat.gcl.functions;

import java.io.Serializable;

public interface CoolingSchedule extends Serializable {
  double computeTemperature(int currentIteration);
}
