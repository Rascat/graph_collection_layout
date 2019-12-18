package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.layout.ForceDirectedGraphCollectionLayout;
import org.rascat.gcl.layout.RandomGraphCollectionLayout;
import org.rascat.gcl.print.Render;

import java.util.Objects;

public class GdlToDotExample {
    public static void main(@NotNull String[] args) throws Exception {
        String pathToGdl = Objects.requireNonNull(args[0]);
        String pathToOutput = Objects.requireNonNull(args[1]);

        ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

        FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
        loader.initDatabaseFromFile(pathToGdl);

        GraphCollection collection = loader.getGraphCollection();

        RandomGraphCollectionLayout layout = new RandomGraphCollectionLayout(1000, 1000);
//        ForceDirectedGraphCollectionLayout layout = new ForceDirectedGraphCollectionLayout(1000, 1000);

        collection = layout.execute(collection);

        Render render = new Render(1000, 1000, pathToOutput);
        render.renderGraphCollection(collection, env);
    }
}
