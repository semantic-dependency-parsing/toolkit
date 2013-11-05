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

    public final int id;
    public final int source;
    public final int target;
    //
    public final String label;

    public Edge(int id, int source, int target, String label) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.label = label;
    }

    @Override
    public int compareTo(Edge otherEdge) {
        if (this.target == otherEdge.target) {
            return this.source - otherEdge.source;
        } else {
            return this.target - otherEdge.target;
        }
    }
}
