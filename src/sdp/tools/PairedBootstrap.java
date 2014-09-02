/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import sdp.graph.Edge;
import sdp.graph.Graph;
import sdp.graph.Node;
import sdp.io.GraphReader;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class PairedBootstrap {

	private static final String VIRTUAL_LABEL = "-VIRTUAL-";
	private static final int B = 10000;
	private static final int SEED = 42;
	private static final Random R = new Random(SEED);

	public static void main(String[] args) throws Exception {
		List<Graph> graphs0 = readGraphs(args[0]);
		List<Graph> graphs1 = readGraphs(args[1]);
		List<Graph> graphs2 = readGraphs(args[2]);

		assert graphs0.size() == graphs1.size();
		assert graphs0.size() == graphs2.size();

		int n = graphs0.size();

		List<Item> items = new ArrayList<Item>(n);
		for (int i = 0; i < n; i++) {
			items.add(new Item(graphs0.get(i), graphs1.get(i), graphs2.get(i)));
		}

		double delta = getDelta(items);
		if (delta == 0) {
			System.err.println("The two systems have the same performance.");
			System.exit(0);
		}
		if (delta > 0) {
			System.err.print("System 2 beats system 1 by ");
		} else {
			System.err.print("System 1 beats system 2 by ");
		}
		System.err.format("%f LF.%n", delta);

		System.err.println("Q: Could it be that the victory was just a random fluke?");

		double p = getP(items);

		if (p < 0.05) {
			System.err.print("A: No; the difference is most probably real");
		} else {
			System.err.print("A: Yes; this is probable");
		}
		System.err.format(" (p = %f)%n", p);
		System.exit(0);
	}

	private static List<Graph> readGraphs(String fileName) throws FileNotFoundException, IOException {
		List<Graph> graphs = new ArrayList<Graph>();
		GraphReader reader = new GraphReader(fileName);
		Graph graph;
		while ((graph = reader.readGraph()) != null) {
			graphs.add(graph);
		}
		return graphs;
	}

	private static List<Item> getSample(List<Item> base) {
		int n = base.size();
		List<Item> sample = new ArrayList<Item>(n);
		for (int i = 0; i < n; i++) {
			int j = R.nextInt(n);
			sample.add(base.get(j));
		}
		return sample;
	}

	private static double getP(List<Item> base) {
		double delta0 = getDelta(base);
		int s = 0;
		double p = 0.0;
		for (int i = 0; i < B; i++) {
			s += (getDelta(getSample(base)) > 2 * delta0) ? 1 : 0;
			p = (double) s / (double) B;
			System.err.format("\rComputing ... (no. of samples = %d, p = %f)", i, p);
		}
		System.err.println();
		return p;
	}

	private static double getDelta(List<Item> sample) {
		Set<MyEdge> edges0 = new HashSet<MyEdge>();
		Set<MyEdge> edges1 = new HashSet<MyEdge>();
		Set<MyEdge> edges2 = new HashSet<MyEdge>();

		int graphId = 0;
		for (Item item : sample) {
			addEdges(edges0, item.graph0, graphId);
			addEdges(edges1, item.graph1, graphId);
			addEdges(edges2, item.graph2, graphId);
			graphId++;
		}

		int nEdgesIn0 = edges0.size();
		int nEdgesIn1 = edges1.size();
		int nEdgesIn2 = edges2.size();
		int nEdgesCorrect1 = getIntersection(edges0, edges1).size();
		int nEdgesCorrect2 = getIntersection(edges0, edges2).size();

		double precision1 = (double) nEdgesCorrect1 / (double) nEdgesIn1;
		double recall1 = (double) nEdgesCorrect1 / (double) nEdgesIn0;
		double fOne1 = 2.0 * precision1 * recall1 / (precision1 + recall1);

		double precision2 = (double) nEdgesCorrect2 / (double) nEdgesIn2;
		double recall2 = (double) nEdgesCorrect2 / (double) nEdgesIn0;
		double fOne2 = 2.0 * precision2 * recall2 / (precision2 + recall2);

		return fOne2 - fOne1;
	}

	private static void addEdges(Set<MyEdge> edges, Graph graph, int graphId) {
		for (Node node : graph.getNodes()) {
			if (node.isTop) {
				edges.add(new MyEdge(graphId, 0, node.id, VIRTUAL_LABEL));
			}
		}
		for (Edge edge : graph.getEdges()) {
			edges.add(new MyEdge(graphId, edge.source, edge.target, edge.label));
		}
	}

	private static Set<MyEdge> getIntersection(Set<MyEdge> edges1, Set<MyEdge> edges2) {
		Set<MyEdge> intersection = new HashSet<MyEdge>(edges1);
		intersection.retainAll(edges2);
		return intersection;
	}

	private static class Item {

		public final Graph graph0;
		public final Graph graph1;
		public final Graph graph2;

		public Item(Graph graph0, Graph graph1, Graph graph2) {
			this.graph0 = graph0;
			this.graph1 = graph1;
			this.graph2 = graph2;
		}
	}

	private static class MyEdge {

		final int graphId;
		final int src;
		final int tgt;
		final String label;

		public MyEdge(int graphId, int src, int tgt, String label) {
			this.graphId = graphId;
			this.src = src;
			this.tgt = tgt;
			this.label = label;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 53 * hash + this.graphId;
			hash = 53 * hash + this.src;
			hash = 53 * hash + this.tgt;
			hash = 53 * hash + (this.label != null ? this.label.hashCode() : 0);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final MyEdge other = (MyEdge) obj;
			if (this.graphId != other.graphId) {
				return false;
			}
			if (this.src != other.src) {
				return false;
			}
			if (this.tgt != other.tgt) {
				return false;
			}
			if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
				return false;
			}
			return true;
		}
	}
}
