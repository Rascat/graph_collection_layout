package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.impl.image.ImageDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.io.Render;
import org.rascat.gcl.layout.SuperVertexGraphCollectionLayout;
import org.rascat.gcl.util.GraphCollectionLoader;
import org.rascat.gcl.util.LayoutParameters;

import java.io.File;

public class SuperVertexLayoutWorkbench {
  public static void main(@NotNull String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    int height = params.height(1000);
    int width = params.width(1000);
    int iterations = params.iteration(10);
    int vertices = params.vertices(20);
    String outputPath = params.outputPath();
    String inputPath = params.inputPath();
    GraphCollectionLoader.InputFormat inputFormat = params.inputFormat(GraphCollectionLoader.InputFormat.GDL);

    ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);
    GraphCollectionLoader loader = new GraphCollectionLoader(cfg);

    GraphCollection collection = loader.load(inputPath, inputFormat);

    SuperVertexGraphCollectionLayout layout = new SuperVertexGraphCollectionLayout(width, height, cfg);
    collection = layout.execute(collection);

    Render render = new Render(height, width, outputPath + "/supervertex.png");
    render.renderGraphCollection(collection, env);

//    ImageDataSink sink = new ImageDataSink(outputPath + File.separator + "super-vertex.png", layout, width, height);
//    result.writeTo(sink);

//    env.execute();
  }
}
