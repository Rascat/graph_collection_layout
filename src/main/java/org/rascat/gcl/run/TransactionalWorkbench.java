package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.io.Render;
import org.rascat.gcl.layout.TransactionalGraphCollectionLayout;
import org.rascat.gcl.util.GraphCollectionLoader;
import org.rascat.gcl.util.LayoutParameters;

import java.io.File;

public class TransactionalWorkbench {
  public static void main(@NotNull String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    int height = params.height(1000);
    int width = params.width(1000);
    int iterations = params.iteration(10);
    double  sameGraphFactor = params.sameGraphFactor(1);
    double differentGraphFactor = params.differentGraphFactor(1);
    int vertices = params.vertices(20);
    boolean isIntermediary = params.isIntermediary();
    String outputPath = params.outputPath();
    String inputPath = params.inputPath();
    GraphCollectionLoader.InputFormat inputFormat = params.inputFormat(GraphCollectionLoader.InputFormat.GDL);

    ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);
    GraphCollectionLoader loader = new GraphCollectionLoader(cfg);

    GraphCollection collection = loader.load(inputPath, inputFormat);

    TransactionalGraphCollectionLayout layout = new TransactionalGraphCollectionLayout(width, height);
    collection = layout.execute(collection);

    Render render = new Render(height, width, outputPath + File.separator + "transactional.png");
    render.renderGraphCollection(collection, env);
  }
}
