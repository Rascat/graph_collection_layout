package org.rascat.gcl.run;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.commons.cli.*;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.functions.SetGephiPosValue;
import org.rascat.gcl.layout.ForceDirectedGraphCollectionLayout;
import org.rascat.gcl.io.Render;


public class Workbench {
    public static void main(@NotNull String[] args) throws Exception {
        Options options = createOptions();
        CommandLine cmd = parseOptions(options, args);

        String pathToGdl = cmd.getOptionValue("input");
        String pathToOutput = cmd.getOptionValue("output");

        int numIterations;
        if (cmd.getOptionValue("iterations") == null) {
            numIterations = 1;
        } else {
            numIterations = Integer.parseInt(cmd.getOptionValue("iterations"));
        }

        int width;
        if (cmd.getOptionValue("width") == null) {
            width = 1000;
        } else {
            width = Integer.parseInt(cmd.getOptionValue("width"));
        }

        int height;
        if (cmd.getOptionValue("height") == null) {
            height = 1000;
        } else {
            height = Integer.parseInt(cmd.getOptionValue("height"));
        }

        int numVertices;
        if (cmd.getOptionValue("vertices") == null) {
            numVertices = 20;
        } else {
            numVertices = Integer.parseInt(cmd.getOptionValue("vertices"));
        }

        boolean isIntermediary = cmd.hasOption("intermediary");

        ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

        FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
        loader.initDatabaseFromFile(pathToGdl);

        GraphCollection collection = loader.getGraphCollection();

//        RandomGraphCollectionLayout layout = new RandomGraphCollectionLayout(1000, 1000);
        ForceDirectedGraphCollectionLayout layout = new ForceDirectedGraphCollectionLayout(width, height);
        layout.setIterations(numIterations);
        layout.setIsIntermediaryLayout(isIntermediary);

        collection = layout.execute(collection, numVertices);

        DataSet<EPGMVertex> positionedVertices = collection.getVertices().map(new SetGephiPosValue());

        collection = collection.getFactory().fromDataSets(collection.getGraphHeads(), positionedVertices, collection.getEdges());
        DOTDataSink sink = new DOTDataSink("out/result.dot", true, DOTDataSink.DotFormat.SIMPLE);
        collection.writeTo(sink, true);

        Render render = new Render(height, width, pathToOutput);
        render.renderGraphCollection(collection, env);
    }

    private static Options createOptions() {
        Options options = new Options();

        Option input = Option.builder("input")
                .required(true)
                .hasArg(true)
                .desc("Path to input gdl file.")
                .build();

        Option output = Option.builder("output")
                .required(true)
                .hasArg(true)
                .desc("Path to output png file.")
                .build();

        Option intermediary = Option.builder("intermediary")
                .required(false)
                .hasArg(false)
                .desc("Flag to signal whether the input graph already contains layout information and therefore does not need to undergo an initial layout procedure.")
                .build();

        Option iterations = Option.builder("iterations")
                .required(false)
                .hasArg(true)
                .desc("Number of iterations the layout algorithm is executed.")
                .build();

        Option width = Option.builder("width")
                .required(false)
                .hasArg(true)
                .desc("Width of the layout.")
                .build();

        Option height = Option.builder("height")
                .required(false)
                .hasArg(true)
                .desc("Height of the layout.")
                .build();

        Option vertices = Option.builder("vertices")
                .required(false)
                .hasArg(true)
                .desc("Number of vertices in the input graph.")
                .build();

        return options
                .addOption(input)
                .addOption(output)
                .addOption(iterations)
                .addOption(width)
                .addOption(height)
                .addOption(vertices)
                .addOption(intermediary);
    }

    private static CommandLine parseOptions(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }
}
