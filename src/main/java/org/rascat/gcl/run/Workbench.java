package org.rascat.gcl.run;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.functions.forces.repulsive.GridRepulsiveForces;
import org.rascat.gcl.layout.functions.forces.attractive.WeightedAttractiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.WeightedRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.SetGraphIdsProperty;
import org.rascat.gcl.layout.functions.prepare.SetPosProperty;
import org.rascat.gcl.layout.AsymmetricForceDirectedLayout;
import org.rascat.gcl.io.Render;
import org.rascat.gcl.util.GraphCollectionLoader;
import org.rascat.gcl.util.LayoutParameters;

import java.io.File;

import static org.rascat.gcl.util.GraphCollectionLoader.*;

public class Workbench {
    public static void main(String[] args) throws Exception {
        LayoutParameters params = new LayoutParameters(args);
        int iterations = params.iterations(10);
        int numVertices = params.vertices(20);
        double  sameGraphFactor = params.sameGraphFactor(1);
        double differentGraphFactor = params.differentGraphFactor(1);
        boolean isIntermediary = params.isIntermediary();
        String outputPath = params.outputPath();
        String inputPath = params.inputPath();
        InputFormat inputFormat = params.inputFormat(InputFormat.GDL);

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);
        GraphCollectionLoader loader = new GraphCollectionLoader(cfg);

        GraphCollection collection = loader.load(inputPath, inputFormat);

        AsymmetricForceDirectedLayout layout = AsymmetricForceDirectedLayout.builder(numVertices)
          .attractiveForces(new WeightedAttractiveForces(sameGraphFactor, 1))
          .repulsiveForces(new GridRepulsiveForces(new WeightedRepulsionFunction(1, differentGraphFactor)))
          .isIntermediary(isIntermediary)
          .iterations(iterations)
          .build();

        GraphCollection layoutCollection = layout.execute(collection);

        // set pos property so we can view the layout with tools like gephi
        layoutCollection = layoutCollection.callForCollection(new SetPosProperty());

        // set graph id as property so we can partition the graph against that
        layoutCollection = layoutCollection.callForCollection(new SetGraphIdsProperty());

        String dotFileName = String.format("%s%c%d-%.0f-%.0f.dot",
          outputPath, File.separatorChar, iterations, sameGraphFactor, differentGraphFactor);

        DOTDataSink sink = new DOTDataSink(dotFileName, true, DOTDataSink.DotFormat.SIMPLE);
        layoutCollection.writeTo(sink, true);

        String pngFileName = String.format("%s%c%d-%.0f-%.0f.png",
          outputPath, File.separatorChar, iterations, sameGraphFactor, differentGraphFactor);

        Render render = new Render(layout.getHeight(), layout.getWidth(), pngFileName);
        render.renderGraphCollection(layoutCollection, env);
    }
}
