package org.rascat.gcl.layout.transformations;

import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMElement;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.properties.Properties;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class VertexWithSameGraphIdMapperTest {

  private VertexWithSameGraphIdMapper<EPGMElement> mapper;

  @BeforeMethod
  public void setUp() {
    this.mapper = new VertexWithSameGraphIdMapper<>();
  }

  @Test
  public void testMap() {
    EPGMGraphHead head = new EPGMGraphHead(
        GradoopId.fromString("1AB363914FD1325CC43790AB"), "TEST_GRAPH", Properties.create());
    EPGMVertex vertex = mapper.map(head);

    assertEquals(vertex.getId(), head.getId());
  }
}