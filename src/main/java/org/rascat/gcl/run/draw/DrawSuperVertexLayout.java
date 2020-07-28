package org.rascat.gcl.run.draw;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.io.impl.image.ImageDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.SuperVertexLayout;
import org.rascat.gcl.layout.functions.prepare.CastDoubleCoordToInt;
import org.rascat.gcl.util.LayoutParameters;

import java.io.File;

public class DrawSuperVertexLayout {

  public static void main(String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    String INPUT_PATH = params.inputPath();
    String OUTPUT_PATH = params.outputPath();
    int VERTICES = params.vertices(0);
    int GRAPHS = params.graphs(0);
    int PRE_LAYOUT_ITERATIONS = params.preLayoutIterations(1);
    int ITERATIONS = params.iterations(1);

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    DataSource source = new CSVDataSource(INPUT_PATH, cfg);
    GraphCollection collection = source.getGraphCollection();

    SuperVertexLayout layout = SuperVertexLayout.builder(VERTICES, GRAPHS)
      .preLayoutIterations(PRE_LAYOUT_ITERATIONS)
      .iterations(ITERATIONS)
      .superKFactor(3D)
      .build();

    collection = layout.execute(collection);

    // convert coordinate data type from double to int
    collection = collection.callForCollection(new CastDoubleCoordToInt());

    int WIDTH = layout.getWidth();
    int HEIGHT = layout.getHeight();

    String fileName = String.format("%s%csvl-v=%d-g=%d-i=%d-a=%dx%d.png",
      OUTPUT_PATH, File.separatorChar, VERTICES, GRAPHS, ITERATIONS, WIDTH, HEIGHT);

    ImageDataSink sink = new ImageDataSink(fileName, WIDTH, HEIGHT, WIDTH, HEIGHT);
    sink.write(collection);

    env.execute("DrawSuperVertexLayout with " + INPUT_PATH);
  }
}
