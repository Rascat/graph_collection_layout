package org.rascat.gcl.run;

import org.apache.commons.cli.ParseException;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.util.LayoutParameters;

import java.io.IOException;

public class VertexCount {

  public static void main(String[] args) throws Exception {
    LayoutParameters params = new LayoutParameters(args);
    String input = params.inputPath();


    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    DataSource source = new CSVDataSource(input, cfg);
    GraphCollection collection = source.getGraphCollection();

    long vCount = collection.getVertices().count();
    System.out.println(vCount);
  }
}
