package org.rascat.gcl;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.csv.CSVDataSink;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.functions.bool.True;
import org.gradoop.flink.model.impl.functions.epgm.LabelIsIn;
import org.gradoop.flink.model.impl.operators.sampling.RandomVertexEdgeSampling;
import org.gradoop.flink.model.impl.operators.sampling.SamplingAlgorithm;
import org.gradoop.flink.model.impl.operators.subgraph.Subgraph;
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

//        LogicalGraph resultGraph = createSampleGraph(graph, 0.2f);
//        LogicalGraph resultGraph = createSubgraph(graph);
        LogicalGraph resultGraph = graph;

        System.out.println("[OG] Amount edges: " + resultGraph.getEdges().count());
        System.out.println("[OG] Amount vertices: " + resultGraph.getVertices().count());
        System.out.println("[OG] Graph heads: " + resultGraph.getGraphHead().collect());

//        DataSink sink = new CSVDataSink(pathToOutput, cfg);
        DataSink sink = new DOTDataSink(pathToOutput, true, DOTDataSink.DotFormat.HTML);
        resultGraph.writeTo(sink, true);

        env.execute();
    }

    private static LogicalGraph createSampleGraph(LogicalGraph graph, float sampleSize) {
        SamplingAlgorithm sampling = new RandomVertexEdgeSampling(sampleSize);
        return sampling.execute(graph);
    }

    private static LogicalGraph createSubgraph(LogicalGraph graph) {
        return graph.vertexInducedSubgraph(new FilterFunction<EPGMVertex>() {
            @Override
            public boolean filter(EPGMVertex epgmVertex) throws Exception {
                return epgmVertex.getLabel().equals("person");
            }
        });
    }
}
