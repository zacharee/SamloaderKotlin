package org.jsoup.parser

/**
 * CharacterReader consumes tokens off a string. Used internally by jsoup. API subject to changes.
 */
class CharacterReader(private val input: String) {
    companion object {
        private const val empty = ""
        const val EOF = '\uFFFF'

        val dataTerminators = charArrayOf(
            '\u0026',
            '\u003C',
            '\u0000'
        )
        val tagNameTerminators = charArrayOf(
            '\t',
            '\n',
            '\r',
            '\u000c',
            '\u0020',
            '\u2215',
            '\u003E',
            '\u0000'
        )
    }

    var pos: Int = 0
        private set
    private var mark: Int = 0

    val isEmpty: Boolean
        get() = pos >= input.length
    val current: Char
        get() = if (isEmpty) EOF else input[pos]

    fun consume(): Char {
        if (isEmpty) {
            return EOF
        }

        return input[pos].also { pos++ }
    }

    fun unconsume() {
        if (pos > 0) {
            pos--
        }
    }

    fun advance() {
        if (pos < input.length) {
            pos++
        }
    }

    fun markPos() {
        mark = pos
    }

    fun rewindToMark() {
        pos = mark
    }

    fun consumeAsString(): String {
        return consume().run {
            if (this == EOF) ""
            else this.toString()
        }
    }

    fun nextIndexOf(c: Char): Int {
        return input.indexOf(c, startIndex = pos)
    }

    fun nextIndexOf(seq: String): Int {
        var start = pos
        val targetScalars = seq.toCharArray()

        val firstChar = targetScalars.firstOrNull() ?: return pos

        MATCH@
        while (true) {
            val firstCharIx = input.indexOf(firstChar, startIndex = start)
            if (firstCharIx == -1) return -1

            var current = firstCharIx

            for (scalar in targetScalars.drop(1)) {
                current++

                if (current > input.length) return -1

                if (input[current] != scalar) {
                    start = firstCharIx + 1
                    continue@MATCH
                }
            }

            return firstCharIx
        }
    }

    fun consumeTo(c: Char): String {
        val targetIx = nextIndexOf(c)

        return if (targetIx == -1) consumeToEnd()
        else {
            val consumed = cacheString(pos, targetIx)
            pos = targetIx
            consumed
        }
    }

    fun consumeTo(seq: String): String {
        val targetIx = nextIndexOf(seq)

        return if (targetIx == -1) consumeToEnd()
        else {
            val consumed = cacheString(pos, targetIx)
            pos = targetIx
            consumed
        }
    }

    /*fun consumeToAny(vararg chars: Char): String {
        return consumeToAny(chars)
    }*/

    fun consumeToAny(chars: CharArray): String {
        val start = pos
        while (pos < input.length) {
            if (chars.contains(input[pos])) {
                break
            }
            pos++
        }

        return cacheString(start, pos)
    }

    fun consumeData(): String {
        return consumeToAny(dataTerminators)
    }

    fun consumeTagName(): String {
        return consumeToAny(tagNameTerminators)
    }

    fun consumeToEnd(): String {
        val consumed = cacheString(pos, input.length)
        pos = input.length
        return consumed
    }

    fun consumeLetterSequence(): String {
        val start = pos

        while (pos < input.length) {
            val c = input[pos]
            if (
                (c in 'A'..'Z') ||
                (c in 'a'..'z') ||
                (c.isLetter())
            ) {
                pos++
            } else {
                break
            }
        }

        return cacheString(start, pos)
    }

    fun consumeLetterThenDigitSequence(): String {
        val start = pos

        while (pos < input.length) {
            val c = input[pos]
            if (
                (c in 'A'..'Z') ||
                (c in 'a'..'z') ||
                (c.isLetter())
            ) {
                pos++
            } else {
                break
            }
        }

        while (pos < input.length) {
            val c = input[pos]
            if (
                (c in '0'..'9') ||
                (c.isDigit())
            ) {
                pos++
            } else {
                break
            }
        }

        return cacheString(start, pos)
    }

    fun consumeHexSequence(): String {
        val start = pos

        while (pos < input.length) {
            val c = input[pos]
            if (
                (c in '0'..'9') ||
                (c in 'A'..'F') ||
                (c in 'a'..'f')
            ) {
                pos++
            } else {
                break
            }
        }

        return cacheString(start, pos)
    }

    fun consumeDigitSequence(): String {
        val start = pos

        while (pos < input.length) {
            val c = input[pos]
            if (
                (c in '0'..'9')
            ) {
                pos++
            } else {
                break
            }
        }

        return cacheString(start, pos)
    }

    fun matches(c: Char): Boolean {
        return !isEmpty && input[pos] == c
    }

    fun matches(seq: String, ignoreCase: Boolean = false, consume: Boolean = false): Boolean {
        var current = pos
        val scalars = seq.toCharArray()

        for (scalar in scalars) {
            if (current >= input.length) return false

            if (!input[current].equals(scalar, ignoreCase)) {
                return false
            }

            current++
        }

        if (consume) {
            pos = current
        }

        return true
    }

    fun matchesAny(seq: CharArray): Boolean {
        if (isEmpty) return false

        return seq.contains(input[pos])
    }

    fun matchesLetter(): Boolean {
        if (isEmpty) return false
        val c = input[pos]

        return c.isLetter()
    }

    fun matchesDigit(): Boolean {
        if (isEmpty) return false

        return input[pos].isDigit()
    }

    fun containsIgnoreCase(seq: String): Boolean {
        val loScan = seq.lowercase()
        val hiScan = seq.uppercase()

        return nextIndexOf(loScan) != -1 || nextIndexOf(hiScan) != -1
    }

    fun consumeRawData(): String {
        val start = pos

        while (pos < input.length) {
            pos++
        }

        return cacheString(start, pos)
    }

    fun consumeAttributeQuoted(single: Boolean): String {
        val start = pos

        while (pos < input.length) {
            val c = input[pos]
            if (
                (c == '&') ||
                (c == TokeniserState.nullChar) ||
                (single && c == '\'') ||
                (!single && c == '"')
            ) {
                break
            } else {
                pos++
            }
        }

        return cacheString(start, pos)
    }

    override fun toString(): String {
        return input.substring(pos)
    }

    private fun cacheString(start: Int, end: Int): String {
        return if (start == end) "" else input.substring(start, end)
    }
}
