package org.rascat.gcl.layout.functions.cooling;

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
