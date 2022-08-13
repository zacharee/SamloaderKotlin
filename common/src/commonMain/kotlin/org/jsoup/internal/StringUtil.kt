package org.jsoup.internal

import com.soywiz.kds.Stack
import com.soywiz.korio.lang.Charset.Companion.appendCodePointV
import io.ktor.http.*
import org.jsoup.charCount
import org.jsoup.helper.Validate
import kotlin.math.min

/**
 * A minimal String utility class. Designed for **internal** jsoup use only - the API and outcome may change without
 * notice.
 */
object StringUtil {
    // memoised padding up to 21 (blocks 0 to 20 spaces)
    val padding: Array<String> = arrayOf(
        "", " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ",
        "         ", "          ", "           ", "            ", "             ", "              ", "               ",
        "                ", "                 ", "                  ", "                   ", "                    "
    )

    /**
     * Join a collection of strings by a separator
     * @param strings collection of string objects
     * @param sep string to place between strings
     * @return joined string
     */
    fun join(strings: Collection<*>, sep: String?): String {
        return join(strings.iterator(), sep)
    }

    /**
     * Join a collection of strings by a separator
     * @param strings iterator of string objects
     * @param sep string to place between strings
     * @return joined string
     */
    fun join(strings: Iterator<*>, sep: String?): String {
        if (!strings.hasNext()) return ""
        val start: String = strings.next().toString()
        if (!strings.hasNext()) // only one, avoid builder
            return start
        val j = StringJoiner(sep)
        j.add(start)
        while (strings.hasNext()) {
            j.add(strings.next())
        }
        return j.complete()
    }

    /**
     * Join an array of strings by a separator
     * @param strings collection of string objects
     * @param sep string to place between strings
     * @return joined string
     */
    fun join(strings: Array<String?>, sep: String?): String {
        return join(strings.toList(), sep)
    }
    /**
     * Returns space padding, up to a max of maxPaddingWidth.
     * @param width amount of padding desired
     * @param maxPaddingWidth maximum padding to apply. Set to `-1` for unlimited.
     * @return string of spaces * width
     */
    /**
     * Returns space padding (up to the default max of 30). Use [.padding] to specify a different limit.
     * @param width amount of padding desired
     * @return string of spaces * width
     * @see .padding
     */
    fun padding(width: Int, maxPaddingWidth: Int = 30): String {
        var width: Int = width
        Validate.isTrue(width >= 0, "width must be >= 0")
        Validate.isTrue(maxPaddingWidth >= -1)
        if (maxPaddingWidth != -1) width = min(width, maxPaddingWidth)
        if (width < padding.size) return padding[width]
        val out = CharArray(width)
        for (i in 0 until width) out[i] = ' '
        return out.concatToString()
    }

    /**
     * Tests if a string is blank: null, empty, or only whitespace (" ", \r\n, \t, etc)
     * @param string string to test
     * @return if string is blank
     */
    fun isBlank(string: String?): Boolean {
        if (string.isNullOrEmpty()) return true
        val l: Int = string.length
        for (i in 0 until l) {
            if (!isWhitespace(string[i].code)) return false
        }
        return true
    }

    /**
     * Tests if a string starts with a newline character
     * @param string string to test
     * @return if its first character is a newline
     */
    fun startsWithNewline(string: String?): Boolean {
        if (string.isNullOrEmpty()) return false
        return string[0] == '\n'
    }

    /**
     * Tests if a string is numeric, i.e. contains only digit characters
     * @param string string to test
     * @return true if only digit chars, false if empty or null or contains non-digit chars
     */
    fun isNumeric(string: String?): Boolean {
        if (string.isNullOrEmpty()) return false
        val l: Int = string.length
        for (i in 0 until l) {
            if (!string[i].isDigit()) return false
        }
        return true
    }

    /**
     * Tests if a code point is "whitespace" as defined in the HTML spec. Used for output HTML.
     * @param c code point to test
     * @return true if code point is whitespace, false otherwise
     * @see .isActuallyWhitespace
     */
    fun isWhitespace(c: Int): Boolean {
        return (c == ' '.code) || (c == '\t'.code) || (c == '\n'.code) || (c == '\u000c'.code) || (c == '\r'.code)
    }

    /**
     * Tests if a code point is "whitespace" as defined by what it looks like. Used for Element.text etc.
     * @param c code point to test
     * @return true if code point is whitespace, false otherwise
     */
    fun isActuallyWhitespace(c: Int): Boolean {
        return (c == ' '.code) || (c == '\t'.code) || (c == '\n'.code) || (c == '\u000c'.code) || (c == '\r'.code) || (c == 160)
        // 160 is &nbsp; (non-breaking space). Not in the spec but expected.
    }

    fun isInvisibleChar(c: Int): Boolean {
        return c == 8203 || c == 173 // zero width sp, soft hyphen
        // previously also included zw non join, zw join - but removing those breaks semantic meaning of text
    }

    /**
     * Normalise the whitespace within this string; multiple spaces collapse to a single, and all whitespace characters
     * (e.g. newline, tab) convert to a simple space.
     * @param string content to normalise
     * @return normalised string
     */
    fun normaliseWhitespace(string: String?): String {
        val sb: StringBuilder = borrowBuilder()
        appendNormalisedWhitespace(sb, string, false)
        return releaseBuilder(sb)
    }

    /**
     * After normalizing the whitespace within a string, appends it to a string builder.
     * @param accum builder to append to
     * @param string string to normalize whitespace within
     * @param stripLeading set to true if you wish to remove any leading whitespace
     */
    fun appendNormalisedWhitespace(accum: StringBuilder?, string: String?, stripLeading: Boolean) {
        var lastWasWhite = false
        var reachedNonWhite = false
        val len: Int = string?.length ?: return
        var c: Char
        var i = 0
        while (i < len) {
            c = string[i]
            if (isActuallyWhitespace(c.code)) {
                if ((stripLeading && !reachedNonWhite) || lastWasWhite) {
                    i += c.charCount
                    continue
                }
                accum?.append(' ')
                lastWasWhite = true
            } else if (!isInvisibleChar(c.code)) {
                accum?.appendCodePointV(c.code)
                lastWasWhite = false
                reachedNonWhite = true
            }
            i += c.charCount
        }
    }

    fun `in`(needle: String?, vararg haystack: String): Boolean {
        val len: Int = haystack.size
        for (i in 0 until len) {
            if ((haystack[i] == needle)) return true
        }
        return false
    }

    fun inSorted(needle: String?, haystack: Array<out String?>): Boolean {
        return haystack.toList().binarySearch(needle) >= 0
    }

    /**
     * Tests that a String contains only ASCII characters.
     * @param string scanned string
     * @return true if all characters are in range 0 - 127
     */
    fun isAscii(string: String): Boolean {
        Validate.notNull(string)
        for (i in string.indices) {
            val c: Int = string[i].code
            if (c > 127) { // ascii range
                return false
            }
        }
        return true
    }

    private val extraDotSegmentsPattern: Regex = Regex("^/((\\.{1,2}/)+)")

    /**
     * Create a new absolute URL, from a provided existing absolute URL and a relative URL component.
     * @param base the existing absolute base URL
     * @param relUrl the relative URL to resolve. (If it's already absolute, it will be returned)
     * @return the resolved absolute URL
     * @throws MalformedURLException if an error occurred generating the URL
     */
    fun resolve(base: Url, relUrl: String?): Url {
        // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
        var relUrl: String? = relUrl
        if (relUrl?.startsWith("?") == true) relUrl = base.pathSegments.joinToString("/") + relUrl
        // workaround: //example.com + ./foo = //example.com/./foo, not //example.com/foo
        val url = URLBuilder("$base/$relUrl").build()
        var fixedFile: String = extraDotSegmentsPattern.matchEntire(url.pathSegments.last())?.value!!.replaceFirst(Regex("/"), "")
        if (url.fragment.isNotBlank()) {
            fixedFile = fixedFile + "#" + url.fragment
        }
        return url
    }

    /**
     * Create a new absolute URL, from a provided existing absolute URL and a relative URL component.
     * @param baseUrl the existing absolute base URL
     * @param relUrl the relative URL to resolve. (If it's already absolute, it will be returned)
     * @return an absolute URL if one was able to be generated, or the empty string if not
     */
    fun resolve(baseUrl: String, relUrl: String): String {
        try {
            val base: Url
            try {
                base = URLBuilder(baseUrl).build()
            } catch (e: Exception) {
                // the base is unsuitable, but the attribute/rel may be abs on its own, so try that
                val abs = URLBuilder(relUrl).build()
                return abs.toString()
            }
            return resolve(base, relUrl).toString()
        } catch (e: Exception) {
            // it may still be valid, just that Java doesn't have a registered stream handler for it, e.g. tel
            // we test here vs at start to normalize supported URLs (e.g. HTTP -> http)
            return if (validUriScheme.containsMatchIn(relUrl)) relUrl else ""
        }
    }

    private val validUriScheme: Regex = Regex("^[a-zA-Z][a-zA-Z0-9+-.]*:")
    private val threadLocalBuilders: Stack<StringBuilder> = Stack()

    /**
     * Maintains cached StringBuilders in a flyweight pattern, to minimize new StringBuilder GCs. The StringBuilder is
     * prevented from growing too large.
     *
     *
     * Care must be taken to release the builder once its work has been completed, with [.releaseBuilder]
     * @return an empty StringBuilder
     */
    fun borrowBuilder(): StringBuilder {
        val builders: Stack<StringBuilder> = threadLocalBuilders
        return if (builders.isEmpty()) StringBuilder(MaxCachedBuilderSize) else builders.pop()
    }

    /**
     * Release a borrowed builder. Care must be taken not to use the builder after it has been returned, as its
     * contents may be changed by this method, or by a concurrent thread.
     * @param sb the StringBuilder to release.
     * @return the string value of the released String Builder (as an incentive to release it!).
     */
    fun releaseBuilder(sb: StringBuilder?): String {
        Validate.notNull(sb)
        var sb: StringBuilder = sb!!
        val string: String = sb.toString()
        if (sb.length > MaxCachedBuilderSize) sb =
            StringBuilder(MaxCachedBuilderSize) // make sure it hasn't grown too big
        else sb.removeRange(sb.indices) // make sure it's emptied on release
        val builders: Stack<StringBuilder> = threadLocalBuilders
        builders.push(sb)
        while (builders.size > MaxIdleBuilders) {
            builders.pop()
        }
        return string
    }

    private const val MaxCachedBuilderSize: Int = 8 * 1024
    private const val MaxIdleBuilders: Int = 8

    /**
     * A StringJoiner allows incremental / filtered joining of a set of stringable objects.
     * @since 1.14.1
     */
    class StringJoiner
    /**
     * Create a new joiner, that uses the specified separator. MUST call [.complete] or will leak a thread
     * local string builder.
     *
     * @param separator the token to insert between strings
     */ constructor(private val separator: String?) {
        var sb: StringBuilder? = borrowBuilder() // sets null on builder release so can't accidentally be reused
        var first: Boolean = true

        /**
         * Add another item to the joiner, will be separated
         */
        fun add(stringy: Any?): StringJoiner {
            Validate.notNull(sb) // don't reuse
            if (!first) sb?.append(separator)
            sb?.append(stringy)
            first = false
            return this
        }

        /**
         * Append content to the current item; not separated
         */
        fun append(stringy: Any?): StringJoiner {
            Validate.notNull(sb) // don't reuse
            sb?.append(stringy)
            return this
        }

        /**
         * Return the joined string, and release the builder back to the pool. This joiner cannot be reused.
         */
        fun complete(): String {
            val string: String = releaseBuilder(sb)
            sb = null
            return string
        }
    }
}
