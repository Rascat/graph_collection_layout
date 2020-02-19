package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.impl.image.ImageDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.layouting.FRLayouter;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.layout.transformations.GraphTransactionsToLogicalGraphTransformation;
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

    GraphTransactionsToLogicalGraphTransformation transformation = new GraphTransactionsToLogicalGraphTransformation(cfg);
    LogicalGraph graph = transformation.transform(collection.getGraphTransactions());

    FRLayouter layout = new FRLayouter(iterations, vertices);
    layout.k(300);
    graph = layout.execute(graph);

    ImageDataSink sink = new ImageDataSink(outputPath + File.separator + "transform.png", layout, 1000, 1000);
    graph.writeTo(sink);

    env.execute();
  }
}
