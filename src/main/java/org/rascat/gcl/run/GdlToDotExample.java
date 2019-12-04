package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.layout.RandomGraphCollectionLayout;
import org.rascat.gcl.util.Printer;

import java.util.Objects;

public class GdlToDotExample {
    public static void main(@NotNull String[] args) throws Exception {
        String pathToGdl = Objects.requireNonNull(args[0]);
        String pathToOutput = Objects.requireNonNull(args[1]);

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

        FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
        loader.initDatabaseFromFile(pathToGdl);
        LogicalGraph g1 = loader.getLogicalGraphByVariable("g1");
        LogicalGraph g2 = loader.getLogicalGraphByVariable("g2");
        LogicalGraph g3 = loader.getLogicalGraphByVariable("g3");
        LogicalGraph g4 = loader.getLogicalGraphByVariable("g4");
        LogicalGraph g5 = loader.getLogicalGraphByVariable("g5");

        Printer.printGraphInfo(g1, "g1");
        Printer.printGraphInfo(g2, "g2");
        Printer.printGraphInfo(g3, "g3");
        Printer.printGraphInfo(g4, "g4");
        Printer.printGraphInfo(g5, "g5");

        GraphCollection collection = loader.getGraphCollection();

        RandomGraphCollectionLayout layout = new RandomGraphCollectionLayout(100, 100, cfg);
        collection = layout.execute(collection);

        DataSink sink = new DOTDataSink(pathToOutput, true, DOTDataSink.DotFormat.HTML);
        collection.writeTo(sink, true);

        env.execute();
    }
}
