/*
 * See the file "LICENSE" for the full license governing this code.
 */
package sdp.io;

/**
 * Constants related to CoNLL-type data formats.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Constants {

	/**
	 * The string that is used to separate data columns.
	 */
	public static final String COLUMN_SEPARATOR = "\t";
	/**
	 * The string that represents undefined values.
	 */
	public static final String UNDEFINED = "_";
	/**
	 * The word form associated with the wall token.
	 */
	public static final String WALL_FORM = "$$_FORM";
	/**
	 * The lemma associated with the wall token.
	 */
	public static final String WALL_LEMMA = "$$_LEMMA";
	/**
	 * The part-of-speech tag associated with the wall token.
	 */
	public static final String WALL_POS = "$$_POS";
	/**
	 * The sense or frame associated with the wall token.
	 */
	public static final String WALL_SENSE = "$$_SENSE";
}
