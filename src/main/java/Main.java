import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.csv.CSVDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollectionFactory;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.s1ck.ldbc.LDBCToFlink;
import org.s1ck.ldbc.tuples.LDBCEdge;
import org.s1ck.ldbc.tuples.LDBCVertex;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

        LDBCToFlink ldbcToFlink = new LDBCToFlink(
                "/home/lulu/code/graph_collection_layout/data/social_network/combined", env);

        DataSet<LDBCEdge> edges = ldbcToFlink.getEdges();
        DataSet<LDBCVertex> vertices = ldbcToFlink.getVertices();

        List<LDBCVertex> vertexList = new ArrayList<>();
        List<LDBCEdge> edgeList = new ArrayList<>();

        edges.output(new LocalCollectionOutputFormat<>(edgeList));
        vertices.output(new LocalCollectionOutputFormat<>(vertexList));

        GraphCollectionFactory factory = new GraphCollectionFactory(cfg);
        DataSink sink = new CSVDataSink("out/", cfg);

        env.execute();
        System.out.println(vertexList);
    }
}
