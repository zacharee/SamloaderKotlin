package swiftsoup

import com.soywiz.korio.lang.Charset.Companion.appendCodePointV

sealed class Token {
    companion object {
        fun reset(sb: StringBuilder) {
            sb.clear()
        }
    }

    var type = TokenType.Doctype

    abstract fun reset(): Token

    enum class TokenType {
        Doctype,
        StartTag,
        EndTag,
        Comment,
        Char,
        EOF,
    }

    class Doctype : Token() {
        val name = StringBuilder()
        var pubSysKey: String? = null
        val publicIdentifier = StringBuilder()
        val systemIdentifier = StringBuilder()
        var forceQuirks = false

        init {
            type = TokenType.Doctype
        }

        override fun reset(): Token {
            reset(name)
            pubSysKey = null
            reset(publicIdentifier)
            reset(systemIdentifier)
            forceQuirks = false
            return this
        }
    }

    open class Tag : Token() {
        var tagName: String? = null
        var normalName: String? = null
        private var pendingAttributeName: String? = null
        private val pendingAttributeValue = StringBuilder()
        private var pendingAttributeValueS: String? = null
        private var hasEmptyAttributeValue = false
        private var hasPendingAttributeValue = false
        var selfClosing = false
        var attributes = Attributes()

        override fun reset(): Token {
            tagName = null
            normalName = null
            pendingAttributeName = null
            reset(pendingAttributeValue)
            pendingAttributeValueS = null
            hasEmptyAttributeValue = false
            hasPendingAttributeValue = false
            selfClosing = false
            attributes = Attributes()
            return this
        }

        fun newAttribute() {
            if (pendingAttributeName != null) {
                val attribute = when {
                    hasPendingAttributeValue -> Attribute(
                        pendingAttributeName!!,
                        if (pendingAttributeValue.isNotEmpty()) {
                            pendingAttributeValue.toString()
                        } else {
                            pendingAttributeValueS!!
                        }
                    )
                    hasEmptyAttributeValue -> Attribute(
                        pendingAttributeName!!, ""
                    )
                    else -> BooleanAttribute(pendingAttributeName!!)
                }
                attributes.set(attribute)
            }
            pendingAttributeName = null
            hasEmptyAttributeValue = false
            hasPendingAttributeValue = false
            reset(pendingAttributeValue)
            pendingAttributeValueS = null
        }

        fun finalizeTag() {
            if (pendingAttributeName != null) {
                newAttribute()
            }
        }

        fun name(name: String): Tag {
            tagName = name
            normalName = name.lowercase()
            return this
        }

        fun appendTagName(append: String) {
            tagName = if (tagName == null) append else tagName + append
            normalName = tagName?.lowercase()
        }

        fun appendTagName(append: Char) {
            appendTagName("$append")
        }

        fun appendAttributeName(append: String) {
            pendingAttributeName = if (pendingAttributeName == null) append else pendingAttributeName + append
        }

        fun appendAttributeName(append: Char) {
            appendAttributeName("$append")
        }

        fun appendAttributeValue(append: String) {
            ensureAttributeValue()
            if (pendingAttributeValue.isEmpty()) {
                pendingAttributeValueS = append
            } else {
                pendingAttributeValue.append(append)
            }
        }

        fun appendAttributeValue(append: kotlin.Char) {
            ensureAttributeValue()
            pendingAttributeValue.appendCodePointV(append.code)
        }

        fun appendAttributeValue(append: CharArray) {
            ensureAttributeValue()
            append.forEach {
                pendingAttributeValue.appendCodePointV(it.code)
            }
        }

        fun setEmptyAttributeValue() {
            hasEmptyAttributeValue = true
        }

        private fun ensureAttributeValue() {
            hasPendingAttributeValue = true
            if (pendingAttributeValueS != null) {
                pendingAttributeValue.append(pendingAttributeValueS)
                pendingAttributeValueS = null
            }
        }
    }

    class StartTag : Tag() {
        init {
            attributes = Attributes()
            type = TokenType.StartTag
        }

        override fun reset(): StartTag {
            super.reset()
            attributes = Attributes()

            return this
        }

        fun nameAttr(name: String, attributes: Attributes): StartTag {
            tagName = name
            this.attributes = attributes
            normalName = tagName?.lowercase()
            return this
        }

        override fun toString(): String {
            return if (attributes.size > 0) {
                "<$tagName ${attributes}>"
            } else {
                "<$tagName>"
            }
        }
    }

    class EndTag : Tag() {
        init {
            type = TokenType.EndTag
        }

        override fun reset(): EndTag {
            super.reset()
            return this
        }

        override fun toString(): String {
            return "</$tagName>"
        }
    }

    class Comment : Token() {
        val data = StringBuilder()
        var bogus = false

        override fun reset(): Token {
            reset(data)
            bogus = false
            return this
        }

        init {
            type = TokenType.Comment
        }

        override fun toString(): String {
            return "<!--$data-->"
        }
    }

    class Char : Token() {
        var data: String? = null

        init {
            type = TokenType.Char
        }

        override fun reset(): Token {
            data = null
            return this
        }

        fun data(data: String): Char {
            this.data = data
            return this
        }

        override fun toString(): String {
            return data!!
        }
    }

    class EOF : Token() {
        init {
            type = TokenType.EOF
        }

        override fun reset(): Token {
            return this
        }
    }
}
