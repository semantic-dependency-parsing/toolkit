/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.tools;

import se.liu.ida.nlp.sdp.toolkit.graph.Graph;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader2015;
import se.liu.ida.nlp.sdp.toolkit.io.GraphWriter;
import se.liu.ida.nlp.sdp.toolkit.io.GraphWriter2015;

/**
 * Test the graph I/O.
 *
 * @author Marco Kuhlmann
 */
public class IOTest {

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            GraphReader reader = new GraphReader2015(arg);
            GraphWriter writer = new GraphWriter2015(arg + ".out");
            int nGraphs = 0;
            int nTokens = 0;
            Graph graph;
            while ((graph = reader.readGraph()) != null) {
                nGraphs++;
                nTokens += graph.getNNodes() - 1;
                writer.writeGraph(graph);
            }
            reader.close();
            writer.close();
            System.out.format("%s: %d graphs, %d tokens%n", arg, nGraphs, nTokens);
        }
    }
}
