package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.commons.cli.*;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
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

//        RandomGraphCollectionLayout layout = new RandomGraphCollectionLayout(1000, 1000);
        ForceDirectedGraphCollectionLayout layout = new ForceDirectedGraphCollectionLayout(width, height);
        layout.setIterations(params.iteration(1));
        layout.setIsIntermediaryLayout(params.isIntermediary());

        collection = layout.execute(collection, params.vertices(20));

        Render render = new Render(height, width, params.outputPath());
        render.renderGraphCollection(collection, env);
    }

}
