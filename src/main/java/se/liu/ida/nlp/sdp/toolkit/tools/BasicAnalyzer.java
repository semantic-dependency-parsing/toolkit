/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.tools;

import se.liu.ida.nlp.sdp.toolkit.graph.Graph;
import se.liu.ida.nlp.sdp.toolkit.graph.InspectedGraph;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader2015;

/**
 * Print some basic statistics about a collection of graphs.
 *
 * @author Marco Kuhlmann
 */
public class BasicAnalyzer {

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            GraphReader reader = new GraphReader2015(arg);
            int nGraphs = 0;
            int nTokens = 0;
            int nCyclic = 0;
            int nForests = 0;
            int nTrees = 0;
            int nProjective = 0;
            int maxIndegree = 0;
            int maxOutdegree = 0;
            Graph graph;
            while ((graph = reader.readGraph()) != null) {
                InspectedGraph analyzer = new InspectedGraph(graph);
                nCyclic += analyzer.isCyclic() ? 1 : 0;
                nForests += analyzer.isForest() ? 1 : 0;
                nTrees += analyzer.isTree() ? 1 : 0;
                nProjective += analyzer.isProjective() ? 1 : 0;
                maxIndegree = Math.max(maxIndegree, analyzer.getMaximalIndegree());
                maxOutdegree = Math.max(maxOutdegree, analyzer.getMaximalOutdegree());
                nGraphs++;
                nTokens += graph.getNNodes() - 1;
            }
            reader.close();
            System.out.format("%s: %d graphs, %d tokens%n", arg, nGraphs, nTokens);
            System.out.format("  cyclic = %d%n", nCyclic);
            System.out.format("  forests = %d%n", nForests);
            System.out.format("  trees = %d%n", nTrees);
            System.out.format("  projective = %d%n", nProjective);
            System.out.format("  max indegree = %d%n", maxIndegree);
            System.out.format("  max outdegree = %d%n", maxOutdegree);
        }
    }
}
