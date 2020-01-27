package org.rascat.gcl.run;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.io.impl.csv.CSVDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.io.Render;
import org.rascat.gcl.layout.RandomGraphCollectionLayout;
import org.rascat.gcl.layout.functions.prepare.SetPosProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateInitialLayout {
  public static void main(String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    int height = params.height(1000);
    int width = params.width(1000);

    Path inputPath = Paths.get(params.inputPath());

    ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
    loader.initDatabaseFromFile(inputPath.toString());

    GraphCollection collection = loader.getGraphCollection();

    RandomGraphCollectionLayout layout = new RandomGraphCollectionLayout(width, height);

    collection = layout.execute(collection);
    DataSet<EPGMVertex> positionedVertices= collection.getVertices().map(new SetPosProperty());

    collection = collection.getFactory()
      .fromDataSets(collection.getGraphHeads(), positionedVertices, collection.getEdges());

    CSVDataSink sink = new CSVDataSink(params.outputPath() + "/initial-layout-int-csv", cfg);

    collection.writeTo(sink, true);

    Render render = new Render(height, width, params.outputPath() + "/initial-layout-int.png");
    render.renderGraphCollection(collection, env);
  }
}
