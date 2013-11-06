/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import se.liu.ida.nlp.sdp.graph.Graph;
import se.liu.ida.nlp.sdp.graph.Node;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class GraphReader extends ParagraphReader {

    public GraphReader(Reader reader) {
        super(reader);
    }

    public GraphReader(File file) throws FileNotFoundException {
        super(file);
    }

    public GraphReader(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public Graph readGraph() throws IOException {
        List<String> lines = super.readParagraph();
        if (lines == null) {
            return null;
        } else {
//            assert lines.size() >= 2;
            assert lines.get(0).matches("#2[0-9]{7}$");

            Graph graph = new Graph(lines.get(0));

            // Add the wall node.
            graph.addNode(Constants.WALL_FORM, Constants.WALL_LEMMA, Constants.WALL_POS, false, false);

            List<Integer> predicates = new ArrayList<Integer>();
            for (String line : lines.subList(1, lines.size())) {
                String[] tokens = line.split(Constants.COLUMN_SEPARATOR);

                assert tokens.length >= 6;
                assert tokens[4].equals("+") || tokens[4].equals("-");
                assert tokens[5].equals("+") || tokens[5].equals("-");

                String form = tokens[1];
                String lemma = tokens[2];
                String pos = tokens[3];
                boolean isRoot = tokens[4].equals("+");
                boolean isPred = tokens[5].equals("+");

                Node node = graph.addNode(form, lemma, pos, isRoot, isPred);
                assert node.id == Integer.parseInt(tokens[0]);

                if (node.isPred) {
                    predicates.add(node.id);
                }
            }

            int id = 1;

            for (String line : lines.subList(1, lines.size())) {
                String[] tokens = line.split(Constants.COLUMN_SEPARATOR);

                assert tokens.length == 6 + predicates.size();

                for (int i = 6; i < tokens.length; i++) {
                    if (!tokens[i].equals(Constants.UNDEFINED)) {
                        graph.addEdge(predicates.get(i - 6), id, tokens[i]);
                    }
                }

                id++;
            }

            for (Node node : graph.getNodes()) {
                assert !node.isPred || node.hasOutgoingEdges();
            }

            return graph;
        }
    }
}
