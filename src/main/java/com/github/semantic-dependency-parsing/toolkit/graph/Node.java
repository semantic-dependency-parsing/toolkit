/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.graph;

import java.util.LinkedList;
import java.util.List;

/**
 * A node in a semantic dependency graph.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Node {

	/**
	 * The unique ID of this node.
	 */
	public final int id;
	/**
	 * The list of incoming edges of this node.
	 */
	public final List<Edge> incomingEdges;
	/**
	 * The list of outgoing edges of this node.
	 */
	public final List<Edge> outgoingEdges;
	/**
	 * The word form associated with this node.
	 */
	public final String form;
	/**
	 * The lemma associated with this node.
	 */
	public final String lemma;
	/**
	 * The part-of-speech tag associated with this node.
	 */
	public final String pos;
	/**
	 * Whether this node is a top node.
	 */
	public final boolean isTop;
	/**
	 * Whether this node is a predicate.
	 */
	public final boolean isPred;
	/**
	 * The sense or frame of this predicate.
	 */
	public final String sense;

	/**
	 * Construct a new node.
	 *
	 * @param id the unique ID of the new node
	 * @param form the word form to be associated with the new node
	 * @param lemma the lemma to be associated with the new node
	 * @param pos the part-of-speech tag to be associated with the new node
	 * @param isTop a flag indicating whether the new node is a top node
	 * @param isPred a flag indicating whether the new node is a predicate
	 * @param sense the sense or frame to be associated with the new node
	 */
	public Node(int id, String form, String lemma, String pos, boolean isTop, boolean isPred, String sense) {
		this.id = id;
		this.incomingEdges = new LinkedList<Edge>();
		this.outgoingEdges = new LinkedList<Edge>();
		this.form = form;
		this.lemma = lemma;
		this.pos = pos;
		this.isTop = isTop;
		this.isPred = isPred;
		this.sense = sense;
	}

	/**
	 * Adds the specified edge as an incoming edge of this node.
	 *
	 * @param edge the edge to be added as an incoming edge
	 * @return the newly added edge
	 */
	public Edge addIncomingEdge(Edge edge) {
		incomingEdges.add(edge);
		return edge;
	}

	/**
	 * Tests whether this node has any incoming edges.
	 *
	 * @return {@code true} if this node has incoming edges; {@code false}
	 * otherwise
	 */
	public boolean hasIncomingEdges() {
		return !incomingEdges.isEmpty();
	}

	/**
	 * Returns the number of incoming edges of this node.
	 *
	 * @return the number of incoming edges of this node
	 */
	public int getNIncomingEdges() {
		return incomingEdges.size();
	}

	/**
	 * Returns the incoming edges of this node.
	 *
	 * @return the incoming edges of this node
	 */
	public List<Edge> getIncomingEdges() {
		return incomingEdges;
	}

	/**
	 * Adds the specified edge as an outgoing edge of this node.
	 *
	 * @param edge the edge to be added as an outgoing edge
	 * @return the newly added edge
	 */
	public Edge addOutgoingEdge(Edge edge) {
		outgoingEdges.add(edge);
		return edge;
	}

	/**
	 * Tests whether this node has any outgoing edges.
	 *
	 * @return {@code true} if this node has any outgoing edges; {@code false}
	 * otherwise
	 */
	public boolean hasOutgoingEdges() {
		return !outgoingEdges.isEmpty();
	}

	/**
	 * Returns the number of outgoing edges of this node.
	 *
	 * @return the number of outgoing edges of this node
	 */
	public int getNOutgoingEdges() {
		return outgoingEdges.size();
	}

	/**
	 * Returns the outgoing edges of this node.
	 *
	 * @return the outgoing edges of this node.
	 */
	public List<Edge> getOutgoingEdges() {
		return outgoingEdges;
	}
}
