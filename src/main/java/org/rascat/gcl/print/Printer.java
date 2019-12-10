package org.rascat.gcl.print;

import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.print.shapes.VertexShape;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Printer {
    public void printGraphCollection(GraphCollection collection) throws Exception {
        BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = image.createGraphics();
        gfx.setColor(Color.RED);

        for (EPGMVertex vertex : collection.getVertices().collect()) {
            gfx.fillOval(vertex.getPropertyValue("X").getInt() * 10, vertex.getPropertyValue("Y").getInt() * 10, 30, 30);
        }

        File file = new File("out/file.png");
        ImageIO.write(image, "png", file);
    }

    private static void printGraphCollection() throws IOException {
        int layoutWidth = 100;
        int layoutHeight = 100;

        BufferedImage image = new BufferedImage(layoutWidth * 10, layoutHeight * 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = image.createGraphics();
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setColor(Color.RED);

        gfx.setStroke(new BasicStroke(10));
        gfx.drawLine(0,0,image.getWidth(),image.getHeight());
        Point2D p1 = new Point(5,6);
        Point2D p2 = new Point(46, 87);
        Point2D p3 = new Point(80, 10);
        Point2D p4 = new Point(33, 69);

        gfx.fillOval(5 * 10,6 * 10,30,30);
        gfx.fillOval(46  * 10, 87 * 10,30,30);

        File file = new File("out/file.png");
        ImageIO.write(image, "png", file);
    }

    public static void main(String[] args) throws IOException {
        printGraphCollection();
    }
}
