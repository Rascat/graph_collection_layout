package org.rascat.gcl.util;

import org.apache.commons.cli.*;

import static org.rascat.gcl.util.GraphCollectionLoader.*;

public class LayoutParameters {

  private CommandLine cmd;

  private final String PARAM_INPUT = "input";
  private final String PARAM_OUTPUT = "output";
  private final String PARAM_STATISTICS = "statistics";
  private final String PARAM_ITERATIONS = "iterations";
  private final String PARAM_PRE_LAYOUT_ITERATIONS = "prelayoutiterations";
  private final String PARAM_WIDTH = "width";
  private final String PARAM_HEIGHT = "height";
  private final String PARAM_VERTICES = "vertices";
  private final String PARAM_INTERMEDIARY = "intermediary";
  private final String PARAM_COMBI_LAYOUT_QUALITY = "quality";
  private final String PARAM_FUSING_LAYOUT_THRESHOLD = "threshold";
  private final String PARAM_SAME_GRAPH_FACTOR = "sgf";
  private final String PARAM_DIFFERENT_GRAPH_FACTOR = "dgf";
  private final String PARAM_INPUT_FORMAT = "format";

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

  public int iterations(int defaultValue) {
    return cmd.getOptionValue(PARAM_ITERATIONS) == null ? defaultValue
      : Integer.parseInt(cmd.getOptionValue(PARAM_ITERATIONS));
  }

  public int preLayoutIterations(int defaultValue) {
    return cmd.getOptionValue(PARAM_PRE_LAYOUT_ITERATIONS) == null ? defaultValue
      : Integer.parseInt(cmd.getOptionValue(PARAM_PRE_LAYOUT_ITERATIONS));
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

  public double quality(double defaultValue) {
    return cmd.getOptionValue(PARAM_COMBI_LAYOUT_QUALITY) == null ? defaultValue
      : Double.parseDouble(cmd.getOptionValue(PARAM_COMBI_LAYOUT_QUALITY));
  }

  public double threshold(double defaultValue) {
    return cmd.getOptionValue(PARAM_FUSING_LAYOUT_THRESHOLD) == null ? defaultValue
      : Double.parseDouble(cmd.getOptionValue(PARAM_FUSING_LAYOUT_THRESHOLD));
  }

  public double sameGraphFactor(double defaultValue) {
    return cmd.getOptionValue(PARAM_SAME_GRAPH_FACTOR) == null ? defaultValue
      : Double.parseDouble(cmd.getOptionValue(PARAM_SAME_GRAPH_FACTOR));
  }

  public double differentGraphFactor(double defaultValue) {
    return cmd.getOptionValue(PARAM_DIFFERENT_GRAPH_FACTOR) == null ? defaultValue
      : Double.parseDouble(cmd.getOptionValue(PARAM_DIFFERENT_GRAPH_FACTOR));
  }

  public InputFormat inputFormat(InputFormat defaultValue) {
    if (cmd.getOptionValue(PARAM_INPUT_FORMAT) == null)
      return defaultValue;

    for (InputFormat type : InputFormat.values()) {
      if (type.getId().equals(cmd.getOptionValue(PARAM_INPUT_FORMAT)))
        return type;
    }

    throw new IllegalArgumentException("Could not map user input " + cmd.getOptionValue(PARAM_INPUT_FORMAT) + " to input type");
  }

  public String statistics(String defaultValue) {
    return cmd.getOptionValue(PARAM_STATISTICS) == null ? defaultValue
      : cmd.getOptionValue(PARAM_STATISTICS);
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

    Option quality = Option.builder(PARAM_COMBI_LAYOUT_QUALITY)
      .required(false)
      .hasArg(true)
      .desc("Quality of the combi layout (value must be in the range 0 <= x <= 1).")
      .build();

    Option threshold = Option.builder(PARAM_FUSING_LAYOUT_THRESHOLD)
      .required(false)
      .hasArg(true)
      .desc("Threshold describing the vertex similarity under which two vertices are being combined." +
        "Value must be in the range 0 <= x <= 1.")
      .build();

    Option sameGraphFactor = Option.builder(PARAM_SAME_GRAPH_FACTOR)
      .required(false)
      .hasArg(true)
      .desc("Factor with which the computed force between two elements of the same logical graph will be modified")
      .build();

    Option differentGraphFactor = Option.builder(PARAM_DIFFERENT_GRAPH_FACTOR)
      .required(false)
      .hasArg(true)
      .desc("Factor with which the computed force between two elements of different logical graphs will be modified")
      .build();

    Option inputFormat = Option.builder(PARAM_INPUT_FORMAT)
      .required(false)
      .hasArg(true)
      .desc("Format of the input.")
      .build();

    Option preLayoutIterations = Option.builder(PARAM_PRE_LAYOUT_ITERATIONS)
      .required(false)
      .hasArg(true)
      .desc("Number of iterations the pre layout is executed")
      .build();

    Option statistics = Option.builder(PARAM_STATISTICS)
      .required(false)
      .hasArg(true)
      .desc("Path to statistics csv file.")
      .build();

    return options
      .addOption(input)
      .addOption(output)
      .addOption(iterations)
      .addOption(width)
      .addOption(height)
      .addOption(vertices)
      .addOption(intermediary)
      .addOption(quality)
      .addOption(threshold)
      .addOption(sameGraphFactor)
      .addOption(differentGraphFactor)
      .addOption(inputFormat)
      .addOption(preLayoutIterations)
      .addOption(statistics);
  }

}
