/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.tools;

import se.liu.ida.nlp.sdp.graph.Graph;
import se.liu.ida.nlp.sdp.graph.GraphInspector;
import se.liu.ida.nlp.sdp.graph.Node;
import se.liu.ida.nlp.sdp.io.GraphReader;

/**
 * Print statistics about a collection of graphs.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Analyzer {

    private int nGraphs;
    private int nNodes;
    private double avgNRoots;
    private double avgNStructuralRoots;
    private double pcSemiConnected;
    private double pcReentrant;
    private double pcCyclic;
    private int maxIndegreeGlobal;
    private double avgIndegreeGlobal;
    private int maxOutdegreeGlobal;
    private double avgOutdegreeGlobal;
    private double avgSingletons;

    public void update(Graph graph) {
        GraphInspector analyzer = new GraphInspector(graph);

        int nRoots = 0;
        int nStructuralRoots = 0;
        boolean isSemiConnected = true;
        boolean isReentrant = false;
        boolean isCyclic = false;
        int maxIndegree = 0;
        double avgIndegree = 0.0;
        int maxOutdegree = 0;
        double avgOutdegree = 0.0;
        int nSingletons = 0;

        for (Node node : graph.getNodes()) {
            if (node.isTop) {
                nRoots++;
            }
            if (!node.hasIncomingEdges()) {
                nStructuralRoots++;
            }
            if (node.getNIncomingEdges() >= 2) {
                isReentrant = true;
            }
            if (analyzer.isCyclic()) {
                isCyclic = true;
            }
            maxIndegree = Math.max(maxIndegree, node.getNIncomingEdges());
            avgIndegree += node.getNIncomingEdges();
            maxOutdegree = Math.max(maxOutdegree, node.getNOutgoingEdges());
            avgOutdegree += node.getNOutgoingEdges();
            if (!node.hasIncomingEdges() && !node.hasOutgoingEdges()) {
                nSingletons++;
            }
            avgIndegreeGlobal += node.getNIncomingEdges();
            avgOutdegreeGlobal += node.getNOutgoingEdges();
        }
        if (analyzer.getNComponents() - nSingletons > 1) {
            System.err.format("%s%n", graph.id);
            isSemiConnected = false;
        }
        avgIndegree /= (double) graph.getNNodes();
        avgOutdegree /= (double) graph.getNNodes();

        nGraphs++;
        nNodes += graph.getNNodes();

        avgNRoots += nRoots;
        avgNStructuralRoots += nRoots;
        pcSemiConnected += isSemiConnected ? 1.0 : 0.0;
        pcReentrant += isReentrant ? 1.0 : 0.0;
        pcCyclic += isCyclic ? 1.0 : 0.0;
        maxIndegreeGlobal = Math.max(maxIndegreeGlobal, maxIndegree);
        avgSingletons += nSingletons;
    }

    public void finish() {
        avgIndegreeGlobal /= nNodes;
        avgOutdegreeGlobal /= nNodes;
        //
        avgNRoots /= nGraphs;
        avgNStructuralRoots /= nGraphs;
        pcSemiConnected /= nGraphs;
        pcReentrant /= nGraphs;
        pcCyclic /= nGraphs;
        avgSingletons /= nGraphs;
    }

    public static void main(String[] args) throws Exception {
        Analyzer analyzer = new Analyzer();
        for (String arg : args) {
            GraphReader reader = new GraphReader(arg);
            Graph graph;
            while ((graph = reader.readGraph()) != null) {
                analyzer.update(graph);
            }
            reader.close();
            analyzer.finish();
            System.out.format("%% semi-connected: %f%n", analyzer.pcSemiConnected);
//            System.out.format("%% cyclic: %f%n", analyzer.pcCyclic);
        }
    }
}
