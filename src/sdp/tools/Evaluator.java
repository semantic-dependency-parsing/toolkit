/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sdp.graph.Edge;
import sdp.graph.Graph;
import sdp.graph.Node;
import sdp.io.GraphReader;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Evaluator {

    private static final String PSEUDO = "-PSEUDO-";
    private final Set<Integer> excludedGraphs;
    private final Set<String> excludedLabels;
    private int nGraphs;
    private int nEdgesReferences;
    private int nEdgesCandidates;
    private int nEdgesInCommon;
    private int nEdgesInCommonUnlabeled;

    public Evaluator() {
        this.excludedGraphs = Collections.<Integer>emptySet();
        this.excludedLabels = Collections.<String>emptySet();
    }

    public Evaluator(Set<Integer> excludedGraphs, Set<String> excludedLabels) {
        this.excludedGraphs = excludedGraphs;
        this.excludedLabels = excludedLabels;
    }

    public void update(Graph reference, Graph candidate) {
        if (excludedGraphs.contains(new Integer(nGraphs))) {
            System.err.format("Excluding graph #%d.%n", nGraphs);
        } else {
            assert reference.getNNodes() == candidate.getNNodes();
            if (reference.getNNodes() != candidate.getNNodes()) {
                System.err.format("Skipping graph #%d.%n", nGraphs);
            } else {
                int nNodes = reference.getNNodes();

                boolean[][] hasEdgeReference = new boolean[nNodes][nNodes];
                String[][] labelsReference = new String[nNodes][nNodes];
                for (Edge edge : reference.getEdges()) {
                    if (!excludedLabels.contains(edge.label)) {
                        nEdgesReferences++;
                        int src = edge.source;
                        int tgt = edge.target;
                        hasEdgeReference[src][tgt] = true;
                        labelsReference[src][tgt] = edge.label;
                    }
                }
                for (Node node : reference.getNodes()) {
                    if (node.isTop && !excludedLabels.contains(PSEUDO)) {
                        nEdgesReferences++;
                        int src = 0;
                        int tgt = node.id;
                        hasEdgeReference[src][tgt] = true;
                        labelsReference[src][tgt] = PSEUDO;
                    }
                }

                for (Edge edge : candidate.getEdges()) {
                    if (!excludedLabels.contains(edge.label)) {
                        nEdgesCandidates++;
                        int src = edge.source;
                        int tgt = edge.target;
                        if (hasEdgeReference[src][tgt]) {
                            nEdgesInCommonUnlabeled++;
                            if (edge.label.equals(labelsReference[src][tgt])) {
                                nEdgesInCommon++;
                            }
                        }
                    }
                }
                for (Node node : candidate.getNodes()) {
                    if (node.isTop && !excludedLabels.contains(PSEUDO)) {
                        nEdgesCandidates++;
                        int src = 0;
                        int tgt = node.id;
                        if (hasEdgeReference[src][tgt]) {
                            nEdgesInCommonUnlabeled++;
                            nEdgesInCommon++;
                        }
                    }
                }
            }
        }

        nGraphs++;
    }

    public double getPrecision() {
        return (double) nEdgesInCommon / (double) nEdgesCandidates;
    }

    public double getRecall() {
        return (double) nEdgesInCommon / (double) nEdgesReferences;
    }

    public double getF1() {
        double p = getPrecision();
        double r = getRecall();
        return 2.0 * p * r / (p + r);
    }

    public double getUnlabeledPrecision() {
        return (double) nEdgesInCommonUnlabeled / (double) nEdgesCandidates;
    }

    public double getUnlabeledRecall() {
        return (double) nEdgesInCommonUnlabeled / (double) nEdgesReferences;
    }

    /**
     * Computes the unlabeled F1 score.
     *
     * @return the unlabeled F1 score
     */
    public double getUnlabeledF1() {
        double p = getUnlabeledPrecision();
        double r = getUnlabeledRecall();
        return 2.0 * p * r / (p + r);
    }

    public static void main(String[] args) throws Exception {
        Set<String> excludedLabels = new HashSet<>();
        excludedLabels.add(PSEUDO);
        Evaluator evaluator = new Evaluator(Collections.<Integer>emptySet(), excludedLabels);
        GraphReader referenceReader = new GraphReader(args[0]);
        GraphReader candidateReader = new GraphReader(args[1]);
        Graph reference;
        Graph candidate;
        while ((reference = referenceReader.readGraph()) != null) {
            candidate = candidateReader.readGraph();
            evaluator.update(reference, candidate);
        }
        referenceReader.close();
        candidateReader.close();
        System.err.format("P: %f%n", evaluator.getPrecision());
        System.err.format("R: %f%n", evaluator.getRecall());
        System.err.format("F: %f%n", evaluator.getF1());
    }
}
