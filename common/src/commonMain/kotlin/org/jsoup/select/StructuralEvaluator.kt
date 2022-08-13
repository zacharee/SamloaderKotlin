package org.jsoup.select

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

/**
 * Base structural evaluator.
 */
internal abstract class StructuralEvaluator : Evaluator() {
    open var evaluator: Evaluator? = null

    internal class Root : Evaluator() {
        override fun matches(root: Element, element: Element): Boolean {
            return root === element
        }
    }

    internal class Has constructor(override var evaluator: Evaluator?) : StructuralEvaluator() {
        private val finder = Collector.FirstFinder(evaluator)

        override fun matches(root: Element, element: Element): Boolean {
            // for :has, we only want to match children (or below), not the input element. And we want to minimize GCs
            for (i in 0 until element.childNodeSize()) {
                val node: Node = element.childNode(i)
                if (node is Element) {
                    val match: Element? = finder.find(element, node)
                    if (match != null) return true
                }
            }
            return false
        }

        override fun toString(): String {
            return ":has($evaluator)"
        }
    }

    internal class Not constructor(override var evaluator: Evaluator?) : StructuralEvaluator() {
        override fun matches(root: Element, element: Element): Boolean {
            return !evaluator!!.matches(root, element)
        }

        override fun toString(): String {
            return ":not($evaluator)"
        }
    }

    internal class Parent constructor(evaluator: Evaluator?) : StructuralEvaluator() {
        init {
            this.evaluator = evaluator
        }

        override fun matches(root: Element, element: Element): Boolean {
            if (root === element) return false
            var parent: Element? = element.parent()
            while (parent != null) {
                if (evaluator!!.matches(root, parent)) return true
                if (parent === root) break
                parent = parent.parent()
            }
            return false
        }

        override fun toString(): String {
            return "$evaluator "
        }
    }

    internal class ImmediateParent constructor(evaluator: Evaluator?) : StructuralEvaluator() {
        init {
            this.evaluator = evaluator
        }

        override fun matches(root: Element, element: Element): Boolean {
            if (root === element) return false
            val parent: Element? = element.parent()
            return parent != null && evaluator!!.matches(root, parent)
        }

        override fun toString(): String {
            return "$evaluator > "
        }
    }

    internal class PreviousSibling constructor(evaluator: Evaluator?) : StructuralEvaluator() {
        init {
            this.evaluator = evaluator
        }

        override fun matches(root: Element, element: Element): Boolean {
            if (root === element) return false
            var prev: Element? = element.previousElementSibling()
            while (prev != null) {
                if (evaluator!!.matches(root, prev)) return true
                prev = prev.previousElementSibling()
            }
            return false
        }

        override fun toString(): String {
            return "$evaluator ~ "
        }
    }

    internal class ImmediatePreviousSibling constructor(evaluator: Evaluator?) : StructuralEvaluator() {
        init {
            this.evaluator = evaluator
        }

        override fun matches(root: Element, element: Element): Boolean {
            if (root === element) return false
            val prev: Element? = element.previousElementSibling()
            return prev != null && evaluator!!.matches(root, prev)
        }

        override fun toString(): String {
            return "$evaluator + "
        }
    }
}
