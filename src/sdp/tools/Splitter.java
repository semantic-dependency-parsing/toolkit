/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import sdp.graph.Graph;
import sdp.io.GraphReader2014;
import sdp.io.GraphWriter2014;

/**
 * Splits the SDP training data into training and development.
 * 
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Splitter {

    public static void main(String[] args) throws Exception {
        GraphReader2014 reader = new GraphReader2014(args[0]);
        GraphWriter2014 writerTrain = new GraphWriter2014(args[1]);
        GraphWriter2014 writerDevel = new GraphWriter2014(args[2]);
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
