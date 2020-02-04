package org.rascat.gcl.layout;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.rascat.gcl.layout.AbstractGraphCollectionLayout;
import org.rascat.gcl.layout.TransactionalGraphCollectionLayout;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class TransactionalGraphCollectionLayoutTest {

  private MiniClusterWithClientResource flinkCluster;
  private ExecutionEnvironment env;
  private GradoopFlinkConfig cfg;

  private GraphCollection collection;

  @BeforeClass
  public void setUpFlinkCluster() {
    this.flinkCluster = new MiniClusterWithClientResource(
        new MiniClusterResourceConfiguration.Builder()
        .setNumberSlotsPerTaskManager(2)
        .setNumberTaskManagers(1)
        .build()
    );
  }

  @BeforeMethod
  public void setUpEnv() throws IOException {
    this.env = ExecutionEnvironment.getExecutionEnvironment();
    this.cfg = GradoopFlinkConfig.createConfig(this.env);
    FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
    loader.initDatabaseFromFile("src/test/resources/example.gdl");
    this.collection = loader.getGraphCollection();
  }


  @Test
  public void testExecute() throws Exception {
    TransactionalGraphCollectionLayout layout = new TransactionalGraphCollectionLayout(10, 10);
    GraphCollection collection = layout.execute(this.collection);

    List<EPGMGraphHead> heads = collection.getGraphHeads().collect();

    for (EPGMGraphHead head: heads) {
      System.out.println("X COORD: " + head.getPropertyValue(AbstractGraphCollectionLayout.KEY_X_COORD));
      System.out.println("Y COORD: " + head.getPropertyValue(AbstractGraphCollectionLayout.KEY_Y_COORD));
    }
  }
}