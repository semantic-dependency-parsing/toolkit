/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Reads paragraphs separated by blank lines from a character-input stream.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class ParagraphReader extends LineNumberReader {

    /**
     * Create a paragraph reader, using the default input-buffer size.
     *
     * @param reader a Reader object to provide the underlying stream
     */
    public ParagraphReader(Reader reader) {
        super(reader);
    }

    /**
     * Create a paragraph reader, reading characters into a buffer of the given
     * size.
     *
     * @param reader a Reader object to provide the underlying stream
     * @param sz the size of the buffer
     */
    public ParagraphReader(Reader reader, int sz) {
        super(reader, sz);
    }

    /**
     * Create a paragraph reader that reads from the specified file. The file
     * will be read using the default input-buffer size.
     *
     * @param file the file to read from
     * @throws FileNotFoundException if the specified file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading
     */
    public ParagraphReader(File file) throws FileNotFoundException {
        super(new FileReader(file));
    }

    /**
     * Create a paragraph reader that reads from the specified file. The file
     * will be read using the default input-buffer size.
     *
     * @param fileName the name of the file to read from
     * @throws FileNotFoundException if the specified file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading
     */
    public ParagraphReader(String fileName) throws FileNotFoundException {
        super(new FileReader(fileName));
    }

    /**
     * Reads a single paragraph. A paragraph is a list of lines terminated by a
     * blank (empty) line, or the end of the stream.
     *
     * @return the lines of the paragraph read, or {@code null} if the end of
     * the stream has been reached
     * @throws IOException if an I/O error occurs
     */
    public List<String> readParagraph() throws IOException {
        String line = super.readLine();
        if (line == null) {
            return null;
        } else {
            if (line.isEmpty()) {
                return Collections.<String>emptyList();
            } else {
                List<String> lines = new LinkedList<String>();
                do {
                    lines.add(line);
                } while ((line = super.readLine()) != null && !line.isEmpty());
                return lines;
            }
        }
    }
}
