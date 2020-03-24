package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.impl.csv.CSVDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.io.Render;
import org.rascat.gcl.layout.RandomLayout;
import org.rascat.gcl.layout.functions.prepare.SetPosProperty;
import org.rascat.gcl.util.GraphCollectionLoader;
import org.rascat.gcl.util.LayoutParameters;

public class CreateInitialLayout {
  public static void main(String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    int height = params.height(1000);
    int width = params.width(1000);
    String inputPath = params.inputPath();
    String outputPath = params.outputPath();
    GraphCollectionLoader.InputFormat type = params.inputFormat(GraphCollectionLoader.InputFormat.GDL);

    ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);
    GraphCollectionLoader loader = new GraphCollectionLoader(cfg);

    GraphCollection collection = loader.load(inputPath, type);

    RandomLayout layout = new RandomLayout(width, height);

    collection = layout.execute(collection);

    collection = collection.callForCollection(new SetPosProperty());

    CSVDataSink sink = new CSVDataSink(outputPath + "/initial-layout-int-csv", cfg);
    collection.writeTo(sink, true);

    Render render = new Render(height, width, outputPath + "/initial-layout-int.png");
    render.renderGraphCollection(collection, env);
  }
}
