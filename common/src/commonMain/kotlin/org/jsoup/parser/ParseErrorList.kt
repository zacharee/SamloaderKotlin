package org.jsoup.parser

/**
 * A container for ParseErrors.
 *
 * @author Jonathan Hedley
 */
class ParseErrorList internal constructor(private val initialCapacity: Int, val maxSize: Int) : MutableList<ParseError?> by ArrayList(
    initialCapacity
) {

    /**
     * Create a new ParseErrorList with the same settings, but no errors in the list
     * @param copy initial and max size details to copy
     */
    internal constructor(copy: ParseErrorList) : this(copy.initialCapacity, copy.maxSize) {}

    fun canAddError(): Boolean {
        return size < maxSize
    }

    companion object {
        private const val INITIAL_CAPACITY: Int = 16
        fun noTracking(): ParseErrorList {
            return ParseErrorList(0, 0)
        }

        fun tracking(maxSize: Int): ParseErrorList {
            return ParseErrorList(INITIAL_CAPACITY, maxSize)
        }
    }
}
