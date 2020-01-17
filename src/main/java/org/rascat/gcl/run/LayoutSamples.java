package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.impl.image.ImageDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.operators.layouting.*;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LayoutSamples {
  public static void main(String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    int height = params.height(1000);
    int width = params.width(1000);
    int iterations = params.iteration(1);
    int vertices = params.vertices(20);

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
    loader.initDatabaseFromFile(params.inputPath());

    GraphCollection collection = loader.getGraphCollection();

    FRLayouter frLayout = new FRLayouter(iterations, vertices);
    CombiLayouter combiLayout = new CombiLayouter(iterations, vertices, .5D);
    FusingFRLayouter fusingLayout = new FusingFRLayouter(iterations, vertices, 0.7D, FusingFRLayouter.OutputFormat.EXTRACTED);
    CentroidFRLayouter centroidFRLayout = new CentroidFRLayouter(iterations, vertices);

    GraphCollection frCollection = frLayout.execute(collection);
    GraphCollection combiCollection = combiLayout.execute(collection);
    GraphCollection fusingCollection = fusingLayout.execute(collection);
    GraphCollection centroidCollection = centroidFRLayout.execute(collection);

    String frPath = params.outputPath() + File.separator + createImgFileName(frLayout, iterations);
    String combiPath = params.outputPath() + File.separator + createImgFileName(combiLayout, iterations);
    String fusingPath = params.outputPath() + File.separator + createImgFileName(fusingLayout, iterations);
    String centroidPath = params.outputPath() + File.separator + createImgFileName(centroidFRLayout, iterations);

    ImageDataSink frSink = new ImageDataSink(frPath, frLayout, width, height);
    ImageDataSink combiSink = new ImageDataSink(combiPath, combiLayout, width, height);
    ImageDataSink fusingSink = new ImageDataSink(fusingPath, fusingLayout, width, height);
    ImageDataSink centroidSink = new ImageDataSink(centroidPath, centroidFRLayout, width, height);

    frCollection.writeTo(frSink);
    combiCollection.writeTo(combiSink);
    fusingCollection.writeTo(fusingSink);
    centroidCollection.writeTo(centroidSink);

    env.execute();
  }

  private static String createImgFileName(LayoutingAlgorithm algorithm, int iterations) {
    DateFormat dateFormat = new SimpleDateFormat("yyMMdd_HH:mm:ss");
    Date date = new Date();
    return String.format("%s_%s_%d.png", dateFormat.format(date), algorithm.getName(), iterations);
  }
}
