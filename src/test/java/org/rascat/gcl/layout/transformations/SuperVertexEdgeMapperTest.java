package org.rascat.gcl.layout.transformations;

import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.id.GradoopIdSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMEdgeFactory;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMVertexFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class SuperVertexEdgeMapperTest {

  private SuperVertexEdgeMapper mapper;
  private EPGMVertexFactory vertexFactory;
  private EPGMEdgeFactory edgeFactory;

  private GradoopId graphIdA = GradoopId.fromString("AAAAAAAAAAAAAAAAAAAAAAAA");
  private GradoopId graphIdB = GradoopId.fromString("CCCCCCCCCCCCCCCCCCCCCCCC");
  private GradoopId graphIdC = GradoopId.fromString("CCCCCCCCCCCCCCCCCCCCCCCC");

  private EPGMVertex source;
  private EPGMVertex target;
  private EPGMEdge edge;
  private Collector<EPGMEdge> collector;
  private List<EPGMEdge> resultList;

  @BeforeTest
  public void setUp() {
    this.mapper = new SuperVertexEdgeMapper();
    this.vertexFactory = new EPGMVertexFactory();
    this.edgeFactory = new EPGMEdgeFactory();
  }

  @BeforeMethod
  public void setUpElements() {
    this.source = vertexFactory.createVertex("source", new GradoopIdSet());
    this.target = vertexFactory.createVertex("target", new GradoopIdSet());
    this.edge = edgeFactory.createEdge(source.getId(), target.getId());
    this.resultList = new ArrayList<>();
    this.collector = new ListCollector<>(resultList);
  }

  @Test
  public void testJoinWithSeparatedVertices() {
    source.getGraphIds().add(graphIdA);
    source.getGraphIds().add(graphIdC);
    target.getGraphIds().add(graphIdB);

    mapper.join(new Tuple2<>(source, edge), target, collector);

    EPGMEdge result = resultList.get(0);
    assertEquals(graphIdA, result.getSourceId());
    assertEquals(graphIdB, result.getTargetId());
  }

  @Test
  public void testJoinWithSameGraphVertices() {
    source.getGraphIds().add(graphIdA);
    target.getGraphIds().add(graphIdA);

    mapper.join(new Tuple2<>(source, edge), target, collector);

    assertTrue(resultList.isEmpty());
  }

  @Test
  public void testJoinWithPartiallyOverlappingVertices() {
    source.getGraphIds().add(graphIdA);
    target.getGraphIds().add(graphIdA);
    target.getGraphIds().add(graphIdC);

    mapper.join(new Tuple2<>(source, edge), target, collector);

    assertTrue(resultList.isEmpty());
  }
}