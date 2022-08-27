package swiftsoup

import com.soywiz.korio.lang.assert

open class Element(var tag: Tag, baseUri: String, attributes: Attributes = Attributes()) : Node(baseUri, attributes) {
    companion object {
        private const val classString = "class"
        private const val emptyString = ""
        private const val idString = "id"
        private const val rootString = "#root"

        //private const val classSplit : Pattern = Pattern("\\s+")
        private const val classSplit = "\\s+"
    }

    override fun nodeName(): String {
        return tag.tagName
    }

    fun tagName() = tag.tagName
    fun tagNameNormal() = tag.tagNameNormal

    fun tagName(tagName: String): Element {
        assert(tagName.isNotEmpty())
        tag = Tag.valueOf(tagName, ParseSettings.preserveCase)
        return this
    }

    fun id(): String {
        val attributes = attributes ?: return emptyString

        return try {
            attributes.getIgnoreCase(idString)
        } catch (e: Exception) {
            emptyString
        }
    }

    override fun attr(attributeKey: String, attributeValue: String): Node {
        attributes?.set(attributeKey, attributeValue)
        return this
    }

    fun parent(): Element? {
        return parentNode as? Element
    }

    fun parents(): Elements {

    }

    fun accumulateParents(el: Element, parent: Elements) {

    }

    fun child(index: Int): Element {

    }

    fun children(): Elements {

    }

    fun textNodes(): List<TextNode> {

    }

    fun dataNodes(): List<DataNode> {

    }

    fun select(cssQuery: String): Elements {

    }

    fun iS(cssQuery: String): Boolean {

    }

    fun iS(evaluator: Evaluator): Boolean {

    }

    fun appendChild(child: Node): Element {

    }

    fun prependChild(child: Node): Element
}
