package org.jsoup.nodes

abstract class LeafNode : Node() {
    var value // either a string value, or an attribute map (in the rare case multiple attributes are set)
            : Any? = null

    override fun hasAttributes(): Boolean {
        return value is Attributes
    }

    override fun attributes(): Attributes {
        ensureAttributes()
        return value as Attributes
    }

    private fun ensureAttributes() {
        if (!hasAttributes()) {
            val coreValue = value
            val attributes = Attributes()
            value = attributes
            if (coreValue != null) attributes.put(nodeName(), coreValue as String?)
        }
    }

    fun coreValue(): String {
        return attr(nodeName())
    }

    fun coreValue(value: String) {
        attr(nodeName(), value)
    }

    override fun attr(attributeKey: String): String {
        return if (!hasAttributes()) {
            if (nodeName() == attributeKey) value as String else EmptyString
        } else super.attr(attributeKey)
    }

    override fun attr(attributeKey: String, attributeValue: String?): Node {
        if (!hasAttributes() && attributeKey == nodeName()) {
            this.value = attributeValue
        } else {
            ensureAttributes()
            super.attr(attributeKey, attributeValue)
        }
        return this
    }

    override fun hasAttr(attributeKey: String): Boolean {
        ensureAttributes()
        return super.hasAttr(attributeKey)
    }

    override fun removeAttr(attributeKey: String): Node {
        ensureAttributes()
        return super.removeAttr(attributeKey)
    }

    override fun absUrl(attributeKey: String): String? {
        ensureAttributes()
        return super.absUrl(attributeKey)
    }

    override fun baseUri(): String {
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
