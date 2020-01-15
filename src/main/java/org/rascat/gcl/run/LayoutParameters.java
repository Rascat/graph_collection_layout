package org.rascat.gcl.run;

import org.apache.commons.cli.*;

public class LayoutParameters {

    private CommandLine cmd;

    private final String PARAM_INPUT = "input";
    private final String PARAM_OUTPUT = "output";
    private final String PARAM_ITERATIONS = "iterations";
    private final String PARAM_WIDTH = "width";
    private final String PARAM_HEIGHT = "height";
    private final String PARAM_VERTICES = "vertices";
    private final String PARAM_INTERMEDIARY = "intermediary";

    public LayoutParameters(String[] args) throws ParseException {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        this.cmd = parser.parse(options, args);
    }

    public String inputPath() {
        return cmd.getOptionValue(PARAM_INPUT);
    }

    public String outputPath() {
        return cmd.getOptionValue(PARAM_OUTPUT);
    }

    public int iteration(int defaultValue) {
       return cmd.getOptionValue(PARAM_ITERATIONS) == null ? defaultValue
               : Integer.parseInt(cmd.getOptionValue(PARAM_ITERATIONS));
    }

    public int width(int defaultValue) {
        return cmd.getOptionValue(PARAM_WIDTH) == null ? defaultValue
                : Integer.parseInt(cmd.getOptionValue(PARAM_WIDTH));
    }

    public int height(int defaultValue) {
        return cmd.getOptionValue(PARAM_HEIGHT) == null ? defaultValue
                : Integer.parseInt(cmd.getOptionValue(PARAM_HEIGHT));
    }

    public int vertices(int defaultValue) {
        return cmd.getOptionValue(PARAM_VERTICES) == null ? defaultValue
                : Integer.parseInt(cmd.getOptionValue(PARAM_VERTICES));
    }

    public boolean isIntermediary() {
        return cmd.hasOption(PARAM_INTERMEDIARY);
    }

    private Options createOptions() {
        Options options = new Options();

        Option input = Option.builder(PARAM_INPUT)
                .required(true)
                .hasArg(true)
                .desc("Path to input gdl file.")
                .build();

        Option output = Option.builder(PARAM_OUTPUT)
                .required(true)
                .hasArg(true)
                .desc("Path to output directory.")
                .build();

        Option intermediary = Option.builder(PARAM_INTERMEDIARY)
                .required(false)
                .hasArg(false)
                .desc("Flag to signal whether the input graph already contains layout information and therefore does not need to undergo an initial layout procedure.")
                .build();

        Option iterations = Option.builder(PARAM_ITERATIONS)
                .required(false)
                .hasArg(true)
                .desc("Number of iterations the layout algorithm is executed.")
                .build();

        Option width = Option.builder(PARAM_WIDTH)
                .required(false)
                .hasArg(true)
                .desc("Width of the layout.")
                .build();

        Option height = Option.builder(PARAM_HEIGHT)
                .required(false)
                .hasArg(true)
                .desc("Height of the layout.")
                .build();

        Option vertices = Option.builder(PARAM_VERTICES)
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
}
