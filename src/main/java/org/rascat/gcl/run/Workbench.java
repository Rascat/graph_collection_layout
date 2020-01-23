package org.rascat.gcl.run;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.layout.functions.forces.GridRepulsiveForces;
import org.rascat.gcl.layout.functions.forces.WeightedAttractiveForces;
import org.rascat.gcl.layout.functions.prepare.RandomPlacement;
import org.rascat.gcl.layout.functions.prepare.SetPosProperty;
import org.rascat.gcl.layout.ForceDirectedGraphCollectionLayout;
import org.rascat.gcl.io.Render;


public class Workbench {
    public static void main(@NotNull String[] args) throws Exception {
        LayoutParameters params = new LayoutParameters(args);
        int height = params.height(1000);
        int width = params.width(1000);

        ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

        FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
        loader.initDatabaseFromFile(params.inputPath());

        GraphCollection collection = loader.getGraphCollection();

        ForceDirectedGraphCollectionLayout layout = new ForceDirectedGraphCollectionLayout
          .Builder(width, height, params.vertices(20))
          .initialLayout(new RandomPlacement(width - (width / 10), height - (height / 10)))
          .attractiveForces(new WeightedAttractiveForces(100))
          .repulsiveForces(new GridRepulsiveForces())
          .isIntermediary(params.isIntermediary())
          .iterations(params.iteration(10))
          .build();

        System.out.println(layout);

        collection = layout.execute(collection);

        DataSet<EPGMVertex> positionedVertices = collection.getVertices().map(new SetPosProperty());

        collection = collection.getFactory().fromDataSets(collection.getGraphHeads(), positionedVertices, collection.getEdges());
        DOTDataSink sink = new DOTDataSink("out/result.dot", true, DOTDataSink.DotFormat.SIMPLE);
        collection.writeTo(sink, true);

        Render render = new Render(height, width, params.outputPath());
        render.renderGraphCollection(collection, env);
    }
}
