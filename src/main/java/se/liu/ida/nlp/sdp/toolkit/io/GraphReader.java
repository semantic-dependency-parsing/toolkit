/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.toolkit.io;

import java.io.IOException;
import se.liu.ida.nlp.sdp.toolkit.graph.Graph;

/**
 * Read semantic dependency graphs from a file.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public interface GraphReader {

	/**
	 * Reads a single graph.
	 *
	 * @return the graph read, or {@code null} if the end of the stream has been
	 * reached
	 * @throws IOException if an I/O error occurs
	 */
	abstract public Graph readGraph() throws IOException;

	/**
	 * Closes the stream and releases any system resources associated with it.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	abstract public void close() throws IOException;
}
