/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import se.liu.ida.nlp.sdp.graph.Edge;
import se.liu.ida.nlp.sdp.graph.Graph;
import se.liu.ida.nlp.sdp.graph.Node;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class GraphWriter {

    /**
     * The low-level writer.
     */
    private final PrintWriter writer;

    public GraphWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public GraphWriter(File file) throws IOException {
        this(new PrintWriter(new BufferedWriter(new FileWriter(file))));
    }

    public GraphWriter(String fileName) throws IOException {
        this(new File(fileName));
    }

    public void writeGraph(Graph graph) throws IOException {
        int nNodes = graph.getNNodes();

        String[][] labels = new String[nNodes][nNodes];
        for (Edge edge : graph.getEdges()) {
            labels[edge.source][edge.target] = edge.label;
        }
        
        writer.println(graph.id);

        for (Node node : graph.getNodes()) {
            if (node.id > 0) {
                StringBuilder sb = new StringBuilder();
                // Field 1: ID
                sb.append(Integer.toString(node.id));
                sb.append(Constants.COLUMN_SEPARATOR);
                // Field 2: FORM
                sb.append(node.form);
                sb.append(Constants.COLUMN_SEPARATOR);
                // Field 3: LEMMA
                sb.append(node.lemma);
                sb.append(Constants.COLUMN_SEPARATOR);
                // Field 4: POS
                sb.append(node.pos);
                sb.append(Constants.COLUMN_SEPARATOR);
                // Field 5: ROOT
                sb.append(node.isRoot ? "+" : "-");
                sb.append(Constants.COLUMN_SEPARATOR);
                // Field 6: PRED
                sb.append(node.isPred ? "+" : "-");

                for (Node source : graph.getNodes().subList(1, nNodes)) {
                    if (source.isPred) {
                        sb.append(Constants.COLUMN_SEPARATOR);
                        String label = labels[source.id][node.id];
                        if (label == null) {
                            sb.append(Constants.UNDEFINED);
                        } else {
                            sb.append(label);
                        }
                    }
                }

                writer.println(sb.toString());
            }
        }

        writer.println();
    }

    public void close() throws IOException {
        writer.close();
    }
}
