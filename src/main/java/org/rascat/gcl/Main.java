package org.rascat.gcl;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.csv.CSVDataSink;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.sampling.RandomVertexEdgeSampling;
import org.gradoop.flink.model.impl.operators.sampling.SamplingAlgorithm;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Main {

    public static void main(@NotNull String[] args) throws Exception {
        String pathToCsv = Objects.requireNonNull(args[0]);
        String pathToOutput = Objects.requireNonNull(args[1]);

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

        CSVDataSource source = new CSVDataSource(pathToCsv, cfg);
        LogicalGraph graph = source.getLogicalGraph();

        System.out.println("[IG] Amount edges: " + graph.getEdges().count());
        System.out.println("[IG] Amount vertices: " + graph.getVertices().count());
        System.out.println("[IG] Graph heads: " + graph.getGraphHead().collect());

        SamplingAlgorithm sampling = new RandomVertexEdgeSampling(0.05f);
        LogicalGraph sampleGraph = sampling.execute(graph);

        System.out.println("[OG] Amount edges: " + sampleGraph.getEdges().count());
        System.out.println("[OG] Amount vertices: " + sampleGraph.getVertices().count());
        System.out.println("[OG] Graph heads: " + sampleGraph.getGraphHead().collect());

//        DataSink sink = new CSVDataSink(pathToOutput, cfg);
        DataSink sink = new DOTDataSink(pathToOutput + "/ldbc.dot", true, DOTDataSink.DotFormat.HTML);
        sampleGraph.writeTo(sink, true);

        env.execute();
    }
}
