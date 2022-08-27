package swiftsoup

import com.soywiz.korio.lang.assert

abstract class Node(baseUri: String? = null, open var attributes: Attributes? = if (baseUri == null) null else Attributes()) {
    companion object {
        private const val abs = "abs:"
        private const val empty = ""
        private val EMPTY_NODES = arrayListOf<Node>()
    }

    open var parentNode: Node? = null
    var childNodes: ArrayList<Node> = EMPTY_NODES
    var baseUri: String? = baseUri?.trim()
        private set

    var siblingIndex = 0
        private set

    abstract fun nodeName(): String

    open fun attr(attributeKey: String): String {
        val v = attributes!!.getIgnoreCase(attributeKey)
        return if (v.isNotEmpty()) {
            v
        } else if (attributeKey.lowercase().startsWith(abs)) {
            absUrl(attributeKey.substring(abs.length))
        } else {
            empty
        }
    }

    open fun attr(attributeKey: String, attributeValue: String): Node {
        attributes?.set(attributeKey, attributeValue)
        return this
    }

    open fun hasAttr(attributeKey: String): Boolean {
        if (attributes == null) return false

        if (attributeKey.startsWith(abs)) {
            val key = attributeKey.substring(abs.length)

            try {
                val abs = absUrl(key)
                if (attributes!!.hasKey(key, true) && abs != empty) {
                    return true
                }
            } catch (e: Exception) {
                return false
            }
        }

        return attributes!!.hasKey(attributeKey, true)
    }

    open fun removeAttr(attributeKey: String): Node {
        attributes?.remove(attributeKey, true)
        return this
    }

    open fun setBaseUri(baseUri: String) {
        val visitor = object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                node.baseUri = baseUri
            }

            override fun tail(node: Node, depth: Int) {}
        }

        traverse(visitor)
    }

    open fun absUrl(attributeKey: String): String {
        assert(attributeKey.isNotEmpty())

        return if (!hasAttr(attributeKey)) {
            empty
        } else {
            StringUtil.resolve(baseUri!!, relUrl = attr(attributeKey))
        }
    }

    open fun childNodesCopy(): ArrayList<Node> {
        return childNodes.map { it.copy() }
    }

    open fun ownerDocument(): Document? {
        return if (this is Document) {
            this
        } else if (parentNode == null) {
            null
        } else {
            parentNode!!.ownerDocument()
        }
    }

    open fun remove() {
        parentNode?.removeChild(this)
    }

    open fun before(html: String): Node {
        addSiblingHtml(siblingIndex, html)
        return this
    }

    open fun before(node: Node): Node {
        assert(parentNode != null)

        parentNode?.addChildren(siblingIndex, node)
        return this
    }

    open fun after(html: String): Node {
        addSiblingHtml(siblingIndex + 1, html)
        return this
    }

    open fun after(node: Node): Node {
        assert(parentNode != null)

        parentNode?.addChildren(siblingIndex + 1 , node)
        return this
    }

    private fun addSiblingHtml(index: Int, html: String) {
        assert(parentNode != null)

        val context = parentNode as? Element
        val nodes = Parser.parseFragment(html, context, baseUri!!)
        parentNode?.addChildren(index, nodes)
    }

    open fun wrap(html: String): Node? {
        assert(html.isNotEmpty())

        val context = parentNode as? Element
        var wrapChildren: List<Node> = Parser.parseFragment(html, context, baseUri!!)
        val wrapNode = if (wrapChildren.isNotEmpty()) wrapChildren[0] else null

        if (wrapNode == null || wrapNode !is Element) {
            return null
        }

        val wrap = wrapNode
        val deepest = getDeepChild(wrap)
        parentNode?.replaceChild(this, wrap)
        wrapChildren = wrapChildren.filter { it != wrap }
        deepest.addChildren(this)

        if (wrapChildren.isNotEmpty()) {
            wrapChildren.forEachIndexed { _, node ->
                node.parentNode?.removeChild(node)
                wrap.appendChild(node)
            }
        }

        return this
    }

    fun unwrap(): Node? {
        assert(parentNode != null)

        val firstChild = childNodes.firstOrNull()
        parentNode?.addChildren(siblingIndex, childNodes)
        this.remove()

        return firstChild
    }

    private fun getDeepChild(el: Element): Element {
        return if (el.children.isNotEmpty()) {
            getDeepChild(el.children()[0])
        } else {
            el
        }
    }

    fun replaceWith(input: Node) {
        assert(parentNode != null)
        parentNode?.replaceChild(this, input)
    }

    fun setParentNode(parentNode: Node) {
        if (this.parentNode != null) {
            this.parentNode?.removeChild(this)
        }

        this.parentNode = parentNode
    }

    fun replaceChild(out: Node, input: Node) {
        assert(out.parentNode == this)

        if (input.parentNode != null) {
            input.parentNode?.removeChild(input)
        }

        val index = out.siblingIndex
        childNodes[index] = input
        input.parentNode = this
        input.siblingIndex = index
        out.parentNode = null
    }

    fun removeChild(out: Node) {
        assert(out.parentNode == this)
        val index = out.siblingIndex
        childNodes.removeAt(index)
        reindexChildren(index)
        out.parentNode = null
    }

    fun addChildren(vararg children: Node) {
        addChildren(children)
    }

    fun addChildren(children: Array<out Node>) {
        children.forEach { node ->
            reparentChild(node)
            ensureChildNodes()
            childNodes.add(node)
            node.siblingIndex = childNodes.size - 1
        }
    }

    fun addChildren(index: Int, vararg children: Node) {
        addChildren(index, children)
    }

    fun addChildren(index: Int, children: Array<out Node>) {
        ensureChildNodes()
        children.reversed().forEachIndexed { _, node ->
            reparentChild(node)
            childNodes.add(index, node)
            reindexChildren(index)
        }
    }

    fun ensureChildNodes() {

    }

    fun reparentChild(child: Node) {
        if (child.parentNode != null) {
            child.parentNode?.removeChild(child)
        }

        child.parentNode = this
    }

    private fun reindexChildren(start: Int) {
        for (i in start until childNodes.size) {
            childNodes[i].siblingIndex = i
        }
    }

    open fun siblingNodes(): List<Node> {
        val nodes = parentNode?.childNodes ?: return listOf()
        val siblings = arrayListOf<Node>()

        nodes.forEach { node ->
            if (node !== this) {
                siblings.add(node)
            }
        }

        return siblings
    }

    open fun nextSibling(): Node? {
        val siblings = parentNode?.childNodes ?: return null

        val index = siblingIndex + 1
        return siblings.getOrNull(index)
    }

    open fun previousSibling(): Node? {
        return parentNode?.childNodes?.getOrNull(siblingIndex - 1)
    }

    fun traverse(nodeVisitor: NodeVisitor): Node {
        val traverser = NodeTraverser(nodeVisitor)
        traverser.traverse(this)
        return this
    }

    open fun outerHtml(): String {
        return StringBuilder(128).apply { outerHtml(this) }.toString()
    }

    fun outerHtml(accum: StringBuilder) {
        NodeTraverser(OuterHtmlVisitor(accum, outputSettings)).traverse(this)
    }

    val outputSettings: OutputSettings
        get() = if (ownerDocument() != null) ownerDocument()!!.outputSettings else Document(empty).outputSettings

    abstract fun outerHtmlHead(accum: StringBuilder, depth: Int, out: OutputSettings)
    abstract fun outerHtmlTail(accum: StringBuilder, depth: Int, out: OutputSettings)

    open fun html(appendable: StringBuilder): StringBuilder {
        outerHtml(appendable)
        return appendable
    }

    fun indent(accum: StringBuilder, depth: Int, out: OutputSettings) {
        accum.append('\n').append(StringUtil.padding(depth * out.indentAmount))
    }

    open fun hasSameValue(o: Node): Boolean {
        if (o === this) return true

        return outerHtml() == o.outerHtml()
    }

    abstract fun copy(parent: Node? = null): Node

    fun copy(clone: Node): Node {
        val thisClone = copy(clone = clone, parent = null)

        val nodesToProcess = arrayListOf<Node>()
        nodesToProcess.add(thisClone)

        while (!nodesToProcess.isEmpty()) {
            val currParent = nodesToProcess.removeFirst()

            for (i in 0 until currParent.childNodes.size) {
                val childClone = currParent.childNodes[i].copy(parent = currParent)
                currParent.childNodes[i] = childClone
                nodesToProcess.add(childClone)
            }
        }

        return thisClone
    }

    fun copy(clone: Node, parent: Node?): Node {
        clone.parentNode = parent
        clone.siblingIndex = if (parent == null) 0 else siblingIndex
        clone.attributes = if (attributes != null) attributes?.copy() else null
        clone.baseUri = baseUri
        clone.childNodes = ArrayList(childNodes)

        return clone
    }

    private class OuterHtmlVisitor(private val accum: StringBuilder, private val out: OutputSettings) : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            node.outerHtmlHead(accum, depth, out)
        }

        override fun tail(node: Node, depth: Int) {
            node.outerHtmlTail(accum, depth, out)
        }
    }
}
