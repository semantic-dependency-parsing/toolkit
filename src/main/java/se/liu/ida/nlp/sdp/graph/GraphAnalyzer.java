/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.graph;

/**
 * Test for several graph-theoretic properties.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class GraphAnalyzer {

    /**
     * The analyzed graph.
     */
    private final Graph graph;
    /**
     * The preorder timestamps of each node.
     */
    private final int[] enter;
    /**
     * The postorder timestamps of each node.
     */
    private final int[] leave;

    /**
     * Construct a new analyzer for the specified graph.
     *
     * @param graph the graph to be analyzed
     */
    public GraphAnalyzer(Graph graph) {
        this.graph = graph;

        int nNodes = graph.getNNodes();

        this.enter = new int[nNodes];
        this.leave = new int[nNodes];
        computeTimestamps();
    }

    /**
     * Computes the preorder and postorder timestamps for the analyzed graph.
     */
    private void computeTimestamps() {
        Timer timer = new Timer();
        for (Node node : graph.getNodes()) {
            if (enter[node.id] == 0) {
                computeTimestamps(node, timer);
            }
        }
    }

    /**
     * Computes the preorder and postorder timestamps for the subgraph starting
     * at the specified node.
     *
     * @param node the entry point for the subgraph
     * @param timer the global timer
     */
    private void computeTimestamps(Node node, Timer timer) {
        enter[node.id] = timer.tick();
        for (Edge outgoingEdge : node.getOutgoingEdges()) {
            // Only visit nodes that have not been visited before.
            if (enter[outgoingEdge.target] == 0) {
                computeTimestamps(graph.getNode(outgoingEdge.target), timer);
            }
        }
        leave[node.id] = timer.tick();
    }

    /**
     * Timer used in depth-first search.
     */
    private static final class Timer {

        /**
         * The current time.
         */
        private int time;

        /**
         * Returns the current time, then increments it.
         *
         * @return the current time
         */
        public int tick() {
            return time++;
        }
    }

    /**
     * Tests whether the analyzed graph contains a cycle.
     *
     * @return {@code true} if and only if the analyzed graph contains a cycle
     */
    public boolean isCyclic() {
        for (Edge edge : graph.getEdges()) {
            // Check whether the current edge is a back edge.
            if (enter[edge.target] < enter[edge.source] && leave[edge.source] < leave[edge.target]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the maximal indegree of the nodes in the analyzed graph.
     *
     * @return the maximal indegree of the nodes in the analyzed graph
     */
    public int getMaximalIndegree() {
        int max = 0;
        for (Node node : graph.getNodes()) {
            max = Math.max(max, node.getNIncomingEdges());
        }
        return max;
    }

    /**
     * Computes the maximal outdegree of the nodes in the analyzed graph.
     *
     * @return the maximal outdegree of the nodes in the analyzed graph
     */
    public int getMaximalOutdegree() {
        int max = 0;
        for (Node node : graph.getNodes()) {
            max = Math.max(max, node.getNOutgoingEdges());
        }
        return max;
    }

    /**
     * Returns the number of root nodes in the analyzed graph. A <em>root
     * node</em> is a node without incoming edges.
     *
     * @return the number of root nodes in the analyzed graph
     */
    public int getNRootNodes() {
        int nRootNodes = 0;
        for (Node node : graph.getNodes()) {
            nRootNodes += node.hasIncomingEdges() ? 0 : 1;
        }
        return nRootNodes;
    }

    /**
     * Returns the number of leaf nodes in the analyzed graph. A <em>leaf
     * node</em> is a node without outgoing edges.
     *
     * @return the number of leaf nodes in the analyzed graph
     */
    public int getNLeafNodes() {
        int nLeafNodes = 0;
        for (Node node : graph.getNodes()) {
            nLeafNodes += node.hasOutgoingEdges() ? 0 : 1;
        }
        return nLeafNodes;
    }

    /**
     * Tests whether the analyzed graph is a forest. A forest is an acyclic
     * graph in which every node has at most one incoming edge.
     *
     * @return {@code true} if and only if the analyzed graph is a forest
     */
    public boolean isForest() {
        return !isCyclic() && getMaximalIndegree() <= 1;
    }

    /**
     * Tests whether the analyzed graph is a tree. A tree is a forest with
     * exactly one root node.
     *
     * @return {@code true} if and only if the analyzed graph is a tree
     */
    public boolean isTree() {
        return isForest() && getNRootNodes() == 1;
    }

    public boolean isProjective() {
        int nNodes = graph.getNNodes();
        boolean[] hasIncomingEdge = new boolean[nNodes];
        boolean[] isCovered = new boolean[nNodes];
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
            hasIncomingEdge[edge1.target] = true;
            for (int i = min1 + 1; i < max1; i++) {
                isCovered[i] = true;
            }
        }
        for (int node = 0; node < nNodes; node++) {
            if (!hasIncomingEdge[node] && isCovered[node]) {
                return false;
            }
        }
        return true;
    }

    private static boolean overlap(int min1, int max1, int min2, int max2) {
        return min1 < min2 && min2 < max1 && max1 < max2 || min2 < min1 && min1 < max2 && max2 < max1;
    }
}
