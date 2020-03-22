package org.rascat.gcl.run.benchmark;

import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.csv.CSVDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.AsymmetricForceDirectedLayout;
import org.rascat.gcl.layout.functions.forces.attractive.WeightedAttractiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.GridRepulsiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.WeightedRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.RandomPlacement;
import org.rascat.gcl.util.GraphCollectionLoader;
import org.rascat.gcl.util.LayoutParameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static org.rascat.gcl.util.GraphCollectionLoader.*;

public class AsymmetricForceDirectedLayoutBenchmark {

  private static String INPUT_PATH;
  private static String OUTPUT_PATH;
  private static String STATISTICS_PATH;
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
    STATISTICS_PATH = params.statistics("out/statistics.csv");
    WIDTH = params.width(1000);
    HEIGHT = params.height(1000);
    VERTICES = params.vertices(100);
    SGF = params.sameGraphFactor(1);
    DGF = params.differentGraphFactor(1);
    ITERATIONS = params.iterations(1);
    InputFormat inputFormat = params.inputFormat(InputFormat.CSV);

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    GraphCollectionLoader loader = new GraphCollectionLoader(cfg);
    GraphCollection collection = loader.load(INPUT_PATH, inputFormat);

    AsymmetricForceDirectedLayout layout = AsymmetricForceDirectedLayout.builder(WIDTH, HEIGHT)
      .initialLayout(new RandomPlacement<>(WIDTH / 10, HEIGHT / 10, WIDTH - (WIDTH / 10), HEIGHT - (HEIGHT / 10)))
      .attractiveForces(new WeightedAttractiveForces(SGF, 1))
      .repulsiveForces(new GridRepulsiveForces(new WeightedRepulsionFunction(1, DGF)))
      .iterations(ITERATIONS)
      .build();

    collection = layout.execute(collection);

    DataSink sink = new CSVDataSink(OUTPUT_PATH, cfg);
    collection.writeTo(sink, true);

    env.execute();
    writeMetaData(env);
  }

  /**
   * Write benchmark meta data to csv file
   *
   * @param env given ExecutionEnvironment
   * @throws IOException exception during file writing
   */
  private static void writeMetaData(ExecutionEnvironment env) throws IOException {

    String template = "%s | %s | %s | %s | %s | %s | %s | %s | %s%n";

    String head = String.format(
      template,
      "Parallelism",
      "Dataset",
      "Runtime",
      "Width",
      "Height",
      "Vertices",
      "Iterations",
      "SGF",
      "DGF"
    );

    String tail = String.format(
      template,
      env.getParallelism(),
      INPUT_PATH,
      env.getLastJobExecutionResult().getNetRuntime(TimeUnit.SECONDS),
      WIDTH,
      HEIGHT,
      VERTICES,
      ITERATIONS,
      SGF,
      DGF
    );

    File f = new File(STATISTICS_PATH);
    if (f.exists() && !f.isDirectory()) {
      FileUtils.writeStringToFile(f, tail, true);
    } else {
      PrintWriter writer = new PrintWriter(STATISTICS_PATH, "UTF-8");
      writer.print(head);
      writer.print(tail);
      writer.close();
    }
  }
}
