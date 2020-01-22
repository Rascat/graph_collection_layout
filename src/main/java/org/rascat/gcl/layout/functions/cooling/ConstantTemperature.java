package org.rascat.gcl.layout.functions.cooling;

import org.rascat.gcl.layout.api.CoolingSchedule;

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
