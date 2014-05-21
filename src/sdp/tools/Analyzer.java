/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import java.util.HashSet;
import java.util.Set;
import sdp.graph.Edge;
import sdp.graph.Graph;
import sdp.graph.InspectedGraph;
import sdp.graph.Node;
import sdp.io.GraphReader;

/**
 * Print statistics about a collection of graphs.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Analyzer {

    // The number of graphs read.
    private int nGraphs;

    // Number of labels seen.
    private final Set<String> labels = new HashSet<String>();

    // The number of nodes seen (excluding wall nodes).
    private int nNodes;

    // Counter to compute the average number of top nodes per graph.
    private double avgNTopNodes;

    // Counter to compute the average number of structutral roots per graph.
    private double avgNStructuralRoots;

    // Counter to compute the percentage of semi-connected graphs.
    private double pcSemiConnected;

    // Counter to compute the percentage of non-singleton nodes that are reentrant.
    private double pcReentrant;

    // Counter to compute the number of cyclic graphs.
    private double pcCyclic;

    // Counter to compute the maximal indegree of a node.
    private int maxIndegreeGlobal;

    // Counter to compute the average indegree per node.
    private double avgIndegreeGlobal;

    // Counter to compute the maximal outdegree of a node.
    private int maxOutdegreeGlobal;

    // Counter to compute the average outdegree per node.
    private double avgOutdegreeGlobal;

    // Counter to compute the average number of singletons per graph.
    private double avgSingletons;

    // Counter to compute the percentage of singleton nodes.
    private double pcSingletons;

    // Counter to compute the percentage of topless graphs.
    private double pcToplessGraphs;

    // Counter to compute the percentage of graphs that are forests.
    private double pcForests;

    // Counter to compute the percentage of graphs that are trees.
    private double pcTrees;

    // Counter to compute the edge density.
    private double edgeDensity;

    // Counter to compute the percentage of (non-singleton) nodes with in-degree = 0 that are not top.
    private double pcSpecial;

    /**
     * Updates the statistics with the specified graph.
     *
     * @param graph a semantic dependency graph
     */
    public void update(Graph graph) {
	InspectedGraph inspectedGraph = new InspectedGraph(graph);

	int nTopNodes = 0;
	int nEdgesFromTopNode = 0;
	int nStructuralRoots = 0;
	boolean isReentrant = false;
	boolean isCyclic = false;
	boolean isSemiConnected = true;
	int maxIndegree = 0;
	double avgIndegree = 0.0;
	int maxOutdegree = 0;
	double avgOutdegree = 0.0;
	int nSingletons = inspectedGraph.getNSingletons();

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
	    if (!inspectedGraph.isSingleton(node.id)) {
		maxIndegree = Math.max(maxIndegree, node.getNIncomingEdges());
		avgIndegree += node.getNIncomingEdges();
		maxOutdegree = Math.max(maxOutdegree, node.getNOutgoingEdges());
		avgOutdegree += node.getNOutgoingEdges();
		avgIndegreeGlobal += node.getNIncomingEdges();
		avgOutdegreeGlobal += node.getNOutgoingEdges();
		pcSpecial += (!node.hasIncomingEdges() && !node.isTop) ? 1 : 0;
	    }
	}
	if (inspectedGraph.isCyclic()) {
	    isCyclic = true;
	}
	if (inspectedGraph.getNComponents() > 1) {
	    isSemiConnected = false;
	}
	avgIndegree /= (double) graph.getNNodes();
	avgOutdegree /= (double) graph.getNNodes();

	nGraphs++;
	nNodes += graph.getNNodes() - 1;

	avgNTopNodes += nTopNodes;
	avgNStructuralRoots += nTopNodes;
	pcSemiConnected += isSemiConnected ? 1.0 : 0.0;
	pcReentrant += isReentrant ? 1.0 : 0.0;
	pcCyclic += isCyclic ? 1.0 : 0.0;
	maxIndegreeGlobal = Math.max(maxIndegreeGlobal, maxIndegree);
	avgSingletons += nSingletons;
	pcSingletons += nSingletons;
	pcToplessGraphs += nTopNodes == 0 ? 1.0 : 0.0;
	pcForests += inspectedGraph.isForest() ? 1.0 : 0.0;
	pcTrees += inspectedGraph.isTree() ? 1.0 : 0.0;
	edgeDensity += graph.getNEdges();

	for (Edge edge : graph.getEdges()) {
	    labels.add(edge.label);
	}

	// Print graph-specific statistics.
	System.out.format("%s", graph.id);
	// number of top nodes
	System.out.format("\t%d", nTopNodes);
	// number of outgoing arcs from top
	System.out.format("\t%d", nEdgesFromTopNode);
	// cyclic?
	System.out.format("\t%s", inspectedGraph.isCyclic() ? "+" : "-");
	// semiconnected?
	System.out.format("\t%s", isSemiConnected ? "+" : "-");
	System.out.println();
    }

    /**
     * Finalizes the computation of the statistics.
     */
    public void finish() {
	double nNonSingletonNodes = nNodes - pcSingletons;
	//
	pcSingletons /= nNodes;
	//
	avgIndegreeGlobal /= nNonSingletonNodes;
	avgOutdegreeGlobal /= nNonSingletonNodes;
	pcReentrant /= nNonSingletonNodes;
	pcSpecial /= nNonSingletonNodes;
	edgeDensity /= nNonSingletonNodes;
	//
	avgNTopNodes /= nGraphs;
	avgNStructuralRoots /= nGraphs;
	pcSemiConnected /= nGraphs;
	pcCyclic /= nGraphs;
	avgSingletons /= nGraphs;
	pcToplessGraphs /= nGraphs;
	pcForests /= nGraphs;
	pcTrees /= nGraphs;
    }

    /**
     * Prints statistics about a set of graphs.
     *
     * @param args names of files from which to read graphs
     * @throws Exception if an I/O exception occurs
     */
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
	System.err.format("number of distinct labels: %d%n", analyzer.labels.size());
	System.err.format("percentage of singleton nodes (over all nodes): %f%n", analyzer.pcSingletons);
	System.err.format("edge density (proportion of edge counts to non-singleton nodes): %f%n", analyzer.edgeDensity);
	System.err.format("percentage of cyclic graphs: %f%n", analyzer.pcCyclic);
	System.err.format("percentage of graphs that are forests: %f%n", analyzer.pcForests);
	System.err.format("percentage of graphs that are semi-connected: %f%n", analyzer.pcSemiConnected);
	System.err.format("percentage of (non-singleton) nodes that are reentrant: %f%n", analyzer.pcReentrant);
	System.err.format("percentage of topless graphs: %f%n", analyzer.pcToplessGraphs);
	System.err.format("average number of top nodes per graph: %f%n", analyzer.avgNTopNodes);
	System.err.format("percentage of (non-singleton) nodes with indegree = 0 that are not top: %f%n", analyzer.pcSpecial);
    }
}
