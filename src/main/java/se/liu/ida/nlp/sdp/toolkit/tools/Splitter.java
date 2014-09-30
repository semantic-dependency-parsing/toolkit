/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.tools;

import se.liu.ida.nlp.sdp.toolkit.graph.Graph;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader;
import se.liu.ida.nlp.sdp.toolkit.io.GraphReader2015;
import se.liu.ida.nlp.sdp.toolkit.io.GraphWriter;
import se.liu.ida.nlp.sdp.toolkit.io.GraphWriter2015;

/**
 * Splits the SDP training data into training and development.
 *
 * @author Marco Kuhlmann
 */
public class Splitter {

	public static void main(String[] args) throws Exception {
		GraphReader reader = new GraphReader2015(args[0]);
		GraphWriter writerTrain = new GraphWriter2015(args[1]);
		GraphWriter writerDevel = new GraphWriter2015(args[2]);
		Graph graph;
		while ((graph = reader.readGraph()) != null) {
			if (graph.id.substring(2, 4).equals("20")) {
				writerDevel.writeGraph(graph);
			} else {
				writerTrain.writeGraph(graph);
			}
		}
		reader.close();
		writerTrain.close();
		writerDevel.close();
	}
}
