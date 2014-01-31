/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.tools;

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
    private final boolean excludeTopNodes;
    private int nEdgesReferences;
    private int nEdgesCandidates;
    private int nEdgesInCommon;
    private int nEdgesInCommonUnlabeled;

    public Evaluator(boolean excludeTopNodes) {
        this.excludeTopNodes = excludeTopNodes;
    }

    public Evaluator() {
        this(false);
    }

    public void update(Graph reference, Graph candidate) {
        assert reference.getNNodes() == candidate.getNNodes();
        if (reference.getNNodes() != candidate.getNNodes()) {
            System.err.format("Will not compare gold graph %s and system graph %s as they have different numbers of nodes.%n", reference.id, candidate.id);
        } else {
            int nNodes = reference.getNNodes();

            boolean[][] hasEdgeReference = new boolean[nNodes][nNodes];
            String[][] labelsReference = new String[nNodes][nNodes];
            for (Edge edge : reference.getEdges()) {
                nEdgesReferences++;
                int src = edge.source;
                int tgt = edge.target;
                hasEdgeReference[src][tgt] = true;
                labelsReference[src][tgt] = edge.label;
            }
            for (Node node : reference.getNodes()) {
                if (node.isTop && !excludeTopNodes) {
                    nEdgesReferences++;
                    int src = 0;
                    int tgt = node.id;
                    hasEdgeReference[src][tgt] = true;
                    labelsReference[src][tgt] = PSEUDO;
                }
            }

            for (Edge edge : candidate.getEdges()) {
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
            for (Node node : candidate.getNodes()) {
                if (node.isTop && !excludeTopNodes) {
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

    public double getUnlabeledF1() {
        double p = getUnlabeledPrecision();
        double r = getUnlabeledRecall();
        return 2.0 * p * r / (p + r);
    }

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
        System.err.format("Number of edges in reference: %d%n", evaluator.nEdgesReferences);
        System.err.format("Number of edges in candidates: %d%n", evaluator.nEdgesCandidates);
        System.err.format("Number of edges in common, labeled: %d%n", evaluator.nEdgesInCommon);
        System.err.format("Number of edges in common, unlabeled: %d%n", evaluator.nEdgesInCommonUnlabeled);
        System.err.format("P: %f%n", evaluator.getPrecision());
        System.err.format("R: %f%n", evaluator.getRecall());
        System.err.format("F: %f%n", evaluator.getF1());
    }

    public static void main(String[] args) throws Exception {
        Evaluator evaluator1 = new Evaluator(false);
        Evaluator evaluator2 = new Evaluator(true);
        System.err.println("Scores including top nodes:");
        evaluate(evaluator1, args[0], args[1]);
        System.err.println();
        System.err.println("Scores excluding top nodes:");
        evaluate(evaluator2, args[0], args[1]);
    }
}
