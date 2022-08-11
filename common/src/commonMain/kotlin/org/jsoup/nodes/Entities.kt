package org.jsoup.nodes

import jsoup.SerializationException
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.parser.CharacterReader
import org.jsoup.parser.Parser
import java.io.IOException
import java.nio.charset.CharsetEncoder
import java.util.*

/**
 * HTML entities, and escape routines. Source: [W3C
 * HTML named character references](http://www.w3.org/TR/html5/named-character-references.html#named-character-references).
 */
object Entities {
    private const val empty = -1
    private const val emptyName = ""
    const val codepointRadix = 36
    private val codeDelims = charArrayOf(',', ';')
    private val multipoints = HashMap<String?, String>() // name -> multiple character references
    private val DefaultOutput = Document.OutputSettings()

    /**
     * Check if the input is a known named entity
     *
     * @param name the possible entity name (e.g. "lt" or "amp")
     * @return true if a known named entity
     */
    fun isNamedEntity(name: String?): Boolean {
        return EscapeMode.extended.codepointForName(name) != empty
    }

    /**
     * Check if the input is a known named entity in the base entity set.
     *
     * @param name the possible entity name (e.g. "lt" or "amp")
     * @return true if a known named entity in the base set
     * @see .isNamedEntity
     */
    fun isBaseNamedEntity(name: String?): Boolean {
        return EscapeMode.base.codepointForName(name) != empty
    }

    /**
     * Get the character(s) represented by the named entity
     *
     * @param name entity (e.g. "lt" or "amp")
     * @return the string value of the character(s) represented by this entity, or "" if not defined
     */
    fun getByName(name: String?): String {
        val `val` = multipoints[name]
        if (`val` != null) return `val`
        val codepoint = EscapeMode.extended.codepointForName(name)
        return if (codepoint != empty) String(intArrayOf(codepoint), 0, 1) else emptyName
    }

    fun codepointsForName(name: String?, codepoints: IntArray): Int {
        val `val` = multipoints[name]
        if (`val` != null) {
            codepoints[0] = `val`.codePointAt(0)
            codepoints[1] = `val`.codePointAt(1)
            return 2
        }
        val codepoint = EscapeMode.extended.codepointForName(name)
        if (codepoint != empty) {
            codepoints[0] = codepoint
            return 1
        }
        return 0
    }
    /**
     * HTML escape an input string. That is, `<` is returned as `&lt;`
     *
     * @param string the un-escaped string to escape
     * @param out the output settings to use
     * @return the escaped string
     */
    /**
     * HTML escape an input string, using the default settings (UTF-8, base entities). That is, `<` is returned as
     * `&lt;`
     *
     * @param string the un-escaped string to escape
     * @return the escaped string
     */
    @JvmOverloads
    fun escape(string: String?, out: Document.OutputSettings? = DefaultOutput): String? {
        if (string == null) return ""
        val accum = StringUtil.borrowBuilder()
        try {
            escape(accum, string, out, false, false, false, false)
        } catch (e: IOException) {
            throw jsoup.SerializationException(e) // doesn't happen
        }
        return StringUtil.releaseBuilder(accum)
    }

    // this method does a lot, but other breakups cause rescanning and stringbuilder generations
    @Throws(IOException::class)
    fun escape(
        accum: Appendable?, string: String?, out: Document.OutputSettings?,
        inAttribute: Boolean, normaliseWhite: Boolean, stripLeadingWhite: Boolean, trimTrailing: Boolean
    ) {
        var lastWasWhite = false
        var reachedNonWhite = false
        val escapeMode = out!!.escapeMode()
        val encoder = out.encoder()
        val coreCharset = out.coreCharset // init in out.prepareEncoder()
        val length = string!!.length
        var codePoint: Int
        var skipped = false
        var offset = 0
        while (offset < length) {
            codePoint = string.codePointAt(offset)
            if (normaliseWhite) {
                if (StringUtil.isWhitespace(codePoint)) {
                    if (stripLeadingWhite && !reachedNonWhite) {
                        offset += Character.charCount(codePoint)
                        continue
                    }
                    if (lastWasWhite) {
                        offset += Character.charCount(codePoint)
                        continue
                    }
                    if (trimTrailing) {
                        skipped = true
                        offset += Character.charCount(codePoint)
                        continue
                    }
                    accum!!.append(' ')
                    lastWasWhite = true
                    offset += Character.charCount(codePoint)
                    continue
                } else {
                    lastWasWhite = false
                    reachedNonWhite = true
                    if (skipped) {
                        accum!!.append(' ') // wasn't the end, so need to place a normalized space
                        skipped = false
                    }
                }
            }
            // surrogate pairs, split implementation for efficiency on single char common case (saves creating strings, char[]):
            if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                val c = codePoint.toChar()
                when (c.code) {
                    '&'.code -> accum!!.append("&amp;")
                    0xA0 -> if (escapeMode != EscapeMode.xhtml) accum!!.append("&nbsp;") else accum!!.append("&#xa0;")
                    '<'.code ->                         // escape when in character data or when in a xml attribute val or XML syntax; not needed in html attr val
                        if (!inAttribute || escapeMode == EscapeMode.xhtml || out.syntax() == Document.OutputSettings.Syntax.xml) accum!!.append(
                            "&lt;"
                        ) else accum!!.append(c)

                    '>'.code -> if (!inAttribute) accum!!.append("&gt;") else accum!!.append(c)
                    '"'.code -> if (inAttribute) accum!!.append("&quot;") else accum!!.append(c)
                    0x9, 0xA, 0xD -> accum!!.append(c)
                    else -> if (c.code < 0x20 || !canEncode(coreCharset, c, encoder)) appendEncoded(
                        accum,
                        escapeMode,
                        codePoint
                    ) else accum!!.append(c)
                }
            } else {
                val c = String(Character.toChars(codePoint))
                if (encoder.canEncode(c)) // uses fallback encoder for simplicity
                    accum!!.append(c) else appendEncoded(accum, escapeMode, codePoint)
            }
            offset += Character.charCount(codePoint)
        }
    }

    @Throws(IOException::class)
    private fun appendEncoded(accum: Appendable?, escapeMode: EscapeMode?, codePoint: Int) {
        val name = escapeMode!!.nameForCodepoint(codePoint)
        if (emptyName != name) // ok for identity check
            accum!!.append('&').append(name).append(';') else accum!!.append("&#x")
            .append(Integer.toHexString(codePoint)).append(';')
    }

    /**
     * Un-escape an HTML escaped string. That is, `&lt;` is returned as `<`.
     *
     * @param string the HTML string to un-escape
     * @return the unescaped string
     */
    fun unescape(string: String?): String? {
        return unescape(string, false)
    }

    /**
     * Unescape the input string.
     *
     * @param string to un-HTML-escape
     * @param strict if "strict" (that is, requires trailing ';' char, otherwise that's optional)
     * @return unescaped string
     */
    fun unescape(string: String?, strict: Boolean): String? {
        return Parser.Companion.unescapeEntities(string, strict)
    }

    /*
     * Provides a fast-path for Encoder.canEncode, which drastically improves performance on Android post JellyBean.
     * After KitKat, the implementation of canEncode degrades to the point of being useless. For non ASCII or UTF,
     * performance may be bad. We can add more encoders for common character sets that are impacted by performance
     * issues on Android if required.
     *
     * Benchmarks:     *
     * OLD toHtml() impl v New (fastpath) in millis
     * Wiki: 1895, 16
     * CNN: 6378, 55
     * Alterslash: 3013, 28
     * Jsoup: 167, 2
     */
    private fun canEncode(charset: CoreCharset?, c: Char, fallback: CharsetEncoder?): Boolean {
        // todo add more charset tests if impacted by Android's bad perf in canEncode
        return when (charset) {
            CoreCharset.ascii -> c.code < 0x80
            CoreCharset.utf -> true // real is:!(Character.isLowSurrogate(c) || Character.isHighSurrogate(c)); - but already check above
            else -> fallback!!.canEncode(c)
        }
    }

    private fun load(e: EscapeMode, pointsData: String?, size: Int) {
        e.nameKeys = arrayOfNulls(size)
        e.codeVals = IntArray(size)
        e.codeKeys = IntArray(size)
        e.nameVals = arrayOfNulls(size)
        var i = 0
        val reader = CharacterReader(pointsData)
        try {
            while (!reader.isEmpty) {
                // NotNestedLessLess=10913,824;1887&
                val name = reader.consumeTo('=')
                reader.advance()
                val cp1 = reader.consumeToAny(*codeDelims).toInt(codepointRadix)
                val codeDelim = reader.current()
                reader.advance()
                val cp2: Int
                if (codeDelim == ',') {
                    cp2 = reader.consumeTo(';').toInt(codepointRadix)
                    reader.advance()
                } else {
                    cp2 = empty
                }
                val indexS = reader.consumeTo('&')
                val index = indexS!!.toInt(codepointRadix)
                reader.advance()
                e.nameKeys[i] = name
                e.codeVals[i] = cp1
                e.codeKeys[index] = cp1
                e.nameVals[index] = name
                if (cp2 != empty) {
                    multipoints[name] = String(intArrayOf(cp1, cp2), 0, 2)
                }
                i++
            }
            Validate.isTrue(i == size, "Unexpected count of entities loaded")
        } finally {
            reader.close()
        }
    }

    enum class EscapeMode(file: String?, size: Int) {
        /**
         * Restricted entities suitable for XHTML output: lt, gt, amp, and quot only.
         */
        xhtml(EntitiesData.xmlPoints, 4),

        /**
         * Default HTML output entities.
         */
        base(EntitiesData.basePoints, 106),

        /**
         * Complete HTML entities.
         */
        extended(EntitiesData.fullPoints, 2125);

        // table of named references to their codepoints. sorted so we can binary search. built by BuildEntities.
        var nameKeys: Array<String?> = arrayOfNulls(0)
        var codeVals // limitation is the few references with multiple characters; those go into multipoints.
                : IntArray = intArrayOf()

        // table of codepoints to named entities.
        var codeKeys // we don't support multicodepoints to single named value currently
                : IntArray = intArrayOf()
        var nameVals: Array<String?> = arrayOfNulls(0)

        init {
            load(this, file, size)
        }

        fun codepointForName(name: String?): Int {
            val index = Arrays.binarySearch(nameKeys, name)
            return if (index >= 0) codeVals[index] else empty
        }

        fun nameForCodepoint(codepoint: Int): String? {
            val index = Arrays.binarySearch(codeKeys, codepoint)
            return if (index >= 0) {
                // the results are ordered so lower case versions of same codepoint come after uppercase, and we prefer to emit lower
                // (and binary search for same item with multi results is undefined
                if (index < nameVals.size - 1 && codeKeys[index + 1] == codepoint) nameVals[index + 1] else nameVals[index]
            } else emptyName
        }

        private fun size(): Int {
            return nameKeys.size
        }
    }

    enum class CoreCharset {
        ascii, utf, fallback;

        companion object {
            fun byName(name: String): CoreCharset {
                if (name == "US-ASCII") return ascii
                return if (name.startsWith("UTF-")) utf else fallback
            }
        }
    }
}
