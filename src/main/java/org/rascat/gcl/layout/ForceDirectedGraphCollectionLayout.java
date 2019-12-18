package org.rascat.gcl.layout;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.core.fs.FileSystem;
import org.gradoop.common.model.api.entities.GraphHead;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.rascat.gcl.functions.*;
import org.rascat.gcl.model.Force;

import static org.rascat.gcl.functions.TransferPosition.Position.SOURCE;
import static org.rascat.gcl.functions.TransferPosition.Position.TARGET;

public class ForceDirectedGraphCollectionLayout {

    private int width;
    private int height;
    private double k;
    private int iterations = 1;

    public ForceDirectedGraphCollectionLayout(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public GraphCollection execute(GraphCollection collection) {
        return execute(collection, 20);
    }

    public GraphCollection execute(GraphCollection collection, int vertexCount) {
        this.k = Math.sqrt((double) area() / vertexCount);
        DataSet<EPGMEdge> edges = collection.getEdges();
        DataSet<EPGMVertex> vertices = collection.getVertices();
        DataSet<EPGMGraphHead> graphHeads = collection.getGraphHeads();

        vertices = vertices.map(new RandomPlacement(width, height));

        DataSet<Force> repulsiveForces =
          vertices.cross(vertices).with(new ComputeRepulsiveForces(k));
        DataSet<Force> repulsiveForcesById = repulsiveForces.groupBy("f0").reduce(new SumForces());

        DataSet<EPGMEdge> positionedEdges = edges.join(vertices)
          .where("sourceId").equalTo("id").with(new TransferPosition(SOURCE))
          .join(vertices)
          .where("targetId").equalTo("id").with(new TransferPosition(TARGET));

        DataSet<Force> attractingForces = positionedEdges.map(new ComputeAttractingForces(k));
        DataSet<Force> attractingForcesById = attractingForces.groupBy("f0").reduce(new SubtractForces());

        DataSet<Force> resultingForces = repulsiveForcesById.union(attractingForces).groupBy("f0").reduce(new SubtractForces());

        vertices = vertices.join(resultingForces).where("id").equalTo("f0").with(new ApplyForces(width / 10, width, height));

        repulsiveForces.writeAsText("out/displacements", FileSystem.WriteMode.OVERWRITE);
        repulsiveForcesById.writeAsText("out/dispByVertex", FileSystem.WriteMode.OVERWRITE);
        attractingForces.writeAsText("out/attractingForces", FileSystem.WriteMode.OVERWRITE);
        attractingForcesById.writeAsText("out/attrForcesById", FileSystem.WriteMode.OVERWRITE);
        resultingForces.writeAsText("out/resultingForces", FileSystem.WriteMode.OVERWRITE);

        return collection.getFactory().fromDataSets(graphHeads, vertices, edges);
    }

    private int area() {
        return this.height * this.width;
    }
}
