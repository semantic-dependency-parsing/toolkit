/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import sdp.graph.Edge;
import sdp.graph.Graph;
import sdp.graph.Node;

/**
 * Write semantic dependency graphs to a file.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class GraphWriter {

    /**
     * The low-level writer.
     */
    private final PrintWriter writer;

    /**
     * Create a graph writer that writes to the specified PrintWriter.
     *
     * @param writer the PrintWriter to be written to
     */
    public GraphWriter(PrintWriter writer) {
        this.writer = writer;
    }

    /**
     * Create a graph writer that writes to the specified file.
     *
     * @param file the file to write to
     * @throws IOException if the specified file does not exist, is a directory
     * rather than a regular file, or for some other reason cannot be opened for
     * writing
     */
    public GraphWriter(File file) throws IOException {
        this(new PrintWriter(new BufferedWriter(new FileWriter(file))));
    }

    /**
     * Create a graph writer that writes to the specified file.
     *
     * @param fileName the name of the file to read from
     * @throws IOException if the specified file does not exist, is a directory
     * rather than a regular file, or for some other reason cannot be opened for
     * writing
     */
    public GraphWriter(String fileName) throws IOException {
        this(new File(fileName));
    }

    /**
     * Writes a single graph.
     *
     * @param graph the graph to be written
     * @throws IOException if an I/O error occurs
     */
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
                // Field 5: TOP
                sb.append(node.isTop ? "+" : "-");
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

    /**
     * Closes the stream and releases any system resources associated with it.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        writer.close();
    }
}
