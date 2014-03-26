/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import sdp.graph.Graph;
import sdp.io.GraphReader;
import sdp.io.GraphWriter;

/**
 * Splits the SDP training data into training and development.
 * 
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Splitter {

    public static void main(String[] args) throws Exception {
        GraphReader reader = new GraphReader(args[0]);
        GraphWriter writerTrain = new GraphWriter(args[1]);
        GraphWriter writerDevel = new GraphWriter(args[2]);
        Graph graph;
        while ((graph = reader.readGraph()) != null) {
            if (graph.id.substring(2, 4).equals("20")) {
                writerDevel.writeGraph(graph);
            } else {
                writerTrain.writeGraph(graph);
            }
        }
        reader.close();
        writerTrain.close();
        writerDevel.close();
    }
}
