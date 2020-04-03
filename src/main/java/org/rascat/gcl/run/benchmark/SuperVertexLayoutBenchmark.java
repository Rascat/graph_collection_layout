package org.rascat.gcl.run.benchmark;

import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSink;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.SuperVertexLayout;
import org.rascat.gcl.util.LayoutParameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class SuperVertexLayoutBenchmark {
  private static String INPUT_PATH;
  private static String OUTPUT_PATH;
  private static String STATISTICS_PATH;
  private static int WIDTH;
  private static int HEIGHT;
  private static int VERTICES;
  private static int PRE_LAYOUT_ITERATIONS;
  private static int ITERATIONS;

  public static void main(String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    INPUT_PATH = params.inputPath();
    OUTPUT_PATH = params.outputPath();
    STATISTICS_PATH = params.statistics("out/statistics.csv");
    VERTICES = params.vertices(100);
    PRE_LAYOUT_ITERATIONS = params.preLayoutIterations(1);
    ITERATIONS = params.iterations(1);

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    DataSource source = new CSVDataSource(INPUT_PATH, cfg);
    GraphCollection collection = source.getGraphCollection();

    SuperVertexLayout layout = SuperVertexLayout.builder(VERTICES)
      .preLayoutIterations(PRE_LAYOUT_ITERATIONS)
      .iterations(ITERATIONS)
      .superKFactor(3D)
      .build();

    collection = layout.execute(collection);
    WIDTH = layout.getWidth();
    HEIGHT = layout.getHeight();

    DataSink sink = new CSVDataSink(OUTPUT_PATH, cfg);
    collection.writeTo(sink, true);

    env.execute();
    writeStatistics(env);
  }

  /**
   * Write benchmark meta data to csv file
   *
   * @param env given ExecutionEnvironment
   * @throws IOException exception during file writing
   */
  private static void writeStatistics(ExecutionEnvironment env) throws IOException {

    String template = "%s|%s|%s|%s|%s|%s|%s|%s|%s%n";

    String head = String.format(
      template,
      "Class",
      "Parallelism",
      "Dataset",
      "Runtime",
      "Width",
      "Height",
      "Vertices",
      "Iterations",
      "Pre-Layout-Iterations"
    );

    String tail = String.format(
      template,
      "SuperVertexLayout",
      env.getParallelism(),
      INPUT_PATH,
      env.getLastJobExecutionResult().getNetRuntime(TimeUnit.SECONDS),
      WIDTH,
      HEIGHT,
      VERTICES,
      ITERATIONS,
      PRE_LAYOUT_ITERATIONS
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
