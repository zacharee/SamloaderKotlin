package swiftsoup

import com.soywiz.korio.lang.assert

open class Attribute(key: String, var value: String) {
    companion object {
        val booleanAttributes = arrayOf(
            "allowfullscreen", "async", "autofocus", "checked", "compact", "controls", "declare", "default", "defer",
            "disabled", "formnovalidate", "hidden", "inert", "ismap", "itemscope", "multiple", "muted", "nohref",
            "noresize", "noshade", "novalidate", "nowrap", "open", "readonly", "required", "reversed", "seamless",
            "selected", "sortable", "truespeed", "typemustmatch"
        )

        fun createFromEncoded(unencodedKey: String, encodedValue: String): Attribute {
            val value = Entities.unescape(string = encodedValue, strict = true)
            return Attribute(unencodedKey, value)
        }
    }

    var key: String = ""
        set(value) {
            assert(value.isNotEmpty())
            field = value.trim()
        }

    val html: String
        get() = StringBuilder().apply {
            html(this, Document("").outputSettings())
        }.toString()

    val isDataAttribute: Boolean
        get() = key.startsWith(Attributes.dataPrefix) && key.length > Attributes.dataPrefix.length

    open val isBooleanAttribute: Boolean
        get() = booleanAttributes.contains(key.lowercase())

    init {
        this.key = key
    }

    fun html(accum: StringBuilder, out: OutputSettings) {
        accum.append(key)
        if (!shouldCollapseAttribute(out)) {
            accum.append("=\"")
            Entities.escape(accum, value, out, true, false, false)
            accum.append("\"")
        }
    }

    fun shouldCollapseAttribute(out: OutputSettings): Boolean {
        return (value.isEmpty() || value.equals(key, true))
                && out.syntax == OutputSettings.Syntax.html
                && isBooleanAttribute
    }

    override fun toString(): String {
        return html
    }

    override fun hashCode(): Int {
        return 31 * key.hashCode() + value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Attribute && key == other.key && value == other.value
    }
}
