/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.io;

import java.io.IOException;
import se.liu.ida.nlp.sdp.toolkit.graph.Graph;

/**
 * Write semantic dependency graphs to a file.
 *
 * @author Marco Kuhlmann
 */
public interface GraphWriter {

	/**
	 * Writes a single graph.
	 *
	 * @param graph the graph to be written
	 * @throws IOException if an I/O error occurs
	 */
	abstract public void writeGraph(Graph graph) throws IOException;

	/**
	 * Closes the stream and releases any system resources associated with it.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	abstract public void close() throws IOException;
}
