package org.rascat.gcl.layout.functions.reduce;

import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMEdgeFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class DistinctEdgeReduceTest {

  private DistinctEdgeReduce reduce;
  private EPGMEdgeFactory factory;

  private GradoopId idA = GradoopId.fromString("AAAAAAAAAAAAAAAAAAAAAAAA");
  private GradoopId idB = GradoopId.fromString("BBBBBBBBBBBBBBBBBBBBBBBB");
  private GradoopId idC = GradoopId.fromString("CCCCCCCCCCCCCCCCCCCCCCCC");

  @BeforeTest
  public void setUp() {
    this.reduce = new DistinctEdgeReduce();
    this.factory = new EPGMEdgeFactory();
  }

  @Test
  public void testReduce() {
    EPGMEdge edgeA = factory.createEdge(idA, idB);
    EPGMEdge edgeB = factory.createEdge(idA, idB);
    EPGMEdge edgeC = factory.createEdge(idA, idC);

    List<EPGMEdge> edges = new ArrayList<>();
    edges.add(edgeA);
    edges.add(edgeB);
    edges.add(edgeC);

    List<EPGMEdge> resultList = new ArrayList<>();
    Collector<EPGMEdge> collector = new ListCollector<>(resultList);

    reduce.reduce(edges, collector);

    assertEquals(resultList.size(), 2);
  }
}