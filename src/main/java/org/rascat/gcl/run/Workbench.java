package org.rascat.gcl.run;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.jetbrains.annotations.NotNull;
import org.rascat.gcl.layout.functions.forces.repulsive.GridRepulsiveForces;
import org.rascat.gcl.layout.functions.forces.attractive.WeightedAttractiveForces;
import org.rascat.gcl.layout.functions.forces.repulsive.WeightedRepulsionFunction;
import org.rascat.gcl.layout.functions.prepare.RandomPlacement;
import org.rascat.gcl.layout.functions.prepare.SetPosProperty;
import org.rascat.gcl.layout.ForceDirectedGraphCollectionLayout;
import org.rascat.gcl.io.Render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        GraphCollection collection = loadGraphCollection(params.inputPath(), params.inputType(LayoutParameters.InputType.GDL), cfg);

        ForceDirectedGraphCollectionLayout layout = new ForceDirectedGraphCollectionLayout
          .Builder(width, height, vertices)
          .initialLayout(new RandomPlacement(width - (width / 10), height - (height / 10)))
          .attractiveForces(new WeightedAttractiveForces(sameGraphFactor, 1))
          .repulsiveForces(new GridRepulsiveForces(new WeightedRepulsionFunction(1, differentGraphFactor)))
          .isIntermediary(isIntermediary)
          .iterations(iterations)
          .build();


        GraphCollection testCollection = layout.execute(collection);

        // set pos property so we can view the layout with tools like gephi
        DataSet<EPGMVertex> positionedVertices = testCollection.getVertices().map(new SetPosProperty());

        // set graph id as property so we can partition the graph against that
        DataSet<EPGMVertex> annotatedVertices = positionedVertices.map(
          (MapFunction<EPGMVertex, EPGMVertex>) value -> {
            List<PropertyValue> ids = new ArrayList<>();
            for (GradoopId id: value.getGraphIds()){
                ids.add(PropertyValue.create(id));
            }
            value.setProperty("graphids", ids);

            return value;
        });

        String outputPath = params.outputPath();

        testCollection = testCollection.getFactory().fromDataSets(testCollection.getGraphHeads(), annotatedVertices, testCollection.getEdges());

        DOTDataSink sink = new DOTDataSink(String.format("%s/%d-%.0f-%.0f.dot", outputPath, iterations, sameGraphFactor, differentGraphFactor), true, DOTDataSink.DotFormat.SIMPLE);
        testCollection.writeTo(sink, true);


        Render render = new Render(height, width, String.format("%s/%d-%.0f-%.0f.png", outputPath, iterations, sameGraphFactor, differentGraphFactor));
        render.renderGraphCollection(testCollection, env);
    }

    private static GraphCollection loadGraphCollection(String inputPath, LayoutParameters.InputType type, GradoopFlinkConfig cfg) throws IOException {
        if (type == LayoutParameters.InputType.GDL) {
            FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
            loader.initDatabaseFromFile(inputPath);
            return  loader.getGraphCollection();

        } else if (type == LayoutParameters.InputType.CSV) {
            DataSource source = new CSVDataSource(inputPath, cfg);
            return  source.getGraphCollection();

        } else {
            throw new IllegalArgumentException("Unable to handle file type " + type);
        }
    }
}
