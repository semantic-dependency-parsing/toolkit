/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.tools;

import se.liu.ida.nlp.sdp.graph.Graph;
import se.liu.ida.nlp.sdp.io.GraphReader;
import se.liu.ida.nlp.sdp.io.GraphWriter;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class IOTest {

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            GraphReader reader = new GraphReader(arg);
            GraphWriter writer = new GraphWriter(arg + ".out");
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
