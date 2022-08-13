package org.jsoup.nodes

import org.jsoup.helper.Validate

/**
 * A Range object tracks the character positions in the original input source where a Node starts or ends. If you want to
 * track these positions, tracking must be enabled in the Parser with
 * [org.jsoup.parser.Parser.setTrackPosition].
 * @see Node.sourceRange
 * @since 1.15.2
 */
class Range
/**
 * Creates a new Range with start and end Positions. Called by TreeBuilder when position tracking is on.
 * @param start the start position
 * @param end the end position
 */(private val start: Position, private val end: Position) {
    /**
     * Get the start position of this node.
     * @return the start position
     */
    fun start(): Position {
        return start
    }

    /**
     * Get the end position of this node.
     * @return the end position
     */
    fun end(): Position {
        return end
    }

    /**
     * Test if this source range was tracked during parsing.
     * @return true if this was tracked during parsing, false otherwise (and all fields will be `-1`).
     */
    val isTracked: Boolean
        get() = this !== Untracked

    /**
     * Internal jsoup method, called by the TreeBuilder. Tracks a Range for a Node.
     * @param node the node to associate this position to
     * @param start if this is the starting range. `false` for Element end tags.
     */
    fun track(node: Node, start: Boolean) {
        node.attributes()!!.putUserData(if (start) RangeKey else EndRangeKey, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        val range = other as Range
        return if (start != range.start) false else end == range.end
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }

    /**
     * Gets a String presentation of this Range, in the format `line,column:pos-line,column:pos`.
     * @return a String
     */
    override fun toString(): String {
        return "$start-$end"
    }

    /**
     * A Position object tracks the character position in the original input source where a Node starts or ends. If you want to
     * track these positions, tracking must be enabled in the Parser with
     * [org.jsoup.parser.Parser.setTrackPosition].
     * @see Node.sourceRange
     */
    class Position
    /**
     * Create a new Position object. Called by the TreeBuilder if source position tracking is on.
     * @param pos position index
     * @param lineNumber line number
     * @param columnNumber column number
     */(private val pos: Int, private val lineNumber: Int, private val columnNumber: Int) {
        /**
         * Gets the position index (0-based) of the original input source that this Position was read at. This tracks the
         * total number of characters read into the source at this position, regardless of the number of preceeding lines.
         * @return the position, or `-1` if untracked.
         */
        fun pos(): Int {
            return pos
        }

        /**
         * Gets the line number (1-based) of the original input source that this Position was read at.
         * @return the line number, or `-1` if untracked.
         */
        fun lineNumber(): Int {
            return lineNumber
        }

        /**
         * Gets the cursor number (1-based) of the original input source that this Position was read at. The cursor number
         * resets to 1 on every new line.
         * @return the cursor number, or `-1` if untracked.
         */
        fun columnNumber(): Int {
            return columnNumber
        }

        /**
         * Test if this position was tracked during parsing.
         * @return true if this was tracked during parsing, false otherwise (and all fields will be `-1`).
         */
        val isTracked: Boolean
            get() = this !== UntrackedPos

        /**
         * Gets a String presentation of this Position, in the format `line,column:pos`.
         * @return a String
         */
        override fun toString(): String {
            return "$lineNumber,$columnNumber:$pos"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            val position = other as Position
            if (pos != position.pos) return false
            return if (lineNumber != position.lineNumber) false else columnNumber == position.columnNumber
        }

        override fun hashCode(): Int {
            var result = pos
            result = 31 * result + lineNumber
            result = 31 * result + columnNumber
            return result
        }
    }

    companion object {
        private val RangeKey: String = Attributes.internalKey("jsoup.sourceRange")
        private val EndRangeKey: String = Attributes.internalKey("jsoup.endSourceRange")
        private val UntrackedPos = Position(-1, -1, -1)
        private val Untracked = Range(UntrackedPos, UntrackedPos)

        /**
         * Retrieves the source range for a given Node.
         * @param node the node to retrieve the position for
         * @param start if this is the starting range. `false` for Element end tags.
         * @return the Range, or the Untracked (-1) position if tracking is disabled.
         */
        fun of(node: Node, start: Boolean): Range {
            val key = if (start) RangeKey else EndRangeKey
            return if (!node.hasAttr(key)) Untracked else Validate.ensureNotNull(
                node.attributes()!!.getUserData(key)
            ) as Range
        }
    }
}
