package org.rascat.gcl.run.draw;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.io.impl.image.ImageDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.AsymmetricForceDirectedLayout;
import org.rascat.gcl.layout.functions.forces.attractive.WeightedAttractiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.GridRepulsiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.WeightedRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.CastDoubleCoordToInt;
import org.rascat.gcl.util.LayoutParameters;

import java.io.File;

public class DrawAsymmetricLayout {

  private static String INPUT_PATH;
  private static String OUTPUT_PATH;
  private static int WIDTH;
  private static int HEIGHT;
  private static int VERTICES;
  private static double SGF;
  private static double DGF;
  private static int ITERATIONS;

  public static void main(String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    INPUT_PATH = params.inputPath();
    OUTPUT_PATH = params.outputPath();
    VERTICES = params.vertices(0);
    SGF = params.sameGraphFactor(1);
    DGF = params.differentGraphFactor(1);
    ITERATIONS = params.iterations(1);

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    DataSource source = new CSVDataSource(INPUT_PATH, cfg);
    GraphCollection collection = source.getGraphCollection();

    AsymmetricForceDirectedLayout layout = AsymmetricForceDirectedLayout.builder(VERTICES)
      .attractiveForces(new WeightedAttractiveForces(SGF, 1))
      .repulsiveForces(new GridRepulsiveForces(new WeightedRepulsionFunction(1, DGF)))
      .iterations(ITERATIONS)
      .build();

    collection = layout.execute(collection);

    // convert coordinate data type from double to int
    collection = collection.callForCollection(new CastDoubleCoordToInt());

    WIDTH = layout.getWidth();
    HEIGHT = layout.getHeight();

    String fileName = String.format("%s%cafdl-v=%d-i=%d-sgf=%.0f-dgf=%.0f-a=%dx%d.png",
      OUTPUT_PATH, File.separatorChar, VERTICES, ITERATIONS, SGF, DGF, WIDTH, HEIGHT);

    ImageDataSink sink = new ImageDataSink(fileName, WIDTH, HEIGHT, WIDTH, HEIGHT);
    sink.write(collection);

    env.execute("DrawAsymmetricLayout with " + INPUT_PATH);
  }
}
