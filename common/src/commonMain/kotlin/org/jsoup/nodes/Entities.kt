package org.jsoup.nodes

import com.soywiz.kds.binarySearch
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.errors.*
import org.jsoup.SerializationException
import org.jsoup.charCount
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.parser.CharacterReader
import org.jsoup.parser.Parser

/**
 * HTML entities, and escape routines. Source: [W3C
 * HTML named character references](http://www.w3.org/TR/html5/named-character-references.html#named-character-references).
 */
object Entities {
    private const val empty = -1
    private const val emptyName = ""
    const val codepointRadix = 36
    private val codeDelims = charArrayOf(',', ';')
    private val multipoints = HashMap<String, String>() // name -> multiple character references
    private val DefaultOutput = Document.OutputSettings()

    /**
     * Check if the input is a known named entity
     *
     * @param name the possible entity name (e.g. "lt" or "amp")
     * @return true if a known named entity
     */
    fun isNamedEntity(name: String): Boolean {
        return EscapeMode.extended().codepointForName(name).run { this != null && code != empty }
    }

    /**
     * Check if the input is a known named entity in the base entity set.
     *
     * @param name the possible entity name (e.g. "lt" or "amp")
     * @return true if a known named entity in the base set
     * @see .isNamedEntity
     */
    fun isBaseNamedEntity(name: String): Boolean {
        return EscapeMode.base().codepointForName(name).run { this != null && code != empty }
    }

    /**
     * Get the character(s) represented by the named entity
     *
     * @param name entity (e.g. "lt" or "amp")
     * @return the string value of the character(s) represented by this entity, or "" if not defined
     */
    fun getByName(name: String): String {
        val `val` = multipoints[name]
        if (`val` != null) return `val`
        val codepoint = EscapeMode.extended().codepointForName(name)
        return if (codepoint != null && codepoint.code != empty) charArrayOf(codepoint.toChar()).concatToString() else emptyName
    }

    fun codepointsForName(name: String, codepoints: IntArray): Int {
        val `val` = multipoints[name]
        if (`val` != null) {
            codepoints[0] = `val`[0].code
            codepoints[1] = `val`[1].code
            return 2
        }
        val codepoint = EscapeMode.extended().codepointForName(name)
        if (codepoint != null && codepoint.code != empty) {
            codepoints[0] = codepoint.code
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
    fun escape(string: String, out: Document.OutputSettings = DefaultOutput): String {
        val accum = StringUtil.borrowBuilder()
        try {
            escape(accum, string, out,
                inAttribute = false,
                normaliseWhite = false,
                stripLeadingWhite = false,
                trimTrailing = false
            )
        } catch (e: IOException) {
            throw SerializationException(e) // doesn't happen
        }
        return StringUtil.releaseBuilder(accum)
    }

    // this method does a lot, but other breakups cause rescanning and stringbuilder generations
    @Throws(IOException::class)
    fun escape(
        accum: Appendable?, string: String?, out: Document.OutputSettings,
        inAttribute: Boolean, normaliseWhite: Boolean, stripLeadingWhite: Boolean, trimTrailing: Boolean
    ) {
        var lastWasWhite = false
        var reachedNonWhite = false
        val escapeMode = out.escapeMode()
        val encoder = out.encoder()
        val coreCharset = out.coreCharset!! // init in out.prepareEncoder()
        val length = string!!.length
        var codePoint: Int
        var skipped = false
        var offset = 0
        while (offset < length) {
            codePoint = string[offset].code
            if (normaliseWhite) {
                if (StringUtil.isWhitespace(codePoint)) {
                    if (stripLeadingWhite && !reachedNonWhite) {
                        offset += codePoint.toChar().charCount
                        continue
                    }
                    if (lastWasWhite) {
                        offset += codePoint.toChar().charCount
                        continue
                    }
                    if (trimTrailing) {
                        skipped = true
                        offset += codePoint.toChar().charCount
                        continue
                    }
                    accum!!.append(' ')
                    lastWasWhite = true
                    offset += codePoint.toChar().charCount
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
            if (codePoint < 0x10000) {
                val c = codePoint.toChar()
                when (c.code) {
                    '&'.code -> accum!!.append("&amp;")
                    0xA0 -> if (escapeMode != EscapeMode.xhtml()) accum!!.append("&nbsp;") else accum!!.append("&#xa0;")
                    '<'.code ->                         // escape when in character data or when in a xml attribute val or XML syntax; not needed in html attr val
                        if (!inAttribute || escapeMode == EscapeMode.xhtml() || out.syntax() == Document.OutputSettings.Syntax.xml) accum!!.append(
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
                accum!!.append(codePoint.toChar())
            }
            offset += codePoint.toChar().charCount
        }
    }

    @Throws(IOException::class)
    private fun appendEncoded(accum: Appendable?, escapeMode: EscapeMode?, codePoint: Int) {
        val name = escapeMode!!.nameForCodepoint(codePoint.toChar())
        if (emptyName != name) // ok for identity check
            accum!!.append('&').append(name).append(';') else accum!!.append("&#x")
            .append(codePoint.toString(16)).append(';')
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
        return string?.let { Parser.unescapeEntities(it, strict) }
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
    private fun canEncode(charset: CoreCharset, c: Char, fallback: CharsetEncoder?): Boolean {
        // todo add more charset tests if impacted by Android's bad perf in canEncode
        return when (charset) {
            CoreCharset.ascii -> c.code < 0x80
            CoreCharset.utf -> true // real is:!(Character.isLowSurrogate(c) || Character.isHighSurrogate(c)); - but already check above
            else -> fallback!!.encodeToByteArray(c.toString()).isNotEmpty()
        }
    }

    sealed class EscapeMode(val file: String, size: Int) {
        class xhtml : EscapeMode(EntitiesData.xmlPoints, 4) {
            override fun copy(): EscapeMode {
                return xhtml()
            }
        }
        class base : EscapeMode(EntitiesData.basePoints, 106) {
            override fun copy(): EscapeMode {
                return base()
            }
        }
        class extended : EscapeMode(EntitiesData.fullPoints, 2125) {
            override fun copy(): EscapeMode {
                return extended()
            }
        }

        data class NamedCodepoint(
            val scalar: Char,
            val name: String
        )

        /*// table of named references to their codepoints. sorted so we can binary search. built by BuildEntities.
        var nameKeys: Array<String?> = arrayOfNulls(0)
        var codeVals // limitation is the few references with multiple characters; those go into multipoints.
                : IntArray = intArrayOf()

        // table of codepoints to named entities.
        var codeKeys // we don't support multicodepoints to single named value currently
                : IntArray = intArrayOf()
        var nameVals: Array<String?> = arrayOfNulls(0)*/

        private val entitiesByName = arrayListOf<NamedCodepoint>()

        private val entitiesByCodepoint by lazy { entitiesByName.sortedBy { it.scalar } }

        init {
            load(file, size)
        }

        abstract fun copy(): EscapeMode

        private fun load(pointsData: String, size: Int) {
            /*nameKeys = arrayOfNulls(size)
            codeVals = IntArray(size)
            codeKeys = IntArray(size)
            nameVals = arrayOfNulls(size)*/
            var i = 0
            val reader = CharacterReader(pointsData)
            try {
                while (!reader.isEmpty) {
                    // NotNestedLessLess=10913,824;1887&
                    val name = reader.consumeTo("=")
                    reader.advance()
                    val cp1 = reader.consumeToAny(codeDelims).toIntOrNull(codepointRadix) ?: 0
                    val codeDelim = reader.current
                    reader.advance()
                    val cp2: Int
                    if (codeDelim == ',') {
                        cp2 = reader.consumeTo(";").toIntOrNull(codepointRadix) ?: 0
                        reader.advance()
                    } else {
                        cp2 = empty
                    }
                    val indexS = reader.consumeTo("\n")
                    //val index = indexS.toIntOrNull(codepointRadix) ?: 0
                    reader.advance()

                    entitiesByName.add(NamedCodepoint(cp1.toChar(), name))

                    /*nameKeys[i] = name
                    codeVals[i] = cp1
                    codeKeys[index] = cp1
                    nameVals[index] = name*/
                    if (cp2 != empty) {
                        multipoints[name] = charArrayOf(cp1.toChar(), cp2.toChar()).concatToString()
                    }
                    i++
                }
                //Validate.isTrue(i == size, "Unexpected count of entities loaded")
            } finally {

            }

            entitiesByName.sortBy { it.name }
        }

        fun codepointForName(name: String): Char? {
            /*val index = nameKeys.map { it ?: "" }.binarySearch(name)
            return if (index >= 0) codeVals[index] else empty*/

            val ix = entitiesByName.binarySearch { it.name.compareTo(name) }
            if (ix >= entitiesByName.size) return null
            val entity = entitiesByName[ix]
            if (entity.name != name) return null
            return entity.scalar
        }

        fun nameForCodepoint(codepoint: Char): String? {
            var ix = entitiesByCodepoint.binarySearch { it.scalar.compareTo(codepoint) }
            val matches = arrayListOf<String>()
            while (ix < entitiesByCodepoint.size && entitiesByCodepoint[ix].scalar == codepoint) {
                matches.add(entitiesByCodepoint[ix].name)
                ix++
            }

            return if (matches.isEmpty()) null else matches.maxOf { it }

            /*val index = codeKeys.binarySearch(codepoint).index
            return if (index >= 0) {
                // the results are ordered so lower case versions of same codepoint come after uppercase, and we prefer to emit lower
                // (and binary search for same item with multi results is undefined
                if (index < nameVals.size - 1 && codeKeys[index + 1] == codepoint) nameVals[index + 1] else nameVals[index]
            } else emptyName*/
        }

        private fun size(): Int {
            return entitiesByName.size
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
