/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import se.liu.ida.nlp.sdp.toolkit.graph.Edge;
import se.liu.ida.nlp.sdp.toolkit.graph.Graph;
import se.liu.ida.nlp.sdp.toolkit.graph.Node;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader2015;

/**
 * Score a collection of dependency graphs relative to a gold standard.
 *
 * @author Marco Kuhlmann
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
	 * The sense used for core predications.
	 */
	private static final String NO_SENSE = "-NOSENSE-";

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

	private final Set<SemanticFrame> semanticFramesInGoldStandard;
	private final Set<SemanticFrame> semanticFramesInSystemOutput;
	private final Set<SemanticFrame> corePredicationsInGoldStandard;
	private final Set<SemanticFrame> corePredicationsInSystemOutput;

	private final ArgumentFilter labelPredicate;

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
	public Scorer(boolean includeLabels, boolean includeTopNodes, boolean includePunctuation, boolean treatEdgesAsUndirected, ArgumentFilter labelPredicate) {
		this.includeLabels = includeLabels;
		this.includeTopNodes = includeTopNodes;
		this.edgesInGoldStandard = new HashSet<ScorerEdge>();
		this.edgesInSystemOutput = new HashSet<ScorerEdge>();
		this.includePunctuation = includePunctuation;
		this.treatEdgesAsUndirected = treatEdgesAsUndirected;
		this.semanticFramesInGoldStandard = new HashSet<>();
		this.semanticFramesInSystemOutput = new HashSet<>();
		this.corePredicationsInGoldStandard = new HashSet<>();
		this.corePredicationsInSystemOutput = new HashSet<>();
		this.labelPredicate = labelPredicate;
	}

	/**
	 * Construct a new scorer.
	 */
	public Scorer() {
		this(true, true, true, false, new TrueFilter());
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

		Set<SemanticFrame> semanticFramesG = getSemanticFrames(goldStandard);
		Set<SemanticFrame> semanticFramesS = getSemanticFrames(systemOutput);

		semanticFramesInGoldStandard.addAll(semanticFramesG);
		semanticFramesInSystemOutput.addAll(semanticFramesS);

		Set<SemanticFrame> corePredicationsG = getCorePredications(goldStandard);
		Set<SemanticFrame> corePredicationsS = getCorePredications(systemOutput);

		corePredicationsInGoldStandard.addAll(corePredicationsG);
		corePredicationsInSystemOutput.addAll(corePredicationsS);
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
	 * Tests whether the specified node represents a scorable predicate.
	 * Currently only predicates corresponding to verbs are scored.
	 *
	 * @param node a node in a dependency graph
	 * @return {@code true} if the specified node represents a scorable
	 * predicate
	 */
	private boolean representsScorablePredicate(Node node) {
		return node.isPred && node.pos.startsWith("V");
	}

	/**
	 * Returns the semantic frames contained in the specified graph.
	 *
	 * @param graph a dependency graph
	 * @return the semantic frames contained in the specified graph
	 */
	private Set<SemanticFrame> getSemanticFrames(Graph graph) {
		Set<SemanticFrame> semanticFrames = new HashSet<>();
		for (Node node : graph.getNodes()) {
			if (representsScorablePredicate(node)) {
				Set<ScorerEdge> outgoingEdges = new HashSet<>();
				for (Edge edge : node.getOutgoingEdges()) {
					if (labelPredicate.applies(edge.label)) {
						ScorerEdge scorerEdge = new ScorerEdge(nGraphs, edge.source, edge.target, edge.label);
						outgoingEdges.add(scorerEdge);
					}
				}
				SemanticFrame frame = new SemanticFrame(nGraphs, node.id, node.sense, outgoingEdges);
				semanticFrames.add(frame);
			}
		}
		return semanticFrames;
	}

	/**
	 * Returns the number of semantic frames in the gold standard.
	 *
	 * @return the number of semantic frames in the gold standard
	 */
	public int getNSemanticFramesInGoldStandard() {
		return semanticFramesInGoldStandard.size();
	}

	/**
	 * Returns the number of semantic frames in the system output.
	 *
	 * @return the number of semantic frames in the system output
	 */
	public int getNSemanticFramesInSystemOutput() {
		return semanticFramesInSystemOutput.size();
	}

	/**
	 * Returns the semantic frames precision computed by this scorer.
	 *
	 * @return the semantic frames precision computed by this scorer
	 */
	public double getSemanticFramesPrecision() {
		return (double) getNSemanticFramesInCommon() / (double) getNSemanticFramesInSystemOutput();
	}

	/**
	 * Returns the semantic frames recall computed by this scorer.
	 *
	 * @return the semantic frames recall computed by this scorer
	 */
	public double getSemanticFramesRecall() {
		return (double) getNSemanticFramesInCommon() / (double) getNSemanticFramesInGoldStandard();
	}

	/**
	 * Returns the semantic frames that occur both in the gold standard and in
	 * the system output.
	 *
	 * @return the semantic frames that occur both in the gold standard and in
	 * the system output
	 */
	private Set<SemanticFrame> getSemanticFramesInCommon() {
		Set<SemanticFrame> intersection = new HashSet<>(semanticFramesInGoldStandard);
		intersection.retainAll(semanticFramesInSystemOutput);
		return intersection;
	}

	/**
	 * Returns the number of semantic frames that occur both in the gold
	 * standard and in the system output.
	 *
	 * @return the number of semantic frames that occur both in the gold
	 * standard and in the system output
	 */
	public int getNSemanticFramesInCommon() {
		return getSemanticFramesInCommon().size();
	}

	/**
	 * Returns the semantic frames F1-score computed by this scorer.
	 *
	 * @return the semantic frames F1-score computed by this scorer
	 */
	public double getSemanticFramesF1() {
		double p = getSemanticFramesPrecision();
		double r = getSemanticFramesRecall();
		return 2.0 * p * r / (p + r);
	}

	/**
	 * Returns the core predications contained in the specified graph.
	 *
	 * @param graph a dependency graph
	 * @return the core predications contained in the specified graph
	 */
	private Set<SemanticFrame> getCorePredications(Graph graph) {
		Set<SemanticFrame> semanticFrames = new HashSet<>();
		for (Node node : graph.getNodes()) {
			if (representsScorablePredicate(node)) {
				Set<ScorerEdge> outgoingEdges = new HashSet<>();
				for (Edge edge : node.getOutgoingEdges()) {
					if (labelPredicate.applies(edge.label)) {
						ScorerEdge scorerEdge = new ScorerEdge(nGraphs, edge.source, edge.target, edge.label);
						outgoingEdges.add(scorerEdge);
					}
				}
				SemanticFrame frame = new SemanticFrame(nGraphs, node.id, NO_SENSE, outgoingEdges);
				semanticFrames.add(frame);
			}
		}
		return semanticFrames;
	}

	/**
	 * Returns the number of core predications in the gold standard.
	 *
	 * @return the number of core predications in the gold standard
	 */
	public int getNCorePredicationsInGoldStandard() {
		return corePredicationsInGoldStandard.size();
	}

	/**
	 * Returns the number of core predications in the system output.
	 *
	 * @return the number of core predications in the system output
	 */
	public int getNCorePredicationsInSystemOutput() {
		return corePredicationsInSystemOutput.size();
	}

	/**
	 * Returns the core predications precision computed by this scorer.
	 *
	 * @return the core predications precision computed by this scorer
	 */
	public double getCorePredicationsPrecision() {
		return (double) getNCorePredicationsInCommon() / (double) getNCorePredicationsInSystemOutput();
	}

	/**
	 * Returns the core predications recall computed by this scorer.
	 *
	 * @return the core predications recall computed by this scorer
	 */
	public double getCorePredicationsRecall() {
		return (double) getNCorePredicationsInCommon() / (double) getNCorePredicationsInGoldStandard();
	}

	/**
	 * Returns the core predications that occur both in the gold standard and in
	 * the system output.
	 *
	 * @return the core predications that occur both in the gold standard and in
	 * the system output
	 */
	private Set<SemanticFrame> getCorePredicationsInCommon() {
		Set<SemanticFrame> intersection = new HashSet<>(corePredicationsInGoldStandard);
		intersection.retainAll(corePredicationsInSystemOutput);
		return intersection;
	}

	/**
	 * Returns the number of core predications that occur both in the gold
	 * standard and in the system output.
	 *
	 * @return the number of core predications that occur both in the gold
	 * standard and in the system output
	 */
	public int getNCorePredicationsInCommon() {
		return getCorePredicationsInCommon().size();
	}

	/**
	 * Returns the core predications F1-score computed by this scorer.
	 *
	 * @return the core predications F1-score computed by this scorer
	 */
	public double getCorePredicationsF1() {
		double p = getCorePredicationsPrecision();
		double r = getCorePredicationsRecall();
		return 2.0 * p * r / (p + r);
	}

	/**
	 * Read graphs from the specified files.
	 *
	 * @param goldStandardFile the file containing the gold standard graphs
	 * @param systemOutputFile the file containing the system output graphs
	 * @throws Exception if an I/O error occurs
	 */
	private static List<GraphPair> readGraphs(String goldStandardFile, String systemOutputFile, int max) throws Exception {
		List<GraphPair> graphPairs = new LinkedList<GraphPair>();
		GraphReader goldStandardReader = new GraphReader2015(goldStandardFile);
		GraphReader systemOutputReader = new GraphReader2015(systemOutputFile);
		Graph goldStandard;
		Graph systemOutput;
		int nGraphs = 0;
		while ((goldStandard = goldStandardReader.readGraph()) != null && (max < 0 || nGraphs < max)) {
			systemOutput = systemOutputReader.readGraph();
			graphPairs.add(new GraphPair(goldStandard, systemOutput));
			nGraphs++;
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
	private static void score(boolean includeTopNodes, boolean includePunctuation, boolean treatEdgesAsUndirected, List<GraphPair> graphPairs, ArgumentFilter labelPredicate) {
		Scorer scorerL = new Scorer(true, includeTopNodes, includePunctuation, treatEdgesAsUndirected, labelPredicate);
		Scorer scorerU = new Scorer(false, includeTopNodes, includePunctuation, treatEdgesAsUndirected, labelPredicate);

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
		System.err.println();

		System.err.println("### Core predications");
		System.err.println();
		System.err.format("Number of core predications in gold standard: %d%n", scorerL.getNCorePredicationsInGoldStandard());
		System.err.format("Number of core predications in system output: %d%n", scorerL.getNCorePredicationsInSystemOutput());
		System.err.println();
		System.err.format("PP: %f%n", scorerL.getCorePredicationsPrecision());
		System.err.format("PR: %f%n", scorerL.getCorePredicationsRecall());
		System.err.format("PF: %f%n", scorerL.getCorePredicationsF1());
		System.err.println();

		System.err.println("### Semantic frames");
		System.err.println();
		System.err.format("Number of semantic frames in gold standard: %d%n", scorerL.getNSemanticFramesInGoldStandard());
		System.err.format("Number of semantic frames in system output: %d%n", scorerL.getNSemanticFramesInSystemOutput());
		System.err.println();
		System.err.format("FP: %f%n", scorerL.getSemanticFramesPrecision());
		System.err.format("FR: %f%n", scorerL.getSemanticFramesRecall());
		System.err.format("FF: %f%n", scorerL.getSemanticFramesF1());
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
		ArgumentFilter labelPredicate = new TrueFilter();
		int graphsToRead = -1;
		for (String arg : args) {
			if (arg.equals("excludePunctuation")) {
				System.err.println("Will exclude punctuation.");
				includePunctuation = false;
			}
			if (arg.equals("treatEdgesAsUndirected")) {
				System.err.println("Will treat edges as undirected.");
				treatEdgesAsUndirected = true;
			}
			if (arg.startsWith("corePredicates=")) {
				String fileName = arg.substring(15);
				System.err.format("Reading core predicates from %s%n", fileName);
				labelPredicate = new ListFilter(new File(fileName));
			}
			if (arg.startsWith("max=")) {
				graphsToRead = Integer.parseInt(arg.substring(4));
				System.err.format("Will read at most %d graphs.%n", graphsToRead);
			}
			if (arg.startsWith("representation=")) {
				String representation = arg.substring(15).toLowerCase();
				if (representation.equals("dm")) {
					System.err.println("Representation type: DM");
					labelPredicate = new DMArgumentFilter();
				}
				if (representation.equals("pas")) {
					System.err.println("Representation type: PAS");
					labelPredicate = new PASArgumentFilter();
				}
				if (representation.equals("psd")) {
					System.err.println("Representation type: PSD");
					labelPredicate = new PSDPredicate();
				}
			}
		}

		System.err.println("# Evaluation");
		System.err.println();

		System.err.format("Gold standard file: %s%n", args[0]);
		System.err.format("System output file: %s%n", args[1]);
		System.err.println();

		List<GraphPair> graphPairs = readGraphs(args[0], args[1], graphsToRead);

		System.err.println("## Scores including virtual dependencies to top nodes");
		System.err.println();
		score(true, includePunctuation, treatEdgesAsUndirected, graphPairs, labelPredicate);
		System.err.println();

		System.err.println("## Scores excluding virtual dependencies to top nodes");
		System.err.println();
		score(false, includePunctuation, treatEdgesAsUndirected, graphPairs, labelPredicate);
	}

	private static class GraphPair {

		public final Graph goldStandard;
		public final Graph systemOutput;

		public GraphPair(Graph goldStandard, Graph systemOutput) {
			this.goldStandard = goldStandard;
			this.systemOutput = systemOutput;
		}
	}

	private interface ArgumentFilter {

		abstract public boolean applies(String label);
	}

	private static class TrueFilter implements ArgumentFilter {

		@Override
		public boolean applies(String label) {
			return true;
		}
	}

	private static class ListFilter implements ArgumentFilter {

		private final Set<String> labels;

		public ListFilter(File file) {
			this.labels = new HashSet<>();
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					labels.add(line.trim());
				}
			} catch (FileNotFoundException e) {
				System.err.println("File not found.");
				System.exit(1);
			} catch (IOException e) {
				System.err.println("I/O exception.");
				System.exit(1);
			}
		}

		@Override
		public boolean applies(String label) {
			return labels.contains(label);
		}
	}

	private static class DMArgumentFilter implements ArgumentFilter {

		@Override
		public boolean applies(String label) {
			return true;
		}
	}

	private static class PASArgumentFilter implements ArgumentFilter {

		private final Set<String> coreArguments;

		public PASArgumentFilter() {
			this.coreArguments = new HashSet<>();
			coreArguments.add("verb_arg1");
			coreArguments.add("verb_arg12");
			coreArguments.add("verb_arg123");
			coreArguments.add("verb_arg1234");
			coreArguments.add("verb_mod_arg1");
			coreArguments.add("verb_mod_arg12");
			coreArguments.add("verb_mod_arg123");
			coreArguments.add("verb_mod_arg1234");
			coreArguments.add("adj_arg1");
			coreArguments.add("adj_arg12");
			coreArguments.add("adj_mod_arg1");
			coreArguments.add("adj_mod_arg12");
			coreArguments.add("coord_arg12");
			coreArguments.add("prep_arg12");
			coreArguments.add("prep_arg123");
			coreArguments.add("prep_mod_arg12");
			coreArguments.add("prep_mod_arg123");
		}

		@Override
		public boolean applies(String label) {
			return coreArguments.contains(label);
		}
	}

	public static class PSDPredicate implements ArgumentFilter {

		@Override
		public boolean applies(String label) {
			return label.endsWith("-arg");
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

	private static class SemanticFrame {

		final int graphId;
		final int node;
		final String sense;
		final Set<ScorerEdge> outgoingEdges;

		public SemanticFrame(int graphId, int node, String sense, Set<ScorerEdge> outgoingEdges) {
			this.graphId = graphId;
			this.node = node;
			this.sense = sense;
			this.outgoingEdges = outgoingEdges;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 53 * hash + this.graphId;
			hash = 53 * hash + this.node;
			hash = 53 * hash + (this.sense != null ? this.sense.hashCode() : 0);
			hash = 53 * hash + (this.outgoingEdges != null ? this.outgoingEdges.hashCode() : 0);
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
			final SemanticFrame other = (SemanticFrame) obj;
			if (this.graphId != other.graphId) {
				return false;
			}
			if (this.node != other.node) {
				return false;
			}
			if ((this.sense == null) ? (other.sense != null) : !this.sense.equals(other.sense)) {
				return false;
			}
			if ((this.outgoingEdges == null) ? (other.outgoingEdges != null) : !this.outgoingEdges.equals(other.outgoingEdges)) {
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
