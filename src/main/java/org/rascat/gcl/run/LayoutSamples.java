package org.rascat.gcl.run;

import org.apache.commons.cli.ParseException;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.impl.image.ImageDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.operators.layouting.LayoutingAlgorithm;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.gradoop.flink.model.impl.operators.layouting.FRLayouter;

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

        ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

        FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
        loader.initDatabaseFromFile(params.inputPath());

        GraphCollection collection = loader.getGraphCollection();

        for (int i = 1; i < 10; i++) {
            FRLayouter layout = new FRLayouter(i, vertices);
            layout.area(width, height);

            collection = layout.execute(collection);

            String out = params.outputPath() + File.separator + createImgFileName(layout, i);
            ImageDataSink sink = new ImageDataSink(out, layout, width, height);
            collection.writeTo(sink);
        }

        env.execute();
    }

    private static String createImgFileName(LayoutingAlgorithm algorithm, int iterations) {
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd_HH:mm:ss");
        Date date = new Date();
        return String.format("%s_%s_%d.png", dateFormat.format(date), algorithm.getName(), iterations);
    }
}
