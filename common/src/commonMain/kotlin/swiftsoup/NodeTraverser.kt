package swiftsoup

class NodeTraverser(private val visitor: NodeVisitor) {
    fun traverse(root: Node?) {
        var node = root
        var depth = 0

        while (node != null) {
            visitor.head(node, depth)
            if (node.childNodes.isNotEmpty()) {
                node = node.childNodes[0]
                depth++
            } else {
                while (node?.nextSibling() == null && depth > 0) {
                    visitor.tail(node!!, depth)
                    node = node.parentNode
                    depth--
                }
                visitor.tail(node!!, depth)
                if (node === root) {
                    break
                }
                node = node.nextSibling()
            }
        }
    }
}
