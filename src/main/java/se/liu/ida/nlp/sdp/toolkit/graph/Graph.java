/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A semantic dependency graph.
 *
 * @author Marco Kuhlmann
 */
public class Graph {

	/**
	 * The list of nodes of this graph.
	 */
	private final List<Node> nodes;
	/**
	 * The list of edges of this graph.
	 */
	private final List<Edge> edges;
	/**
	 * The unique ID of this graph.
	 */
	public final String id;

	/**
	 * Construct an empty graph.
	 *
	 * @param id the unique ID of the new graph
	 */
	public Graph(String id) {
		this.id = id;
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
	}

	/**
	 * Adds a new node to this graph.
	 *
	 * @param form the word form to be associated with the new node
	 * @param lemma the lemma to be associated with the new node
	 * @param pos the part-of-speech tag to be associated with the new node
	 * @param isTop a flag indicating whether the new node is a TOP node
	 * @param isPred a flag indicating whether the new node represents a
	 * predicate
	 * @param sense the sense or frame to be associated with the new node
	 * @return the newly added node
	 */
	public Node addNode(String form, String lemma, String pos, boolean isTop, boolean isPred, String sense) {
		Node node = new Node(nodes.size(), form, lemma, pos, isTop, isPred, sense);
		nodes.add(node);
		return node;
	}

	/**
	 * Adds a new edge to this graph.
	 *
	 * @param source the ID of the source node of the new edge
	 * @param target the ID of the target node of the new edge
	 * @param label the label of the new edge
	 * @return the newly added edge
	 */
	public Edge addEdge(int source, int target, String label) {
		assert 0 <= source && source < nodes.size();
		assert 0 <= target && target < nodes.size();
		Edge edge = new Edge(edges.size(), source, target, label);
		edges.add(edge);
		nodes.get(source).addOutgoingEdge(edge);
		nodes.get(target).addIncomingEdge(edge);
		return edge;
	}

	/**
	 * Returns the number of nodes of this graph.
	 *
	 * @return the number of nodes of this graph
	 */
	public int getNNodes() {
		return nodes.size();
	}

	/**
	 * Returns the nodes of this graph. This returns a list whose elements are
	 * sorted in increasing order of their IDs.
	 *
	 * @return the nodes of this graph
	 */
	public List<Node> getNodes() {
		return nodes;
	}

	/**
	 * Returns the node of this graph with the specified ID.
	 *
	 * @param node the ID of the node to return
	 * @return the node with the specified ID
	 */
	public Node getNode(int node) {
		assert 0 <= node && node < nodes.size();
		return nodes.get(node);
	}

	/**
	 * Returns the number of edges of this graph.
	 *
	 * @return the number of edges of this graph
	 */
	public int getNEdges() {
		return edges.size();
	}

	/**
	 * Returns the edges of this graph. This returns a list whose elements are
	 * sorted in increasing order of their IDs.
	 *
	 * @return the edges of this graph
	 */
	public List<Edge> getEdges() {
		return edges;
	}

	/**
	 * Returns the edge of this graph with the specified ID.
	 *
	 * @param edge the ID of the edge of return
	 * @return the edge with the specified ID
	 */
	public Edge getEdge(int edge) {
		assert 0 <= edge && edge < edges.size();
		return edges.get(edge);
	}

	/**
	 * Returns the top nodes of this graph. This returns a list whose elements
	 * are sorted in increasing order of their IDs.
	 *
	 * @return the top nodes of this graph
	 */
	public List<Node> getTops() {
		List<Node> roots = new LinkedList<Node>();
		for (Node node : nodes) {
			if (node.isTop) {
				roots.add(node);
			}
		}
		return roots;
	}

	/**
	 * Returns the predicates of this graph. This returns a list whose elements
	 * are sorted in increasing order of their IDs.
	 *
	 * @return the predicates of this graph
	 */
	public List<Node> getPreds() {
		List<Node> preds = new LinkedList<Node>();
		for (Node node : nodes) {
			if (node.isPred) {
				preds.add(node);
			}
		}
		return preds;
	}
}
