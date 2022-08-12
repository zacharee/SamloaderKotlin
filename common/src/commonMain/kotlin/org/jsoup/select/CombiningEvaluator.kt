package org.jsoup.select

import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Element

/**
 * Base combining (and, or) evaluator.
 */
abstract class CombiningEvaluator internal constructor() : Evaluator() {
    val evaluators: ArrayList<Evaluator?>
    var num: Int = 0

    init {
        evaluators = ArrayList()
    }

    internal constructor(evaluators: Collection<Evaluator?>) : this() {
        this.evaluators.addAll(evaluators)
        updateNumEvaluators()
    }

    fun rightMostEvaluator(): Evaluator? {
        return if (num > 0) evaluators.get(num - 1) else null
    }

    fun replaceRightMostEvaluator(replacement: Evaluator?) {
        evaluators.set(num - 1, replacement)
    }

    fun updateNumEvaluators() {
        // used so we don't need to bash on size() for every match test
        num = evaluators.size
    }

    class And internal constructor(evaluators: Collection<Evaluator?>) : CombiningEvaluator(evaluators) {
        internal constructor(vararg evaluators: Evaluator?) : this(evaluators.toList()) {}

        public override fun matches(root: Element, node: Element): Boolean {
            for (i in num - 1 downTo 0) { // process backwards so that :matchText is evaled earlier, to catch parent query. todo - should redo matchText to virtually expand during match, not pre-match (see SelectorTest#findBetweenSpan)
                val s: Evaluator? = evaluators.get(i)
                if (!s!!.matches(root, node)) return false
            }
            return true
        }

        public override fun toString(): String {
            return StringUtil.join(evaluators, "")
        }
    }

    class Or : CombiningEvaluator {
        /**
         * Create a new Or evaluator. The initial evaluators are ANDed together and used as the first clause of the OR.
         * @param evaluators initial OR clause (these are wrapped into an AND evaluator).
         */
        internal constructor(evaluators: Collection<Evaluator?>) : super() {
            if (num > 1) this.evaluators.add(And(evaluators)) else  // 0 or 1
                this.evaluators.addAll(evaluators)
            updateNumEvaluators()
        }

        internal constructor(vararg evaluators: Evaluator?) : this(evaluators = evaluators.toList()) {}
        internal constructor() : super() {}

        fun add(e: Evaluator?) {
            evaluators.add(e)
            updateNumEvaluators()
        }

        public override fun matches(root: Element, node: Element): Boolean {
            for (i in 0 until num) {
                val s: Evaluator? = evaluators.get(i)
                if (s!!.matches(root, node)) return true
            }
            return false
        }

        public override fun toString(): String {
            return StringUtil.join(evaluators, ", ")
        }
    }
}
