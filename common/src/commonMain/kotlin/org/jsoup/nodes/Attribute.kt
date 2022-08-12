package org.jsoup.nodes

import io.ktor.utils.io.errors.*
import org.jsoup.SerializationException
import org.jsoup.helper.Validate
import org.jsoup.internal.Normalizer
import org.jsoup.internal.StringUtil

/**
 * A single key + value attribute. (Only used for presentation.)
 */
data class Attribute(
    var _key: String?,
    var `val`: String?,
    var parent: Attributes?
) : MutableMap.MutableEntry<String?, String?> {
    override var key: String? = null
        set(value) {
            var key = value
            Validate.notNull(key)
            key = key?.trim { it <= ' ' }
            Validate.notEmpty(key) // trimming could potentially make empty, so validate here
            if (parent != null) {
                val i = parent!!.indexOfKey(this.key)
                if (i != Attributes.NotFound) parent!!.keys[i] = key
            }
            field = key
        }

    /**
     * Create a new attribute from unencoded (raw) key and value.
     * @param key attribute key; case is preserved.
     * @param value attribute value (may be null)
     * @see .createFromEncoded
     */
    constructor(key: String?,  value: String?) : this(key, value, null) {}

    /**
     * Get the attribute value. Will return an empty string if the value is not set.
     * @return the attribute value
     */
    override val value: String
        get() = Attributes.Companion.checkNotNull(`val`)

    /**
     * Check if this Attribute has a value. Set boolean attributes have no value.
     * @return if this is a boolean attribute / attribute without a value
     */
    fun hasDeclaredValue(): Boolean {
        return `val` != null
    }

    /**
     * Set the attribute value.
     * @param val the new attribute value; may be null (to set an enabled boolean attribute)
     * @return the previous value (if was null; an empty string)
     */
    override fun setValue( `val`: String?): String? {
        var oldVal = this.`val`
        if (parent != null) {
            val i = parent!!.indexOfKey(key)
            if (i != Attributes.Companion.NotFound) {
                oldVal = parent!![key] // trust the container more
                parent!!.vals[i] = `val`
            }
        }
        this.`val` = `val`
        return Attributes.Companion.checkNotNull(oldVal)
    }

    /**
     * Get the HTML representation of this attribute; e.g. `href="index.html"`.
     * @return HTML
     */
    fun html(): String? {
        val sb = StringUtil.borrowBuilder()
        try {
            html(sb, Document("").outputSettings())
        } catch (exception: IOException) {
            throw SerializationException(exception)
        }
        return StringUtil.releaseBuilder(sb)
    }

    @Throws(IOException::class)
    protected fun html(accum: Appendable?, out: Document.OutputSettings?) {
        html(key, `val`, accum, out)
    }

    /**
     * Create a new attribute from unencoded (raw) key and value.
     * @param key attribute key; case is preserved.
     * @param val attribute value (may be null)
     * @param parent the containing Attributes (this Attribute is not automatically added to said Attributes)
     * @see .createFromEncoded
     */
    init {
        var key = _key
        Validate.notNull(key)
        key = key!!.trim { it <= ' ' }
        Validate.notEmpty(key) // trimming could potentially make empty, so validate here
        this.key = key!!
    }

    /**
     * Get the string representation of this attribute, implemented as [.html].
     * @return string
     */
    override fun toString(): String {
        return html()!!
    }

    val isDataAttribute: Boolean
        get() = isDataAttribute(key)

    /**
     * Collapsible if it's a boolean attribute and value is empty or same as name
     *
     * @param out output settings
     * @return  Returns whether collapsible or not
     */
    protected fun shouldCollapseAttribute(out: Document.OutputSettings?): Boolean {
        return shouldCollapseAttribute(key, `val`, out)
    }

    override fun equals( o: Any?): Boolean { // note parent not considered
        if (this === o) return true
        if (o == null || this::class != o::class) return false
        val attribute = o as Attribute
        if (if (key != null) key != attribute.key else attribute.key != null) return false
        return if (`val` != null) `val` == attribute.`val` else attribute.`val` == null
    }

    override fun hashCode(): Int { // note parent not considered
        var result = if (key != null) key.hashCode() else 0
        result = 31 * result + if (`val` != null) `val`.hashCode() else 0
        return result
    }

    public fun clone(): Attribute {
        return copy()
    }

    companion object {
        private val booleanAttributes = arrayOf(
            "allowfullscreen", "async", "autofocus", "checked", "compact", "declare", "default", "defer", "disabled",
            "formnovalidate", "hidden", "inert", "ismap", "itemscope", "multiple", "muted", "nohref", "noresize",
            "noshade", "novalidate", "nowrap", "open", "readonly", "required", "reversed", "seamless", "selected",
            "sortable", "truespeed", "typemustmatch"
        )

        @Throws(IOException::class)
        protected fun html(key: String?,  `val`: String?, accum: Appendable?, out: Document.OutputSettings?) {
            var key = key
            key = getValidKey(key, out!!.syntax())
            if (key == null) return  // can't write it :(
            htmlNoValidate(key, `val`, accum, out)
        }

        @Throws(IOException::class)
        fun htmlNoValidate(key: String?,  `val`: String?, accum: Appendable?, out: Document.OutputSettings?) {
            // structured like this so that Attributes can check we can write first, so it can add whitespace correctly
            accum!!.append(key)
            if (!shouldCollapseAttribute(key, `val`, out)) {
                accum.append("=\"")
                Entities.escape(accum, Attributes.Companion.checkNotNull(`val`), out, true, false, false, false)
                accum.append('"')
            }
        }

        private val xmlKeyValid = Regex("[a-zA-Z_:][-a-zA-Z0-9_:.]*")
        private val xmlKeyReplace = Regex("[^-a-zA-Z0-9_:.]")
        private val htmlKeyValid = Regex("[^\\x00-\\x1f\\x7f-\\x9f \"'/=]+")
        private val htmlKeyReplace = Regex("[\\x00-\\x1f\\x7f-\\x9f \"'/=]")

        fun getValidKey(key: String?, syntax: Document.OutputSettings.Syntax?): String? {
            // we consider HTML attributes to always be valid. XML checks key validity
            var key = key
            if (syntax == Document.OutputSettings.Syntax.xml && !xmlKeyValid.containsMatchIn(key!!)) {
                key = xmlKeyReplace.replace(key, "")
                return if (xmlKeyValid.containsMatchIn(key)) key else null // null if could not be coerced
            } else if (syntax == Document.OutputSettings.Syntax.html && !htmlKeyValid.containsMatchIn(key!!)) {
                key = htmlKeyReplace.replace(key, "")
                return if (htmlKeyValid.containsMatchIn(key)) key else null // null if could not be coerced
            }
            return key
        }

        /**
         * Create a new Attribute from an unencoded key and a HTML attribute encoded value.
         * @param unencodedKey assumes the key is not encoded, as can be only run of simple \w chars.
         * @param encodedValue HTML attribute encoded value
         * @return attribute
         */
        fun createFromEncoded(unencodedKey: String?, encodedValue: String?): Attribute {
            val value = Entities.unescape(encodedValue, true)
            return Attribute(unencodedKey, value, null) // parent will get set when Put
        }

        protected fun isDataAttribute(key: String?): Boolean {
            return key?.startsWith(Attributes.dataPrefix) == true && key.length > Attributes.dataPrefix.length
        }

        // collapse unknown foo=null, known checked=null, checked="", checked=checked; write out others
        protected fun shouldCollapseAttribute(
            key: String?,
             `val`: String?,
            out: Document.OutputSettings?
        ): Boolean {
            return out!!.syntax() == Document.OutputSettings.Syntax.html &&
                    (`val` == null || (`val`.isEmpty() || `val`.equals(key, ignoreCase = true)) && isBooleanAttribute(
                        key
                    ))
        }

        /**
         * Checks if this attribute name is defined as a boolean attribute in HTML5
         */
        fun isBooleanAttribute(key: String?): Boolean {
            return booleanAttributes.toList().binarySearch(Normalizer.lowerCase(key)) >= 0
        }
    }
}
