/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import sdp.graph.Edge;
import sdp.graph.Graph;
import sdp.graph.Node;
import sdp.io.GraphReader;

/**
 * Score a collection of dependency graphs relative to a gold standard.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Evaluator {

    /**
     * The label used for virtual edges.
     */
    private static final String VIRTUAL = "-VIRTUAL-";

    /**
     * A flag indicating whether to exclude top nodes when scoring graphs.
     */
    private final boolean excludeTopNodes;

    /**
     * Counter to store the number of graphs read.
     */
    private int nGraphs;

    /**
     * Counter for the number of edges in the gold standard.
     */
    private int nEdgesReferences;

    /**
     * Counter for the number of edges in the system output.
     */
    private int nEdgesCandidates;

    /**
     * Counter for the number of common edges.
     */
    private int nEdgesInCommon;

    /**
     * Counter for the number of common unlabeled edges.
     */
    private int nEdgesInCommonUnlabeled;

    /**
     * Counter for the number of exact matches.
     */
    private int nExactMatches;

    /**
     * Counter for the number of exact unlabeled matches.
     */
    private int nExactMatchesUnlabeled;

    /**
     * Construct a new scorer.
     *
     * @param excludeTopNodes flag indicating whether the scorer should exclude
     * top nodes
     */
    public Evaluator(boolean excludeTopNodes) {
	this.excludeTopNodes = excludeTopNodes;
    }

    /**
     * Construct a new scorer.
     */
    public Evaluator() {
	this(false);
    }

    /**
     * Updates this scorer with the specified pair of graphs.
     *
     * @param reference the graph that should be considered as the gold standard
     * @param candidate the graph that should be considered as the system output
     */
    public void update(Graph reference, Graph candidate) {
	assert reference.getNNodes() == candidate.getNNodes();
	if (reference.getNNodes() != candidate.getNNodes()) {
	    System.err.format("Will not compare gold graph %s and system graph %s as they have different numbers of nodes.%n", reference.id, candidate.id);
	} else {
	    int nNodes = reference.getNNodes();

	    int nEdgesR = 0;
	    int nEdgesC = 0;
	    int nEdgesI = 0;
	    int nEdgesIU = 0;

	    boolean[][] hasEdgeReference = new boolean[nNodes][nNodes];
	    String[][] labelsReference = new String[nNodes][nNodes];
	    for (Edge edge : reference.getEdges()) {
		nEdgesR++;
		int src = edge.source;
		assert src != 0;
		int tgt = edge.target;
		hasEdgeReference[src][tgt] = true;
		labelsReference[src][tgt] = edge.label;
	    }
	    for (Node node : reference.getNodes()) {
		if (node.isTop && !excludeTopNodes) {
		    nEdgesR++;
		    int src = 0;
		    int tgt = node.id;
		    hasEdgeReference[src][tgt] = true;
		    labelsReference[src][tgt] = VIRTUAL;
		}
	    }

	    for (Edge edge : candidate.getEdges()) {
		nEdgesC++;
		int src = edge.source;
		assert src != 0;
		int tgt = edge.target;
		if (hasEdgeReference[src][tgt]) {
		    nEdgesIU++;
		    if (edge.label.equals(labelsReference[src][tgt])) {
			nEdgesI++;
		    }
		}
	    }
	    for (Node node : candidate.getNodes()) {
		if (node.isTop && !excludeTopNodes) {
		    nEdgesC++;
		    int src = 0;
		    int tgt = node.id;
		    if (hasEdgeReference[src][tgt]) {
			nEdgesIU++;
			nEdgesI++;
		    }
		}
	    }

	    nGraphs++;

	    nEdgesReferences += nEdgesR;
	    nEdgesCandidates += nEdgesC;
	    nEdgesInCommon += nEdgesI;
	    nEdgesInCommonUnlabeled += nEdgesIU;

	    nExactMatches += nEdgesI == nEdgesR && nEdgesI == nEdgesC ? 1 : 0;
	    nExactMatchesUnlabeled += nEdgesIU == nEdgesR && nEdgesIU == nEdgesC ? 1 : 0;

//	    if (nEdgesI == nEdgesR && nEdgesI == nEdgesC) {
//		for (Edge edge : reference.getEdges()) {
//		    System.out.println(String.format("G %s %d %d %s", reference.id, edge.source, edge.target, edge.label));
//		}
//		for (Node node : reference.getNodes()) {
//		    if (node.isTop) {
//			System.out.println(String.format("G %s %d %d %s", reference.id, 0, node.id, VIRTUAL));
//		    }
//		}
//		System.out.println();
//		for (Edge edge : candidate.getEdges()) {
//		    System.out.println(String.format("S %s %d %d %s", candidate.id, edge.source, edge.target, edge.label));
//		}
//		for (Node node : candidate.getNodes()) {
//		    if (node.isTop) {
//			System.out.println(String.format("S %s %d %d %s", candidate.id, 0, node.id, VIRTUAL));
//		    }
//		}
//		System.out.println();
//	    }
	}
    }

    /**
     * Returns the precision computed by this scorer.
     *
     * @return the precision computed by this scorer
     */
    public double getPrecision() {
	return (double) nEdgesInCommon / (double) nEdgesCandidates;
    }

    /**
     * Returns the recall computed by this scorer.
     *
     * @return the recall computed by this scorer
     */
    public double getRecall() {
	return (double) nEdgesInCommon / (double) nEdgesReferences;
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
     * Returns the unlabeled precision computed by this scorer.
     *
     * @return the unlabeled precision computed by this scorer
     */
    public double getUnlabeledPrecision() {
	return (double) nEdgesInCommonUnlabeled / (double) nEdgesCandidates;
    }

    /**
     * Returns the unlabeled recall computed by this scorer.
     *
     * @return the unlabeled recall computed by this scorer
     */
    public double getUnlabeledRecall() {
	return (double) nEdgesInCommonUnlabeled / (double) nEdgesReferences;
    }

    /**
     * Returns the unlabeled F1-score computed by this scorer.
     *
     * @return the unlabeled F1-score computed by this scorer
     */
    public double getUnlabeledF1() {
	double p = getUnlabeledPrecision();
	double r = getUnlabeledRecall();
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
     * Returns the unlabeled exact match score computed by this scorer.
     *
     * @return the unlabeled exact match score computed by this scorer
     */
    public double getUnlabeledExactMatch() {
	return (double) nExactMatchesUnlabeled / (double) nGraphs;
    }

    /**
     * Evaluates the graphs in the specified files using the specified scorer.
     *
     * @param evaluator the evaluator to be used for the scoring
     * @param referencesFile the file containing the reference graphs
     * @param candidatesFile the file containing the candidate graphs
     * @throws Exception if an I/O error occurs
     */
    private static void evaluate(Evaluator evaluator, String referencesFile, String candidatesFile) throws Exception {
	GraphReader referenceReader = new GraphReader(referencesFile);
	GraphReader candidateReader = new GraphReader(candidatesFile);
	Graph reference;
	Graph candidate;
	while ((reference = referenceReader.readGraph()) != null) {
	    candidate = candidateReader.readGraph();
	    evaluator.update(reference, candidate);
	}
	referenceReader.close();
	candidateReader.close();
	System.err.format("Number of edges in gold standard: %d%n", evaluator.nEdgesReferences);
	System.err.format("Number of edges in system output: %d%n", evaluator.nEdgesCandidates);
	System.err.format("Number of edges in common, labeled: %d%n", evaluator.nEdgesInCommon);
	System.err.format("Number of edges in common, unlabeled: %d%n", evaluator.nEdgesInCommonUnlabeled);
	System.err.println();
	System.err.println("### Labeled scores");
	System.err.println();
	System.err.format("LP: %f%n", evaluator.getPrecision());
	System.err.format("LR: %f%n", evaluator.getRecall());
	System.err.format("LF: %f%n", evaluator.getF1());
	System.err.format("LM: %f%n", evaluator.getExactMatch());
	System.err.println();
	System.err.println("### Unlabeled scores");
	System.err.println();
	System.err.format("UP: %f%n", evaluator.getUnlabeledPrecision());
	System.err.format("UR: %f%n", evaluator.getUnlabeledRecall());
	System.err.format("UF: %f%n", evaluator.getUnlabeledF1());
	System.err.format("UM: %f%n", evaluator.getUnlabeledExactMatch());
    }

    /**
     * Compute scores for two files.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	Evaluator evaluator1 = new Evaluator(false);
	Evaluator evaluator2 = new Evaluator(true);
	System.err.println("# Evaluation");
	System.err.println();
	System.err.format("Gold standard file: %s%n", args[0]);
	System.err.format("System output file: %s%n", args[1]);
	System.err.println();
	System.err.println("## Scores including virtual dependencies to top nodes");
	System.err.println();
	evaluate(evaluator1, args[0], args[1]);
	System.err.println();
	System.err.println("## Scores excluding virtual dependencies to top nodes");
	System.err.println();
	evaluate(evaluator2, args[0], args[1]);
    }
}
