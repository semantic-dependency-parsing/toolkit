/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.graph;

/**
 * An edge in a semantic dependency graph.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Edge implements Comparable<Edge> {

    /**
     * The unique ID of this edge.
     */
    public final int id;
    /**
     * The ID of the source node of this edge.
     */
    public final int source;
    /**
     * The ID of the target node of this edge.
     */
    public final int target;
    /**
     * The label of this edge.
     */
    public final String label;

    /**
     * Construct a new edge.
     *
     * @param id the unique ID of the new edge
     * @param source the ID of the source node of the new edge
     * @param target the ID of the target node of the new edge
     * @param label the label of the new edge
     */
    public Edge(int id, int source, int target, String label) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.label = label;
    }

    /**
     * Compares this edge with the specified edge for order. The order used is
     * the lexicographical order on the (target, source) pairs.
     *
     * @param otherEdge the edge to be compared to this edge
     * @return a negative integer, zero, or a positive integer as this edge is
     * less than, equal to, or greater than the specified edge
     */
    @Override
    public int compareTo(Edge otherEdge) {
        if (this.target == otherEdge.target) {
            return this.source - otherEdge.source;
        } else {
            return this.target - otherEdge.target;
        }
    }
}
