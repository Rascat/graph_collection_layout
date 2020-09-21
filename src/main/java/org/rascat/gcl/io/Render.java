package org.rascat.gcl.io;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.gradoop.common.model.api.entities.GraphHead;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.layout.functions.prepare.TransferPosition;
import org.rascat.gcl.layout.functions.select.SelectFirstGraphId;
import org.rascat.gcl.layout.model.VertexType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

import static java.awt.RenderingHints.*;
import static org.rascat.gcl.layout.model.VertexType.TAIL;
import static org.rascat.gcl.layout.model.VertexType.HEAD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class Render {

  private int imageWidth;
  private int imageHeight;
  private String out;

  private final String DEFAULT_IMG_FORMAT = "png";
  private final int DEFAULT_RADIUS = 15;
  private final float DEFAULT_STROKE_WIDTH = 2F;

  private final Color COLOR_PRIMARY = Color.RED;
  private final Color COLOR_SECONDARY = Color.BLACK;

  private boolean assignColorToGraph;
  private Map<GradoopId, Color> graphColorMap;

  public Render(int layoutWidth, int layoutHeight, String out) {
    this(layoutWidth, layoutHeight, out, false);
  }

  public Render(int layoutWidth, int layoutHeight, String out, boolean assignColorToGraph) {
    this.imageWidth = layoutWidth + (2 * DEFAULT_RADIUS) + (int)(2 * DEFAULT_STROKE_WIDTH);
    this.imageHeight = layoutHeight + (2 * DEFAULT_RADIUS) + (int)(2 * DEFAULT_STROKE_WIDTH);
    this.out = out;
    this.assignColorToGraph = assignColorToGraph;
  }

  public void renderGraphCollection(GraphCollection collection, ExecutionEnvironment env) throws Exception {
    BufferedImage image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gfx = image.createGraphics();
    gfx.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
    gfx.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
    gfx.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
    gfx.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);

    DataSet<EPGMEdge> preparedEdges = this.prepareEdges(collection.getVertices(), collection.getEdges());

    List<EPGMVertex> vertices = new ArrayList<>();
    collection.getVertices().output(new LocalCollectionOutputFormat<>(vertices));
    List<EPGMEdge> edges = new ArrayList<>();
    preparedEdges.output(new LocalCollectionOutputFormat<>(edges));
    List<EPGMGraphHead> graphHeads = new ArrayList<>();
    collection.getGraphHeads().output(new LocalCollectionOutputFormat<>(graphHeads));

    env.execute();

    if (assignColorToGraph) {
      this.graphColorMap = new HashMap<>();
      List<Color> colors = getColorPalette();
      int colorsSize = colors.size();

      if (graphHeads.size() > colorsSize) {
        throw new RuntimeException("Not enough colors for graph count: " + graphHeads.size());
      }

      Random random = new Random();
      List<Integer> numberCache = new ArrayList<>();
      int randomInt;

      for (GraphHead graphHead : graphHeads) {
        do {
          randomInt = random.nextInt(colorsSize);
        } while (numberCache.contains(randomInt));

        numberCache.add(randomInt);
        Color selectedColor = colors.get(randomInt);
        graphColorMap.put(graphHead.getId(), selectedColor);
      }
    }

    drawEdges(edges, gfx);
    drawVertices(vertices, gfx);

    File file = new File(out);
    ImageIO.write(image, DEFAULT_IMG_FORMAT, file);
  }

  private void drawEdges(Collection<EPGMEdge> edges, Graphics2D gfx) {
    gfx.setStroke(new BasicStroke(DEFAULT_STROKE_WIDTH));
    gfx.setColor(COLOR_SECONDARY);
    for (EPGMEdge edge : edges) {
      double sourceX = edge.getPropertyValue(TAIL.getKeyX()).getDouble();
      double sourceY = edge.getPropertyValue(TAIL.getKeyY()).getDouble();
      double targetX = edge.getPropertyValue(HEAD.getKeyX()).getDouble();
      double targetY = edge.getPropertyValue(HEAD.getKeyY()).getDouble();

      sourceX = shiftCoord(sourceX);
      sourceY = shiftCoord(sourceY);
      targetX = shiftCoord(targetX);
      targetY = shiftCoord(targetY);

      gfx.draw(new Line2D.Double(sourceX, sourceY, targetX, targetY));
    }
  }

  private void drawVertices(Collection<EPGMVertex> vertices, Graphics2D gfx) {

    for (EPGMVertex vertex : vertices) {
      if (assignColorToGraph) {
        SelectFirstGraphId<EPGMVertex> select = new SelectFirstGraphId<>();
        GradoopId firstGraphId = select.getKey(vertex);

        gfx.setColor(graphColorMap.get(firstGraphId));
      } else {
        gfx.setColor(COLOR_PRIMARY);
      }

      double x = vertex.getPropertyValue(KEY_X_COORD).getDouble();
      double y = vertex.getPropertyValue(KEY_Y_COORD).getDouble();
      x = shiftCoord(x);
      y = shiftCoord(y);

      gfx.fill(this.createCircle(x, y, DEFAULT_RADIUS));

      gfx.setColor(Color.BLACK);
      gfx.draw(this.createCircle(x, y, DEFAULT_RADIUS));

//            String label = vertex.getLabel().equals("") ? "X" : vertex.getLabel();
//            gfx.setColor(Color.BLACK);
//            gfx.drawString(label,(float) (x - label.length() * 4), (float) y + 5);
    }
  }

  private Ellipse2D createCircle(double x, double y, double r) {
    return new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
  }

  /**
   * Prepare the given edges for drawing. Assign them start- and end-coordinates from their
   * vertices.
   *
   * @param vertices The vertices to take the edge-coordinates from
   * @param edges    The raw edges
   * @return The prepared edges
   */
  private DataSet<EPGMEdge> prepareEdges(DataSet<EPGMVertex> vertices, DataSet<EPGMEdge> edges) {
    edges = edges.join(vertices).where("sourceId").equalTo("id")
      .with(new TransferPosition(TAIL))
      .join(vertices).where("targetId").equalTo("id")
      .with(new TransferPosition(VertexType.HEAD));
    return edges;
  }

  private double shiftCoord(double coord) {
    return coord + DEFAULT_RADIUS + DEFAULT_STROKE_WIDTH;
  }

  /**
   * Returns the crayola "Star Brite" color palette (https://en.wikipedia.org/wiki/List_of_Crayola_crayon_colors)
   *
   * @return list of colors
   */
  public static List<Color> getColorPalette() {
    Color color0 = new Color(248, 152, 200);
    Color color1 = new Color(233, 30, 99);
    Color color2 = new Color(214, 37, 24);
    Color color3 = new Color(173, 0, 0);
    Color color4 = new Color(250, 122, 0);
    Color color5 = new Color(205, 220, 57);
    Color color6 = new Color(0, 216, 160);
    Color color7 = new Color(27, 167, 123);
    Color color8 = new Color(0, 76, 113);
    Color color9 = new Color(26, 173, 224);
    Color color10 = new Color(0, 105, 189);
    Color color11 = new Color(51, 51, 153);
    Color color12 = new Color(86, 65, 140);
    Color color13 = new Color(21, 23, 21); // probably too dark
    Color color14 = new Color(230, 51, 0);
    Color color15 = new Color(222, 105, 0);
    Color color16 = Color.decode("0xFFCBA4");
    Color color17 = Color.decode("0xC9A0DC");
    Color color18 = Color.decode("0x0066FF");
    Color color19 = Color.decode("0xFFFF99");
    Color color20 = Color.decode("0xC5E17A");
    Color color21 = Color.decode("0xC154C1");

    return Arrays.asList(color0, color1, color2, color3, color4, color5, color6, color7, color8,
      color9, color10, color11, color12, color14, color15, color16, color17, color18, color19, color20, color21);
  }
}
