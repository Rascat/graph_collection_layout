package org.rascat.gcl.functions.cooling;

public class ConstantTemperature implements CoolingSchedule {

  private double temperature;

  public ConstantTemperature(double temperature) {
    this.temperature = temperature;
  }

  @Override
  public double computeTemperature(int currentIteration) {
    return this.temperature;
  }
}
