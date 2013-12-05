/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import sdp.graph.Graph;
import sdp.graph.GraphInspector;
import sdp.graph.Node;
import sdp.io.GraphReader;

/**
 * Print statistics about a collection of graphs.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Analyzer {

    private int nGraphs;
    private int nNodes;
    private double avgNTopNodes;
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

        int nTopNodes = 0;
        int nEdgesFromTopNode = 0;
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
                nTopNodes++;
                nEdgesFromTopNode += node.getNOutgoingEdges();
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
            isSemiConnected = false;
        }
        avgIndegree /= (double) graph.getNNodes();
        avgOutdegree /= (double) graph.getNNodes();

        nGraphs++;
        nNodes += graph.getNNodes();

        avgNTopNodes += nTopNodes;
        avgNStructuralRoots += nTopNodes;
        pcSemiConnected += isSemiConnected ? 1.0 : 0.0;
        pcReentrant += isReentrant ? 1.0 : 0.0;
        pcCyclic += isCyclic ? 1.0 : 0.0;
        maxIndegreeGlobal = Math.max(maxIndegreeGlobal, maxIndegree);
        avgSingletons += nSingletons;

        System.out.format("%s", graph.id);
        // number of top nodes
        System.out.format("\t%d", nTopNodes);
        // number of outgoing arcs from top
        System.out.format("\t%d", nEdgesFromTopNode);
        // cyclic?
        System.out.format("\t%s", analyzer.isCyclic() ? "+" : "-");
        // semiconnected?
        System.out.format("\t%s", isSemiConnected ? "+" : "-");
        System.out.println();
    }

    public void finish() {
        avgIndegreeGlobal /= nNodes;
        avgOutdegreeGlobal /= nNodes;
        //
        avgNTopNodes /= nGraphs;
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
        }
        analyzer.finish();
        System.err.format("number of graphs: %d%n", analyzer.nGraphs);
        System.err.format("average number of top nodes per graph: %f%n", analyzer.avgNTopNodes);
        System.err.format("percentage of cyclic graphs: %f%n", analyzer.pcCyclic);
        System.err.format("percentage of semi-connected graphs: %f%n", analyzer.pcSemiConnected);
    }
}
