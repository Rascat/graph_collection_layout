package org.rascat.gcl.run;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.layout.functions.forces.attractive.StandardAttractiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.GridRepulsiveForces;
import org.rascat.gcl.layout.functions.forces.attractive.WeightedAttractiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.StandardRepulsionFunction;
import org.rascat.gcl.layout.functions.forces.repulsive.WeightedRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.RandomPlacement;
import org.rascat.gcl.layout.functions.prepare.SetPosProperty;
import org.rascat.gcl.layout.ForceDirectedGraphCollectionLayout;
import org.rascat.gcl.io.Render;

public class Workbench {
    public static void main(@NotNull String[] args) throws Exception {
        LayoutParameters params = new LayoutParameters(args);
        int height = params.height(1000);
        int width = params.width(1000);
        int iterations = params.iteration(10);
        double  sameGraphFactor = params.sameGraphFactor(1);
        double differentGraphFactor = params.differentGraphFactor(1);
        int vertices = params.vertices(20);
        boolean isIntermediary = params.isIntermediary();

        ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

//        FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
//        loader.initDatabaseFromFile(params.inputPath());
//        GraphCollection collection = loader.getGraphCollection();

        DataSource source = new CSVDataSource(params.inputPath(), cfg);
        GraphCollection collection = source.getGraphCollection();

        ForceDirectedGraphCollectionLayout layout = new ForceDirectedGraphCollectionLayout
          .Builder(width, height, vertices)
          .initialLayout(new RandomPlacement(width - (width / 10), height - (height / 10)))
          .attractiveForces(new WeightedAttractiveForces(sameGraphFactor, 1))
          .repulsiveForces(new GridRepulsiveForces(new WeightedRepulsionFunction(1, differentGraphFactor)))
          .isIntermediary(isIntermediary)
          .iterations(iterations)
          .build();

        ForceDirectedGraphCollectionLayout baseLayout = new ForceDirectedGraphCollectionLayout
          .Builder(width, height, vertices)
          .initialLayout(new RandomPlacement(width - (width / 10), height - (height / 10)))
          .attractiveForces(new StandardAttractiveForces())
          .repulsiveForces(new GridRepulsiveForces(new StandardRepulsionFunction()))
          .isIntermediary(isIntermediary)
          .iterations(iterations)
          .build();

        GraphCollection testCollection = layout.execute(collection);
//        GraphCollection baseCollection = baseLayout.execute(collection);

        DataSet<EPGMVertex> positionedVertices = testCollection.getVertices().map(new SetPosProperty());
//        DataSet<EPGMVertex> positionedBaseVertices = baseCollection.getVertices().map(new SetPosProperty());


        String outputPath = params.outputPath();

        testCollection = testCollection.getFactory().fromDataSets(testCollection.getGraphHeads(), positionedVertices, testCollection.getEdges());
//        baseCollection = baseCollection.getFactory().fromDataSets(baseCollection.getGraphHeads(), positionedBaseVertices, baseCollection.getEdges());

        DOTDataSink sink = new DOTDataSink(String.format("%s/%d-%.0f-%.0f.dot", outputPath, iterations, sameGraphFactor, differentGraphFactor), true, DOTDataSink.DotFormat.SIMPLE);
        testCollection.writeTo(sink, true);

        DOTDataSink baseSink = new DOTDataSink(outputPath + "/base.dot", true, DOTDataSink.DotFormat.SIMPLE);
//        baseCollection.writeTo(baseSink, true);

        Render render = new Render(height, width, String.format("%s/%d-%.0f-%.0f.png", outputPath, iterations, sameGraphFactor, differentGraphFactor));
        render.renderGraphCollection(testCollection, env);
    }

}
