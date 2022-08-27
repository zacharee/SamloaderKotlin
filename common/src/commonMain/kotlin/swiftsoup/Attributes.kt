package swiftsoup

class Attributes {
    companion object {
        const val dataPrefix = "data-"
    }

    private val attributes = arrayListOf<Attribute>()

    val size: Int
        get() = attributes.size

    val html: String
        get() = StringBuilder().apply {
            html(this, Document("").outputSettings)
        }.toString()

    operator fun get(key: String): String {
        return attributes.firstOrNull { it.key == key }?.value ?: ""
    }

    fun getIgnoreCase(key: String): String {
        return attributes.firstOrNull { it.key.equals(key, true) }?.value ?: ""
    }

    operator fun set(key: String, value: String) {
        val attr = Attribute(key, value)
        set(attr)
    }

    operator fun set(key: String, value: Boolean) {
        if (value) {
            set(BooleanAttribute(key))
        } else {
            remove(key)
        }
    }

    fun set(attribute: Attribute) {
        val ix = attributes.indexOfFirst { it.key == attribute.key }

        if (ix != -1) {
            attributes[ix] = attribute
        } else {
            attributes.add(attribute)
        }
    }

    fun remove(key: String, ignoreCase: Boolean = false) {
        attributes.removeAll { it.key.equals(key, ignoreCase) }
    }

    fun hasKey(key: String, ignoreCase: Boolean = false): Boolean {
        return attributes.any { it.key.equals(key, ignoreCase) }
    }

    fun addAll(incoming: Attributes?) {
        incoming?.attributes?.forEach {
            set(it)
        }
    }

    fun asList() = ArrayList(attributes)

    fun dataset(): Map<String, String> {
        val prefixLength = dataPrefix.length
        val pairs = attributes.filter { it.isDataAttribute }
            .map { it.key.substring(prefixLength) to it.value }
        return pairs.toMap()
    }

    fun html(accum: StringBuilder, out: OutputSettings) {
        attributes.forEach { attr ->
            accum.append(" ")
            attr.html(accum, out)
        }
    }

    fun lowercaseAllKeys() {
        for (i in attributes.indices) {
            attributes[i].key = attributes[i].key.lowercase()
        }
    }

    override fun toString(): String {
        return html
    }

    override fun equals(other: Any?): Boolean {
        return other is Attributes
                && attributes.containsAll(other.attributes)
                && other.attributes.containsAll(attributes)
    }

    override fun hashCode(): Int {
        return attributes.hashCode()
    }

    fun copy(): Attributes {
        return Attributes().also {
            it.attributes.addAll(attributes)
        }
    }
}
