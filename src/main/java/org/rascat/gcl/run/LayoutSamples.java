package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.impl.image.ImageDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.operators.layouting.*;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.util.GraphCollectionLoader;
import org.rascat.gcl.util.LayoutParameters;

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
    GraphCollectionLoader.InputFormat type = params.inputFormat(GraphCollectionLoader.InputFormat.GDL);
    boolean isIntermediary = params.isIntermediary();

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);
    GraphCollectionLoader loader = new GraphCollectionLoader(cfg);

    GraphCollection collection = loader.load(params.inputPath(), type);

    FRLayouter frLayout = new FRLayouter(iterations, vertices).useExistingLayout(isIntermediary);
    CombiLayouter combiLayout = new CombiLayouter(iterations, vertices, params.quality(.5D)).useExistingLayout(isIntermediary);
    FusingFRLayouter fusingLayout = new FusingFRLayouter(iterations, vertices, params.threshold(.5D), FusingFRLayouter.OutputFormat.EXTRACTED);
    fusingLayout.useExistingLayout(isIntermediary);
    CentroidFRLayouter centroidFRLayout = new CentroidFRLayouter(iterations, vertices);
    centroidFRLayout.useExistingLayout(isIntermediary);

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
