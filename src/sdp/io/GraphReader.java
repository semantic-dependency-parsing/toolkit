/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import sdp.graph.Graph;
import sdp.graph.Node;

/**
 * Read semantic dependency graphs from a file. This reader implements the
 * format described at
 * {@link http://alt.qcri.org/semeval2014/task8/index.php?id=dependency-formats}.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class GraphReader extends ParagraphReader {

    /**
     * Create a graph reader, using the default input-buffer size.
     *
     * @param reader a Reader object to provide the underlying stream
     */
    public GraphReader(Reader reader) {
        super(reader);
    }

    /**
     * Create a graph reader that reads from the specified file. The file will
     * be read using the default input-buffer size.
     *
     * @param file the file to read from
     * @throws FileNotFoundException if the specified file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading
     */
    public GraphReader(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Create a graph reader that reads from the specified file. The file will
     * be read using the default input-buffer size.
     *
     * @param fileName the name of the file to read from
     * @throws FileNotFoundException if the specified file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading
     */
    public GraphReader(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * Reads a single graph.
     *
     * @return the graph read, or {@code null} if the end of the stream has been
     * reached
     * @throws IOException if an I/O error occurs
     */
    public Graph readGraph() throws IOException {
        List<String> lines = super.readParagraph();
        if (lines == null) {
            return null;
        } else {
            // Every graph should contain at least one token.
            assert lines.size() >= 2;
            // Assert the format of the graph ID.
            assert lines.get(0).matches("#2[0-9]{7}$");

            Graph graph = new Graph(lines.get(0));

            // Add the wall node.
            graph.addNode(Constants.WALL_FORM, Constants.WALL_LEMMA, Constants.WALL_POS, false, false);

            // Add the token nodes to the graph and collect a list of predicates.
            List<Integer> predicates = new ArrayList<Integer>();
            for (String line : lines.subList(1, lines.size())) {
                String[] tokens = line.split(Constants.COLUMN_SEPARATOR);

                // There should be at least six columns: ID, FORM, LEMMA, POS, TOP, PRED
                assert tokens.length >= 6;
                // Enforce valid values for the TOP column.
                assert tokens[4].equals("+") || tokens[4].equals("-");
                // Enforce valid values for the PRED column.
                assert tokens[5].equals("+") || tokens[5].equals("-");

                String form = tokens[1];
                String lemma = tokens[2];
                String pos = tokens[3];
                boolean isTop = tokens[4].equals("+");
                boolean isPred = tokens[5].equals("+");

                Node node = graph.addNode(form, lemma, pos, isTop, isPred);
                // Make sure that the node ID equals the value of the ID column.
                assert node.id == Integer.parseInt(tokens[0]);

                if (node.isPred) {
                    predicates.add(node.id);
                }
            }

            // Add the edges to the graph.
            int id = 1;
            for (String line : lines.subList(1, lines.size())) {
                String[] tokens = line.split(Constants.COLUMN_SEPARATOR);

                // There should be exactly 6 + number of predicates many columns.
                assert tokens.length == 6 + predicates.size();

                for (int i = 6; i < tokens.length; i++) {
                    if (!tokens[i].equals(Constants.UNDEFINED)) {
                        graph.addEdge(predicates.get(i - 6), id, tokens[i]);
                    }
                }
                id++;
            }

            // If a node is labeled as a PRED, it should have outgoing edges.
            for (Node node : graph.getNodes()) {
                assert !node.isPred || node.hasOutgoingEdges();
            }

            return graph;
        }
    }
}
