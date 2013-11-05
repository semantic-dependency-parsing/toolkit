/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.graph;

import java.util.LinkedList;
import java.util.List;

/**
 * A node in a semantic dependency graph.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Node {

    public final int id;
    public final List<Edge> incomingEdges;
    public final List<Edge> outgoingEdges;
    //
    public final String form;
    public final String lemma;
    public final String pos;
    public final boolean isRoot;
    public final boolean isPred;

    public Node(int id, String form, String lemma, String pos, boolean isRoot, boolean isPred) {
        this.id = id;
        this.incomingEdges = new LinkedList<Edge>();
        this.outgoingEdges = new LinkedList<Edge>();
        //
        this.form = form;
        this.lemma = lemma;
        this.pos = pos;
        this.isRoot = isRoot;
        this.isPred = isPred;
    }

    public Edge addIncomingEdge(Edge edge) {
        incomingEdges.add(edge);
        return edge;
    }

    public boolean hasIncomingEdges() {
        return !incomingEdges.isEmpty();
    }

    public int getNIncomingEdges() {
        return incomingEdges.size();
    }

    public List<Edge> getIncomingEdges() {
        return incomingEdges;
    }

    public Edge addOutgoingEdge(Edge edge) {
        outgoingEdges.add(edge);
        return edge;
    }

    public boolean hasOutgoingEdges() {
        return !outgoingEdges.isEmpty();
    }

    public int getNOutgoingEdges() {
        return outgoingEdges.size();
    }

    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }
}
