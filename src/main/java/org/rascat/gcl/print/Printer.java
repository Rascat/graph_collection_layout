package org.rascat.gcl.print;

import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class Printer {

    private int imageHeight;
    private int imageWidth;
    private String out;

    private String DEFAULT_IMG_FORMAT = "png";
    private int DEFAULT_RADIUS = 15;

    public Printer(int imageHeight, int imageWidth, String out) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.out = out;
    }

    public void printGraphCollection(GraphCollection collection) throws Exception {
        BufferedImage image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = image.createGraphics();
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setColor(Color.RED);

        for (EPGMVertex vertex : collection.getVertices().collect()) {
            int x = vertex.getPropertyValue("X").getInt();
            int y = vertex.getPropertyValue("Y").getInt();
            gfx.fill(this.createCircle(x, y, DEFAULT_RADIUS));

        }

        File file = new File(out);
        ImageIO.write(image, DEFAULT_IMG_FORMAT, file);
    }

    private Ellipse2D createCircle(double x, double y, double r) {
        return new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }
}
