/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

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
     * Counter to store the number of graphs read.
     */
    private int nGraphs;

    /**
     * Set containing the edges from the reference graphs.
     */
    private final Set<ScorerEdge> edgesReference;

    /**
     * Set containing the edges from the candidate graphs.
     */
    private final Set<ScorerEdge> edgesCandidate;

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
     */
    public Scorer(boolean includeLabels, boolean includeTopNodes) {
	this.includeLabels = includeLabels;
	this.includeTopNodes = includeTopNodes;
	this.edgesReference = new HashSet<ScorerEdge>();
	this.edgesCandidate = new HashSet<ScorerEdge>();
    }

    /**
     * Construct a new scorer.
     */
    public Scorer() {
	this(true, true);
    }

    /**
     * Updates this scorer with the specified pair of graphs.
     *
     * @param reference the graph that should be considered as the gold standard
     * @param candidate the graph that should be considered as the system output
     */
    public void update(Graph reference, Graph candidate) {
	assert reference.getNNodes() == candidate.getNNodes();

	Set<ScorerEdge> edgesR = getEdges(reference);
	Set<ScorerEdge> edgesC = getEdges(candidate);

	nGraphs++;
	nExactMatches += edgesR.equals(edgesC) ? 1 : 0;

	edgesReference.addAll(edgesR);
	edgesCandidate.addAll(edgesC);
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
	    String label = includeLabels ? edge.label : UNLABELED;
	    edges.add(new ScorerEdge(nGraphs, edge.source, edge.target, label));
	}
	if (includeTopNodes) {
	    for (Node node : graph.getNodes()) {
		if (node.isTop) {
		    edges.add(new ScorerEdge(nGraphs, 0, node.id, VIRTUAL));
		}
	    }
	}
	return edges;
    }

    public int getNEdgesInReferences() {
	return edgesReference.size();
    }

    public int getNEdgesInCandidates() {
	return edgesCandidate.size();
    }

    /**
     * Returns the precision computed by this scorer.
     *
     * @return the precision computed by this scorer
     */
    public double getPrecision() {
	return (double) getNEdgesInCommon() / (double) getNEdgesInCandidates();
    }

    /**
     * Returns the recall computed by this scorer.
     *
     * @return the recall computed by this scorer
     */
    public double getRecall() {
	return (double) getNEdgesInCommon() / (double) getNEdgesInReferences();
    }

    /**
     * Returns the edges that occur both in the gold standard and in the system
     * output.
     *
     * @return the edges that occur both in the gold standard and in the system
     * output
     */
    private Set<ScorerEdge> getEdgesInCommon() {
	Set<ScorerEdge> intersection = new HashSet<ScorerEdge>(edgesReference);
	intersection.retainAll(edgesCandidate);
	return intersection;
    }

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
     * @param referencesFile the file containing the reference graphs
     * @param candidatesFile the file containing the candidate graphs
     * @throws Exception if an I/O error occurs
     */
    private static List<GraphPair> readGraphs(String referencesFile, String candidatesFile) throws Exception {
	List<GraphPair> graphPairs = new LinkedList<GraphPair>();
	GraphReader referenceReader = new GraphReader(referencesFile);
	GraphReader candidateReader = new GraphReader(candidatesFile);
	Graph reference;
	Graph candidate;
	while ((reference = referenceReader.readGraph()) != null) {
	    candidate = candidateReader.readGraph();
	    graphPairs.add(new GraphPair(reference, candidate));
	}
	assert candidateReader.readGraph() == null;
	referenceReader.close();
	candidateReader.close();
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
	    scorer.update(pair.reference, pair.candidate);
	}
    }

    /**
     * Scores the specified graphs.
     *
     * @param includeTopNodes whether the scoring should include top nodes
     * @param graphPairs a list of reference-candidate pairs
     */
    private static void score(boolean includeTopNodes, List<GraphPair> graphPairs) {
	Scorer scorerL = new Scorer(true, includeTopNodes);
	Scorer scorerU = new Scorer(false, includeTopNodes);

	score(scorerL, graphPairs);
	score(scorerU, graphPairs);

	System.err.format("Number of edges in gold standard: %d%n", scorerL.getNEdgesInReferences());
	System.err.format("Number of edges in system output: %d%n", scorerL.getNEdgesInCandidates());
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
	for (String label : scorerL.getLabels()) {
	    System.err.format("%s N1: %d N2: %d P: %f R: %f%n", label, scorerL.getNEdgesReference(label), scorerL.getNEdgesCandidate(label), scorerL.getPrecisionPerLabel(label), scorerL.getRecallPerLabel(label));
	}
	System.err.println();

	System.err.println("### Breakdown by edge length");
	System.err.println();
	for (int length : scorerL.getLengths()) {
	    System.err.format("%d N1: %d N2: %d P: %f R: %f%n", length, scorerL.getNEdgesReference(length), scorerL.getNEdgesCandidate(length), scorerL.getPrecisionPerLength(length), scorerL.getRecallPerLength(length));
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
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	System.err.println("# Evaluation");
	System.err.println();

	System.err.format("Gold standard file: %s%n", args[0]);
	System.err.format("System output file: %s%n", args[1]);
	System.err.println();

	List<GraphPair> graphPairs = readGraphs(args[0], args[1]);

	System.err.println("## Scores including virtual dependencies to top nodes");
	System.err.println();
	score(true, graphPairs);
	System.err.println();

	System.err.println("## Scores excluding virtual dependencies to top nodes");
	System.err.println();
	score(false, graphPairs);
    }

    private static class GraphPair {

	public final Graph reference;
	public final Graph candidate;

	public GraphPair(Graph reference, Graph candidate) {
	    this.reference = reference;
	    this.candidate = candidate;
	}
    }

    private static class ScorerEdge {

	private final int graphId;
	private final int src;
	private final int tgt;
	private final String label;

	public ScorerEdge(int graphId, int src, int tgt, String label) {
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

    private Set<String> getLabels() {
	Set<String> labels = new HashSet<String>();
	for (ScorerEdge edge : edgesReference) {
	    labels.add(edge.label);
	}
	for (ScorerEdge edge : edgesCandidate) {
	    labels.add(edge.label);
	}
	return labels;
    }

    private int getNEdges(String label, Set<ScorerEdge> edges) {
	int n = 0;
	for (ScorerEdge edge : edges) {
	    if (edge.label.equals(label)) {
		n++;
	    }
	}
	return n;
    }

    private int getNEdgesReference(String label) {
	return getNEdges(label, edgesReference);
    }

    private int getNEdgesCandidate(String label) {
	return getNEdges(label, edgesCandidate);
    }

    private double getPrecisionPerLabel(String label) {
	int nEdges = 0;
	int nCorrect = 0;
	for (ScorerEdge edgeC : edgesCandidate) {
	    if (edgeC.label.equals(label)) {
		nEdges++;
		if (edgesReference.contains(edgeC)) {
		    nCorrect++;
		}
	    }
	}
	return (double) nCorrect / (double) nEdges;
    }

    private double getRecallPerLabel(String label) {
	int nEdges = 0;
	int nCorrect = 0;
	for (ScorerEdge edgeR : edgesReference) {
	    if (edgeR.label.equals(label)) {
		nEdges++;
		if (edgesCandidate.contains(edgeR)) {
		    nCorrect++;
		}
	    }
	}
	return (double) nCorrect / (double) nEdges;
    }

    private int getQuantizedLength(ScorerEdge edge) {
	int min = Math.min(edge.src, edge.tgt);
	int max = Math.max(edge.src, edge.tgt);
	int length = max - min;
	if (length <= 4) {
	    return length;
	} else if (length < 10) {
	    return 5;
	} else {
	    return 10;
	}
    }

    private Set<Integer> getLengths() {
	Set<Integer> lengths = new HashSet<Integer>();
	for (ScorerEdge edge : edgesReference) {
	    lengths.add(getQuantizedLength(edge));
	}
	for (ScorerEdge edge : edgesCandidate) {
	    lengths.add(getQuantizedLength(edge));
	}
	return lengths;
    }

    private int getNEdges(int length, Set<ScorerEdge> edges) {
	int n = 0;
	for (ScorerEdge edge : edges) {
	    if (getQuantizedLength(edge) == length) {
		n++;
	    }
	}
	return n;
    }

    private int getNEdgesReference(int length) {
	return getNEdges(length, edgesReference);
    }

    private int getNEdgesCandidate(int length) {
	return getNEdges(length, edgesCandidate);
    }

    private double getPrecisionPerLength(int length) {
	int nEdges = 0;
	int nCorrect = 0;
	for (ScorerEdge edgeC : edgesCandidate) {
	    if (getQuantizedLength(edgeC) == length) {
		nEdges++;
		if (edgesReference.contains(edgeC)) {
		    nCorrect++;
		}
	    }
	}
	return (double) nCorrect / (double) nEdges;
    }

    private double getRecallPerLength(int length) {
	int nEdges = 0;
	int nCorrect = 0;
	for (ScorerEdge edgeR : edgesReference) {
	    if (getQuantizedLength(edgeR) == length) {
		nEdges++;
		if (edgesCandidate.contains(edgeR)) {
		    nCorrect++;
		}
	    }
	}
	return (double) nCorrect / (double) nEdges;
    }
}
