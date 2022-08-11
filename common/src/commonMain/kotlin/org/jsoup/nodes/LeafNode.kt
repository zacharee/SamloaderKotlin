package org.jsoup.nodes

abstract class LeafNode : Node() {
    var value // either a string value, or an attribute map (in the rare case multiple attributes are set)
            : Any? = null

    override fun hasAttributes(): Boolean {
        return value is Attributes
    }

    override fun attributes(): Attributes? {
        ensureAttributes()
        return value as Attributes?
    }

    private fun ensureAttributes() {
        if (!hasAttributes()) {
            val coreValue = value
            val attributes = Attributes()
            value = attributes
            if (coreValue != null) attributes.put(nodeName(), coreValue as String?)
        }
    }

    fun coreValue(): String? {
        return attr(nodeName())
    }

    fun coreValue(value: String?) {
        attr(nodeName(), value)
    }

    override fun attr(key: String?): String? {
        return if (!hasAttributes()) {
            if (nodeName() == key) value as String? else Node.Companion.EmptyString
        } else super.attr(key)
    }

    override fun attr(key: String?, value: String?): Node {
        if (!hasAttributes() && key == nodeName()) {
            this.value = value
        } else {
            ensureAttributes()
            super.attr(key, value)
        }
        return this
    }

    override fun hasAttr(key: String): Boolean {
        ensureAttributes()
        return super.hasAttr(key)
    }

    override fun removeAttr(key: String?): Node? {
        ensureAttributes()
        return super.removeAttr(key)
    }

    override fun absUrl(key: String?): String? {
        ensureAttributes()
        return super.absUrl(key)
    }

    override fun baseUri(): String? {
        return if (hasParent()) parent()!!.baseUri() else ""
    }

    override fun doSetBaseUri(baseUri: String?) {
        // noop
    }

    override fun childNodeSize(): Int {
        return 0
    }

    override fun empty(): Node {
        return this
    }

    override fun ensureChildNodes(): MutableList<Node> {
        return EmptyNodes
    }

    override fun doClone(parent: Node?): LeafNode {
        val clone = super.doClone(parent) as LeafNode

        // Object value could be plain string or attributes - need to clone
        if (hasAttributes()) clone.value = (value as Attributes?)!!.clone()
        return clone
    }
}
