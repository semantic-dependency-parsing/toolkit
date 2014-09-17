/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.graph;

/**
 * Inspect graph-theoretic properties.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class InspectedGraph {

	/**
	 * The analyzed graph.
	 */
	private final Graph graph;

	/**
	 * The number of non-wall node of this graph.
	 */
	private final int nNonWallNodes;

	/**
	 * DFS of the graph.
	 */
	private final DFS directedDFS;

	/**
	 * DFS of the undirected graph.
	 */
	private final DFS undirectedDFS;

	/**
	 * Flags indicating whether a node is a singleton.
	 */
	private final boolean[] isSingleton;

	/**
	 * The number of singleton nodes in this graph.
	 */
	private final int nSingletons;

	/**
	 * Construct a new inspector for the specified graph.
	 *
	 * @param graph the graph to be inspected
	 */
	public InspectedGraph(Graph graph) {
		this.graph = graph;

		int nNodes = graph.getNNodes();
		this.nNonWallNodes = nNodes - 1;
		this.isSingleton = new boolean[nNodes];
		this.nSingletons = computeSingletons();

		this.directedDFS = new DFS(graph);
		this.undirectedDFS = new DFS(graph, true);
	}

	/**
	 * Returns the number of non-wall nodes of the inspected graph.
	 *
	 * @return the number of non-wall nodes of the inspected graph
	 */
	public int getNNonWallNodes() {
		return nNonWallNodes;
	}

	/**
	 * Returns the number of weakly connected components of the inspected graph.
	 *
	 * @return The number of weakly connected components of the inspected graph
	 */
	public int getNComponents() {
		return undirectedDFS.getNRuns();
	}

	/**
	 * Tests whether the inspected graph contains a cycle.
	 *
	 * @return {@code true} if and only if the inspected graph contains a cycle
	 */
	public boolean isCyclic() {
		return directedDFS.isCyclic();
	}

	/**
	 * Computes flags indicating whether a node is a singleton.
	 */
	private int computeSingletons() {
		int n = 0;
		for (Node node : graph.getNodes()) {
			if (node.id != 0 && !node.hasIncomingEdges() && !node.hasOutgoingEdges() && !node.isTop) {
				isSingleton[node.id] = true;
				n++;
			}
		}
		return n;
	}

	/**
	 * Tests whether the specified node is a singleton.
	 *
	 * @param id a node id
	 * @return {@code true} if the specified node is a singleton
	 */
	public boolean isSingleton(int id) {
		return isSingleton[id];
	}

	/**
	 * Returns the number of singleton nodes of this graph. A node is a
	 * singleton if it has no neighbors and is not a top node.
	 *
	 * @return the number of singleton nodes of this graph
	 */
	public int getNSingletons() {
		return nSingletons;
	}

	/**
	 * Computes the maximal indegree of the nodes in the inspected graph.
	 *
	 * @return the maximal indegree of the nodes in the inspected graph
	 */
	public int getMaximalIndegree() {
		int max = 0;
		for (Node node : graph.getNodes()) {
			max = Math.max(max, node.getNIncomingEdges());
		}
		return max;
	}

	/**
	 * Computes the maximal outdegree of the nodes in the inspected graph.
	 *
	 * @return the maximal outdegree of the nodes in the inspected graph
	 */
	public int getMaximalOutdegree() {
		int max = 0;
		for (Node node : graph.getNodes()) {
			max = Math.max(max, node.getNOutgoingEdges());
		}
		return max;
	}

	/**
	 * Returns the number of root nodes in the inspected graph. A
	 * <em>root node</em> is a node without incoming edges. The wall node is not
	 * considered to be a root node.
	 *
	 * @return the number of root nodes in the inspected graph
	 */
	public int getNRootNodes() {
		int nRootNodes = 0;
		for (Node node : graph.getNodes()) {
			nRootNodes += node.hasIncomingEdges() ? 0 : 1;
		}
		return nRootNodes - 1; // the wall node
	}

	/**
	 * Returns the number of leaf nodes in the inspected graph. A
	 * <em>leaf node</em> is a node without outgoing edges. The wall node is not
	 * considered to be a leaf node.
	 *
	 * @return the number of leaf nodes in the inspected graph
	 */
	public int getNLeafNodes() {
		int nLeafNodes = 0;
		for (Node node : graph.getNodes()) {
			nLeafNodes += node.hasOutgoingEdges() ? 0 : 1;
		}
		return nLeafNodes - 1; // the wall node
	}

	/**
	 * Tests whether the inspected graph is a forest. A forest is an acyclic
	 * graph in which every node has at most one incoming edge.
	 *
	 * @return {@code true} if and only if the inspected graph is a forest
	 */
	public boolean isForest() {
		return !isCyclic() && getMaximalIndegree() <= 1;
	}

	/**
	 * Tests whether the inspected graph is a tree. A tree is a forest with
	 * exactly one root node.
	 *
	 * @return {@code true} if and only if the inspected graph is a tree
	 */
	public boolean isTree() {
		return isForest() && getNRootNodes() - getNSingletons() == 1;
	}

	/**
	 * Tests whether the inspected graph is noncrossing. A graph is noncrossing
	 * if there are no overlapping edges.
	 *
	 * @return {@code true} if and only if the inspected graph is noncrossing
	 */
	public boolean isNoncrossing() {
		for (Edge edge1 : graph.getEdges()) {
			int min1 = Math.min(edge1.source, edge1.target);
			int max1 = Math.max(edge1.source, edge1.target);
			for (Edge edge2 : graph.getEdges()) {
				int min2 = Math.min(edge2.source, edge2.target);
				int max2 = Math.max(edge2.source, edge2.target);
				if (overlap(min1, max1, min2, max2)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Tests whether the specified edges overlap (cross).
	 *
	 * @param min1 the position of the left node of the first edge
	 * @param max1 the position of the right node of the first edge
	 * @param min2 the position of the left node of the second edge
	 * @param max2 the position of the right node of the second edge
	 * @return {@code true} if and only if the specified edges overlap
	 */
	private static boolean overlap(int min1, int max1, int min2, int max2) {
		return min1 < min2 && min2 < max1 && max1 < max2 || min2 < min1 && min1 < max2 && max2 < max1;
	}

	/**
	 * Tests whether the inspected graph is projective. A graph is projective if
	 * it is noncrossing and there are no covered roots. In the context of
	 * semantic dependency graphs, a <em>root</em> is defined as a non-singleton
	 * node without incoming edges.
	 *
	 * @return {@code true} if and only if the inspected graph is projective
	 */
	public boolean isProjective() {
		if (!isNoncrossing()) {
			return false;
		} else {
			for (Edge edge : graph.getEdges()) {
				int min = Math.min(edge.source, edge.target);
				int max = Math.max(edge.source, edge.target);
				for (int i = min + 1; i < max; i++) {
					Node node = graph.getNode(i);
					if (!isSingleton(i) && !node.hasIncomingEdges()) {
						return false;
					}
				}
			}
			return true;
		}
	}
}
