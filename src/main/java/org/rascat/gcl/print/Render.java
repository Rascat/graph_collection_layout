package org.rascat.gcl.print;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Render implements DataSink {

    private int imageHeight;
    private int imageWidth;
    private String out;

    private String DEFAULT_IMG_FORMAT = "png";
    private int DEFAULT_RADIUS = 15;

    public Render(int imageHeight, int imageWidth, String out) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.out = out;
    }

    public void renderGraphCollection(GraphCollection collection) throws Exception {
        BufferedImage image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = image.createGraphics();
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setColor(Color.RED);
        DataSet<EPGMEdge> preparedEdges = this.prepareEdges(collection.getVertices(), collection.getEdges());

        List<EPGMVertex> vertices = new ArrayList<>();
        collection.getVertices().output(new LocalCollectionOutputFormat<>(vertices));

        for (EPGMVertex vertex : vertices) {
            int x = vertex.getPropertyValue("X").getInt();
            int y = vertex.getPropertyValue("Y").getInt();
            System.out.println(String.format("vx: %d, vy: %d", x, y));
            gfx.fill(this.createCircle(x, y, DEFAULT_RADIUS));
        }

        List<EPGMEdge> edges = new ArrayList<>();
        preparedEdges.output(new LocalCollectionOutputFormat<>(edges));

        gfx.setStroke(new BasicStroke(1));
        gfx.setColor(Color.BLACK);
        for (EPGMEdge edge : edges) {
            int sourceX = edge.getPropertyValue("source_x").getInt();
            int sourceY = edge.getPropertyValue("source_y").getInt();
            int targetX = edge.getPropertyValue("target_x").getInt();
            int targetY = edge.getPropertyValue("target_y").getInt();

            System.out.println(String.format("sx: %d, sy: %d, tx: %d, ty: %d", sourceX, sourceY, targetX, targetY));

            gfx.draw(new Line2D.Double(sourceX, sourceY, targetX, targetY));
        }

        File file = new File(out);
        ImageIO.write(image, DEFAULT_IMG_FORMAT, file);
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
          .with(new JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge>() {
              public EPGMEdge join(EPGMEdge first, EPGMVertex second) throws Exception {
                  first.setProperty("source_x", second.getPropertyValue("X"));
                  first.setProperty("source_y", second.getPropertyValue("Y"));
                  return first;
              }
          }).join(vertices).where("targetId").equalTo("id")
          .with(new JoinFunction<EPGMEdge, EPGMVertex, EPGMEdge>() {
              public EPGMEdge join(EPGMEdge first, EPGMVertex second) throws Exception {
                  first.setProperty("target_x", second.getPropertyValue("X"));
                  first.setProperty("target_y", second.getPropertyValue("Y"));
                  return first;
              }
          });
        return edges;
    }

    @Override
    public void write(LogicalGraph logicalGraph) throws IOException {
        throw new UnsupportedOperationException("Plotting is not supported for GraphCollections");
    }

    @Override
    public void write(GraphCollection graphCollection) throws IOException {
        try {
            renderGraphCollection(graphCollection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(LogicalGraph logicalGraph, boolean b) throws IOException {
        throw new UnsupportedOperationException("Plotting is not supported for GraphCollections");
    }

    @Override
    public void write(GraphCollection graphCollection, boolean b) throws IOException {
        try {
            renderGraphCollection(graphCollection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
