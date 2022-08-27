package swiftsoup

interface NodeVisitor {
    fun head(node: Node, depth: Int)
    fun tail(node: Node, depth: Int)
}
