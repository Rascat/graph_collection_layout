package org.rascat.gcl.layout.functions.cooling;

public class ExponentialSimulatedAnnealing implements CoolingSchedule {

  private double startTemp;
  private double endTemp;
  private int maxIterations;
  private double base;

  public ExponentialSimulatedAnnealing(int width, int height, double k, int maxIterations) {
    this.startTemp = Math.sqrt(width*width + height*height) / 2.0;
    this.endTemp = k / 10.0;
    this.maxIterations = maxIterations;
    calculateBase();
  }

  @Override
  public double computeTemperature(int currentIteration) {
    return startTemp * Math.pow(base, currentIteration);
  }

  private void calculateBase() {
    this.base = Math.pow(endTemp / startTemp, 1.0 / maxIterations);
  }
}
