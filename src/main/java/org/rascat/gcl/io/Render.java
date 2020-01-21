package org.rascat.gcl.io;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.layout.functions.prepare.TransferPosition;
import org.rascat.gcl.layout.model.VertexType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.rascat.gcl.layout.model.VertexType.TAIL;
import static org.rascat.gcl.layout.model.VertexType.HEAD;
import static org.rascat.gcl.layout.AbstractGraphCollectionLayout.*;

public class Render {

    private int imageHeight;
    private int imageWidth;
    private String out;

    private String DEFAULT_IMG_FORMAT = "png";
    private int DEFAULT_RADIUS = 15;
    private float DEFAULT_STROKE_WIDTH = 2F;

    private Color VERTEX_COLOR = Color.RED;
    private Color EDGE_COLOR = Color.BLACK;

    public Render(int imageHeight, int imageWidth, String out) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.out = out;
    }

    public void renderGraphCollection(GraphCollection collection, ExecutionEnvironment env) throws Exception {
        BufferedImage image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = image.createGraphics();
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        DataSet<EPGMEdge> preparedEdges = this.prepareEdges(collection.getVertices(), collection.getEdges());

        List<EPGMVertex> vertices = new ArrayList<>();
        collection.getVertices().output(new LocalCollectionOutputFormat<>(vertices));
        List<EPGMEdge> edges = new ArrayList<>();
        preparedEdges.output(new LocalCollectionOutputFormat<>(edges));

        env.execute();

        drawEdges(edges, gfx);
        drawVertices(vertices, gfx);

        File file = new File(out);
        ImageIO.write(image, DEFAULT_IMG_FORMAT, file);
    }

    private void drawEdges(Collection<EPGMEdge> edges, Graphics2D gfx) {
        gfx.setStroke(new BasicStroke(DEFAULT_STROKE_WIDTH));
        gfx.setColor(EDGE_COLOR);
        for (EPGMEdge edge : edges) {
            double sourceX = edge.getPropertyValue(TAIL.getKeyX()).getDouble();
            double sourceY = edge.getPropertyValue(TAIL.getKeyY()).getDouble();
            double targetX = edge.getPropertyValue(HEAD.getKeyX()).getDouble();
            double targetY = edge.getPropertyValue(HEAD.getKeyY()).getDouble();

            gfx.draw(new Line2D.Double(sourceX, sourceY, targetX, targetY));
        }
    }

    private void drawVertices(Collection<EPGMVertex> vertices, Graphics2D gfx) {

        for (EPGMVertex vertex : vertices) {
            gfx.setColor(VERTEX_COLOR);
            double x = vertex.getPropertyValue(KEY_X_COORD).getDouble();
            double y = vertex.getPropertyValue(KEY_Y_COORD).getDouble();
            gfx.fill(this.createCircle(x, y, DEFAULT_RADIUS));

            String label = vertex.getLabel().equals("") ? "X" : vertex.getLabel();
            gfx.setColor(Color.BLACK);
            gfx.drawString(label,(float) (x - label.length() * 4), (float) y + 5);
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
          .with(new TransferPosition(TAIL)
          ).join(vertices).where("targetId").equalTo("id")
          .with(new TransferPosition(VertexType.HEAD));
        return edges;
    }
}
