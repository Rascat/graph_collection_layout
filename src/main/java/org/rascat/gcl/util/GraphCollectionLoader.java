package org.rascat.gcl.util;

import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;

import java.io.IOException;

public class GraphCollectionLoader {

  private GradoopFlinkConfig cfg;

  public GraphCollectionLoader(GradoopFlinkConfig cfg) {
    this.cfg = cfg;
  }

  public GraphCollection load(String inputPath, InputFormat type) throws IOException {
    if (type == InputFormat.GDL) {
      FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
      loader.initDatabaseFromFile(inputPath);
      return  loader.getGraphCollection();

    } else if (type == InputFormat.CSV) {
      DataSource source = new CSVDataSource(inputPath, cfg);
      return  source.getGraphCollection();

    } else {
      throw new IllegalArgumentException("Unable to handle file type " + type);
    }
  }

  public enum InputFormat {
    GDL("gdl"),
    CSV("csv");

    private String id;

    InputFormat(String id) {
      this.id = id;
    }

    public String getId() {
      return this.id;
    }
  }
}
