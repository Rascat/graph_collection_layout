package org.rascat.gcl.functions.cooling;

import org.rascat.gcl.functions.cooling.CoolingSchedule;

public class LinearSimulatedAnnealing implements CoolingSchedule {

  private double startTemperature;

  public LinearSimulatedAnnealing(double startTemperature) {
    this.startTemperature = startTemperature;
  }

  @Override
  public double computeTemperature(int currentIteration) {
    return startTemperature / currentIteration;
  }
}
