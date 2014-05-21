/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import sdp.graph.Edge;
import sdp.graph.Graph;
import sdp.graph.Node;
import sdp.io.GraphReader;

/**
 * Score a collection of dependency graphs relative to a gold standard.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Scorer {

	/**
	 * The label used for unlabeled edges.
	 */
	private static final String UNLABELED = "-UNLABELED-";

	/**
	 * The label used for virtual edges.
	 */
	private static final String VIRTUAL = "-VIRTUAL-";

	/**
	 * A flag indicating whether to include labels when scoring graphs.
	 */
	private final boolean includeLabels;

	/**
	 * A flag indicating whether to include top nodes when scoring graphs.
	 */
	private final boolean includeTopNodes;

	/**
	 * A flag indicating whether to include punctuation when scoring graphs.
	 */
	private final boolean includePunctuation;

	/**
	 * A flag indicating whether to treat edges as undirected when scoring
	 * graphs.
	 */
	private final boolean treatEdgesAsUndirected;

	/**
	 * Counter to store the number of graphs read.
	 */
	private int nGraphs;

	/**
	 * Set containing the edges from the gold standard graphs.
	 */
	private final Set<ScorerEdge> edgesInGoldStandard;

	/**
	 * Set containing the edges from the system output graphs.
	 */
	private final Set<ScorerEdge> edgesInSystemOutput;

	/**
	 * Counter for the number of exact matches.
	 */
	private int nExactMatches;

	/**
	 * Construct a new scorer.
	 *
	 * @param includeLabels flag indicating whether the scorer should do labeled
	 * scoring
	 * @param includeTopNodes flag indicating whether the scorer should include
	 * top nodes
	 * @param includePunctuation flag indicating whether the scorer should
	 * include punctuation
	 * @param treatEdgesAsUndirected flag indicating whether the scorer should
	 * treat edges as undirected
	 */
	public Scorer(boolean includeLabels, boolean includeTopNodes, boolean includePunctuation, boolean treatEdgesAsUndirected) {
		this.includeLabels = includeLabels;
		this.includeTopNodes = includeTopNodes;
		this.edgesInGoldStandard = new HashSet<ScorerEdge>();
		this.edgesInSystemOutput = new HashSet<ScorerEdge>();
		this.includePunctuation = includePunctuation;
		this.treatEdgesAsUndirected = treatEdgesAsUndirected;
	}

	/**
	 * Construct a new scorer.
	 */
	public Scorer() {
		this(true, true, true, false);
	}

	/**
	 * Updates this scorer with the specified pair of graphs.
	 *
	 * @param goldStandard the graph that should be considered as the gold
	 * standard
	 * @param systemOutput the graph that should be considered as the system
	 * output
	 */
	public void update(Graph goldStandard, Graph systemOutput) {
		assert goldStandard.getNNodes() == systemOutput.getNNodes();

		Set<ScorerEdge> edgesG = getEdges(goldStandard);
		Set<ScorerEdge> edgesS = getEdges(systemOutput);

		nGraphs++;
		nExactMatches += edgesG.equals(edgesS) ? 1 : 0;

		edgesInGoldStandard.addAll(edgesG);
		edgesInSystemOutput.addAll(edgesS);
	}

	/**
	 * Tests whether the specified node represents a punctuation token.
	 *
	 * @param node a node
	 * @return {@code true} if the specified node represents a punctuation token
	 */
	private boolean isPunctuation(Node node) {
		return node.pos.equals(".") || node.pos.equals(",") || node.pos.equals(":") || node.pos.equals("(") || node.pos.equals(")");
	}

	/**
	 * Tests whether an edge between the specified nodes is admissible.
	 *
	 * @param graph a graph
	 * @param src the source node of the presumed edge
	 * @param tgt the target node of the presumed edge
	 * @return {@code true} if an edge from the specified source node to the
	 * specified target node would be admissible
	 */
	private boolean edgeIsAdmissible(Graph graph, int src, int tgt) {
		if (includePunctuation) {
			return true;
		} else {
			return !isPunctuation(graph.getNode(src)) && !isPunctuation(graph.getNode(tgt));
		}
	}

	/**
	 * Extracts the (scorer-internal) edges from the specified graph.
	 *
	 * @param graph the graph from which to extract the edges
	 * @return the set of extracted edges
	 */
	private Set<ScorerEdge> getEdges(Graph graph) {
		Set<ScorerEdge> edges = new HashSet<ScorerEdge>();
		for (Edge edge : graph.getEdges()) {
			if (edgeIsAdmissible(graph, edge.source, edge.target)) {
				String label = includeLabels ? edge.label : UNLABELED;
				edges.add(makeEdge(nGraphs, edge.source, edge.target, label));
			}
		}
		if (includeTopNodes) {
			for (Node node : graph.getNodes()) {
				if (node.isTop && edgeIsAdmissible(graph, 0, node.id)) {
					edges.add(makeEdge(nGraphs, 0, node.id, VIRTUAL));
				}
			}
		}
		return edges;
	}

	/**
	 * Returns the number of edges in the gold standard.
	 *
	 * @return the number of edges in the gold standard
	 */
	public int getNEdgesInGoldStandard() {
		return edgesInGoldStandard.size();
	}

	/**
	 * Returns the number of edges in the system output.
	 *
	 * @return the number of edges in the system output
	 */
	public int getNEdgesInSystemOutput() {
		return edgesInSystemOutput.size();
	}

	/**
	 * Returns the precision computed by this scorer.
	 *
	 * @return the precision computed by this scorer
	 */
	public double getPrecision() {
		return (double) getNEdgesInCommon() / (double) getNEdgesInSystemOutput();
	}

	/**
	 * Returns the recall computed by this scorer.
	 *
	 * @return the recall computed by this scorer
	 */
	public double getRecall() {
		return (double) getNEdgesInCommon() / (double) getNEdgesInGoldStandard();
	}

	/**
	 * Returns the edges that occur both in the gold standard and in the system
	 * output.
	 *
	 * @return the edges that occur both in the gold standard and in the system
	 * output
	 */
	private Set<ScorerEdge> getEdgesInCommon() {
		Set<ScorerEdge> intersection = new HashSet<ScorerEdge>(edgesInGoldStandard);
		intersection.retainAll(edgesInSystemOutput);
		return intersection;
	}

	/**
	 * Returns the number of edges that occur both in the gold standard and in
	 * the system output.
	 *
	 * @return the number of edges that occur both in the gold standard and in
	 * the system output
	 */
	public int getNEdgesInCommon() {
		return getEdgesInCommon().size();
	}

	/**
	 * Returns the F1-score computed by this scorer.
	 *
	 * @return the F1-score computed by this scorer
	 */
	public double getF1() {
		double p = getPrecision();
		double r = getRecall();
		return 2.0 * p * r / (p + r);
	}

	/**
	 * Returns the exact match score computed by this scorer.
	 *
	 * @return the exact match score computed by this scorer
	 */
	public double getExactMatch() {
		return (double) nExactMatches / (double) nGraphs;
	}

	/**
	 * Read graphs from the specified files.
	 *
	 * @param goldStandardFile the file containing the gold standard graphs
	 * @param systemOutputFile the file containing the system output graphs
	 * @throws Exception if an I/O error occurs
	 */
	private static List<GraphPair> readGraphs(String goldStandardFile, String systemOutputFile) throws Exception {
		List<GraphPair> graphPairs = new LinkedList<GraphPair>();
		GraphReader goldStandardReader = new GraphReader(goldStandardFile);
		GraphReader systemOutputReader = new GraphReader(systemOutputFile);
		Graph goldStandard;
		Graph systemOutput;
		while ((goldStandard = goldStandardReader.readGraph()) != null) {
			systemOutput = systemOutputReader.readGraph();
			graphPairs.add(new GraphPair(goldStandard, systemOutput));
		}
		assert systemOutputReader.readGraph() == null;
		goldStandardReader.close();
		systemOutputReader.close();
		return graphPairs;
	}

	/**
	 * Scores the specified graphs using the specified scorer.
	 *
	 * @param scorer the scorer to use
	 * @param graphPairs a list of reference-candidate pairs
	 */
	private static void score(Scorer scorer, List<GraphPair> graphPairs) {
		for (GraphPair pair : graphPairs) {
			scorer.update(pair.goldStandard, pair.systemOutput);
		}
	}

	/**
	 * Scores the specified graphs.
	 *
	 * @param includeTopNodes whether the scoring should include top nodes
	 * @param graphPairs a list of reference-candidate pairs
	 */
	private static void score(boolean includeTopNodes, boolean includePunctuation, boolean treatEdgesAsUndirected, List<GraphPair> graphPairs) {
		Scorer scorerL = new Scorer(true, includeTopNodes, includePunctuation, treatEdgesAsUndirected);
		Scorer scorerU = new Scorer(false, includeTopNodes, includePunctuation, treatEdgesAsUndirected);

		score(scorerL, graphPairs);
		score(scorerU, graphPairs);

		System.err.format("Number of edges in gold standard: %d%n", scorerL.getNEdgesInGoldStandard());
		System.err.format("Number of edges in system output: %d%n", scorerL.getNEdgesInSystemOutput());
		System.err.format("Number of edges in common, labeled: %d%n", scorerL.getNEdgesInCommon());
		System.err.format("Number of edges in common, unlabeled: %d%n", scorerU.getNEdgesInCommon());
		System.err.println();

		System.err.println("### Labeled scores");
		System.err.println();
		System.err.format("LP: %f%n", scorerL.getPrecision());
		System.err.format("LR: %f%n", scorerL.getRecall());
		System.err.format("LF: %f%n", scorerL.getF1());
		System.err.format("LM: %f%n", scorerL.getExactMatch());
		System.err.println();

		System.err.println("### Breakdown by label type");
		System.err.println();
		System.err.println("Label type,Number of edges in gold standard,Number of edges in system output,Precision,Recall");
		List<String> labels = new ArrayList<String>(scorerL.getLabels());
		Collections.sort(labels);
		for (String label : labels) {
			System.err.format("%s,%d,%d,%f,%f%n", label, scorerL.getNEdgesInGoldStandardByLabel(label), scorerL.getNEdgesInSystemOutputByLabel(label), scorerL.getPrecisionPerLabel(label), scorerL.getRecallPerLabel(label));
		}
		System.err.println();

		System.err.println("### Breakdown by edge length");
		System.err.println();
		List<String> quantizedLengths = new ArrayList<String>();
		for (int i = 1; i < 100; i++) {
			String quantizedLength = scorerL.getQuantizedLength(i);
			if (!quantizedLengths.contains(quantizedLength)) {
				quantizedLengths.add(quantizedLength);
			}
		}
		System.err.println("Edge length,Number of edges in gold standard,Number of edges in system output,Precision,Recall");
		for (String quantizedLength : quantizedLengths) {
			System.err.format("%s,%d,%d,%f,%f%n", quantizedLength, scorerL.getNEdgesInGoldStandardByQuantizedLength(quantizedLength), scorerL.getNEdgesInSystemOutputByQuantizedLength(quantizedLength), scorerL.getPrecisionPerQuantizedLength(quantizedLength), scorerL.getRecallPerQuantizedLength(quantizedLength));
		}
		System.err.println();

		System.err.println("### Unlabeled scores");
		System.err.println();
		System.err.format("UP: %f%n", scorerU.getPrecision());
		System.err.format("UR: %f%n", scorerU.getRecall());
		System.err.format("UF: %f%n", scorerU.getF1());
		System.err.format("UM: %f%n", scorerU.getExactMatch());
	}

	/**
	 * Compute scores for two files.
	 *
	 * @param args the names of the files containing the gold standard graphs
	 * and the system output graphs
	 * @throws Exception if an I/O exception occurs
	 */
	public static void main(String[] args) throws Exception {
		boolean includePunctuation = true;
		boolean treatEdgesAsUndirected = false;
		for (String arg : args) {
			if (arg.equals("excludePunctuation")) {
				System.err.println("Will exclude punctuation.");
				includePunctuation = false;
			}
			if (arg.equals("treatEdgesAsUndirected")) {
				System.err.println("Will treat edges as undirected.");
				treatEdgesAsUndirected = true;
			}
		}

		System.err.println("# Evaluation");
		System.err.println();

		System.err.format("Gold standard file: %s%n", args[0]);
		System.err.format("System output file: %s%n", args[1]);
		System.err.println();

		List<GraphPair> graphPairs = readGraphs(args[0], args[1]);

		System.err.println("## Scores including virtual dependencies to top nodes");
		System.err.println();
		score(true, includePunctuation, treatEdgesAsUndirected, graphPairs);
		System.err.println();

		System.err.println("## Scores excluding virtual dependencies to top nodes");
		System.err.println();
		score(false, includePunctuation, treatEdgesAsUndirected, graphPairs);
	}

	private static class GraphPair {

		public final Graph goldStandard;
		public final Graph systemOutput;

		public GraphPair(Graph goldStandard, Graph systemOutput) {
			this.goldStandard = goldStandard;
			this.systemOutput = systemOutput;
		}
	}

	private ScorerEdge makeEdge(int graphId, int src, int tgt, String label) {
		if (treatEdgesAsUndirected) {
			return new UndirectedScorerEdge(graphId, src, tgt, label);
		} else {
			return new ScorerEdge(graphId, src, tgt, label);
		}
	}

	private static class ScorerEdge {

		final int graphId;
		final int src;
		final int tgt;
		final String label;

		public ScorerEdge(int graphId, int src, int tgt, String label) {
			this.graphId = graphId;
			this.src = src;
			this.tgt = tgt;
			this.label = label;
		}

		public int getLength() {
			return Math.max(src, tgt) - Math.min(src, tgt);
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
			final ScorerEdge other = (ScorerEdge) obj;
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

	private static class UndirectedScorerEdge extends ScorerEdge {

		public UndirectedScorerEdge(int graphId, int src, int tgt, String label) {
			super(graphId, src, tgt, label);
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 53 * hash + this.graphId;
			hash = 53 * hash + Math.min(this.src, this.tgt);
			hash = 53 * hash + Math.max(this.src, this.tgt);
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
			final ScorerEdge other = (ScorerEdge) obj;
			if (this.graphId != other.graphId) {
				return false;
			}
			if (Math.min(this.src, this.tgt) != Math.min(other.src, other.tgt)) {
				return false;
			}
			if (Math.max(this.src, this.tgt) != Math.max(other.src, other.tgt)) {
				return false;
			}
			if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
				return false;
			}
			return true;
		}
	}

	private Set<String> getLabels() {
		Set<String> labels = new HashSet<String>();
		for (ScorerEdge edge : edgesInGoldStandard) {
			labels.add(edge.label);
		}
		for (ScorerEdge edge : edgesInSystemOutput) {
			labels.add(edge.label);
		}
		return labels;
	}

	private int getNEdgesByLabel(String label, Set<ScorerEdge> edges) {
		int n = 0;
		for (ScorerEdge edge : edges) {
			n += edge.label.equals(label) ? 1 : 0;
		}
		return n;
	}

	private int getNEdgesInGoldStandardByLabel(String label) {
		return getNEdgesByLabel(label, edgesInGoldStandard);
	}

	private int getNEdgesInSystemOutputByLabel(String label) {
		return getNEdgesByLabel(label, edgesInSystemOutput);
	}

	private double getPrecisionPerLabel(String label) {
		int nEdges = 0;
		int nCorrect = 0;
		for (ScorerEdge edgeS : edgesInSystemOutput) {
			if (edgeS.label.equals(label)) {
				nEdges++;
				if (edgesInGoldStandard.contains(edgeS)) {
					nCorrect++;
				}
			}
		}
		return (double) nCorrect / (double) nEdges;
	}

	private double getRecallPerLabel(String label) {
		int nEdges = 0;
		int nCorrect = 0;
		for (ScorerEdge edgeG : edgesInGoldStandard) {
			if (edgeG.label.equals(label)) {
				nEdges++;
				if (edgesInSystemOutput.contains(edgeG)) {
					nCorrect++;
				}
			}
		}
		return (double) nCorrect / (double) nEdges;
	}

	private String getQuantizedLength(int length) {
		if (length <= 4) {
			return Integer.toString(length);
		} else if (length < 10) {
			return "5-9";
		} else {
			return "10-";
		}
	}

	private String getQuantizedLength(ScorerEdge edge) {
		return getQuantizedLength(edge.getLength());
	}

	private Set<String> getQuantizedLengths() {
		Set<String> lengths = new HashSet<String>();
		for (ScorerEdge edge : edgesInGoldStandard) {
			lengths.add(getQuantizedLength(edge));
		}
		for (ScorerEdge edge : edgesInSystemOutput) {
			lengths.add(getQuantizedLength(edge));
		}
		return lengths;
	}

	private int getNEdgesByQuantizedLength(String quantizedLength, Set<ScorerEdge> edges) {
		int n = 0;
		for (ScorerEdge edge : edges) {
			if (getQuantizedLength(edge).equals(quantizedLength)) {
				n++;
			}
		}
		return n;
	}

	private int getNEdgesInGoldStandardByQuantizedLength(String quantizedLength) {
		return getNEdgesByQuantizedLength(quantizedLength, edgesInGoldStandard);
	}

	private int getNEdgesInSystemOutputByQuantizedLength(String quantizedLength) {
		return getNEdgesByQuantizedLength(quantizedLength, edgesInSystemOutput);
	}

	private double getPrecisionPerQuantizedLength(String quantizedLength) {
		int nEdges = 0;
		int nCorrect = 0;
		for (ScorerEdge edgeS : edgesInSystemOutput) {
			if (getQuantizedLength(edgeS).equals(quantizedLength)) {
				nEdges++;
				if (edgesInGoldStandard.contains(edgeS)) {
					nCorrect++;
				}
			}
		}
		return (double) nCorrect / (double) nEdges;
	}

	private double getRecallPerQuantizedLength(String quantizedLength) {
		int nEdges = 0;
		int nCorrect = 0;
		for (ScorerEdge edgeG : edgesInGoldStandard) {
			if (getQuantizedLength(edgeG).equals(quantizedLength)) {
				nEdges++;
				if (edgesInSystemOutput.contains(edgeG)) {
					nCorrect++;
				}
			}
		}
		return (double) nCorrect / (double) nEdges;
	}
}
