package swiftsoup

class ParseSettings(
    private val preserveTagCase: Boolean,
    private val preserveAttributeCase: Boolean
) {
    companion object {
        val htmlDefault = ParseSettings(preserveTagCase = false, preserveAttributeCase = false)
        val preserveCase = ParseSettings(preserveTagCase = true, preserveAttributeCase = true)
    }

    fun normalizeTag(name: String): String {
        return name.trim().run {
            if (!preserveTagCase) lowercase()
            else this
        }
    }

    fun normalizeAttribute(name: String): String {
        return name.trim().run {
            if (!preserveAttributeCase) lowercase()
            else this
        }
    }

    fun normalizeAttributes(attributes: Attributes): Attributes {
        if (!preserveAttributeCase) {
            attributes.lowercaseAllKeys()
        }
        return attributes
    }
}
