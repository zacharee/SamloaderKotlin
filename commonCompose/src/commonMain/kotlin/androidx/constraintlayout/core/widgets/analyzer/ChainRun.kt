/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.core.widgets.analyzer

import androidx.constraintlayout.core.widgets.ConstraintWidget
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer

class ChainRun(widget: ConstraintWidget, orientation: Int) : WidgetRun(widget) {
    var mWidgets: java.util.ArrayList<WidgetRun> = java.util.ArrayList<WidgetRun>()
    private var mChainStyle = 0

    init {
        this.orientation = orientation
        build()
    }

    override fun toString(): String {
        val log: java.lang.StringBuilder = java.lang.StringBuilder("ChainRun ")
        log.append(if (orientation == ConstraintWidget.Companion.HORIZONTAL) "horizontal : " else "vertical : ")
        for (run in mWidgets) {
            log.append("<")
            log.append(run)
            log.append("> ")
        }
        return log.toString()
    }

    public override fun supportsWrapComputation(): Boolean {
        val count: Int = mWidgets.size
        for (i in 0 until count) {
            val run: WidgetRun = mWidgets.get(i)
            if (!run.supportsWrapComputation()) {
                return false
            }
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override val wrapDimension: Long
        get() {
            val count: Int = mWidgets.size
            var wrapDimension: Long = 0
            for (i in 0 until count) {
                val run: WidgetRun = mWidgets.get(i)
                wrapDimension += run.start.mMargin.toLong()
                wrapDimension += run.wrapDimension
                wrapDimension += run.end.mMargin.toLong()
            }
            return wrapDimension
        }

    private fun build() {
        var current = mWidget
        var previous: ConstraintWidget? = current.getPreviousChainMember(orientation)
        while (previous != null) {
            current = previous
            previous = current.getPreviousChainMember(orientation)
        }
        mWidget = current // first element of the chain
        mWidgets.add(current.getRun(orientation))
        var next: ConstraintWidget? = current.getNextChainMember(orientation)
        while (next != null) {
            current = next
            mWidgets.add(current.getRun(orientation))
            next = current.getNextChainMember(orientation)
        }
        for (run in mWidgets) {
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                run.mWidget.horizontalChainRun = this
            } else if (orientation == ConstraintWidget.Companion.VERTICAL) {
                run.mWidget.verticalChainRun = this
            }
        }
        val isInRtl = (orientation == ConstraintWidget.Companion.HORIZONTAL
                && (mWidget.getParent() as ConstraintWidgetContainer).isRtl)
        if (isInRtl && mWidgets.size > 1) {
            mWidget = mWidgets.get(mWidgets.size - 1).mWidget
        }
        mChainStyle =
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) mWidget.getHorizontalChainStyle() else mWidget.getVerticalChainStyle()
    }

    public override fun clear() {
        mRunGroup = null
        for (run in mWidgets) {
            run.clear()
        }
    }

    public override fun reset() {
        start.resolved = false
        end.resolved = false
    }

    override fun update(dependency: Dependency?) {
        if (!(start.resolved && end.resolved)) {
            return
        }
        val parent: ConstraintWidget = mWidget.getParent()
        var isInRtl = false
        if (parent is ConstraintWidgetContainer) {
            isInRtl = (parent as ConstraintWidgetContainer).isRtl
        }
        val distance: Int = end.value - start.value
        var size = 0
        var numMatchConstraints = 0
        var weights = 0f
        var numVisibleWidgets = 0
        val count: Int = mWidgets.size
        // let's find the first visible widget...
        var firstVisibleWidget = -1
        for (i in 0 until count) {
            val run: WidgetRun = mWidgets.get(i)
            if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                continue
            }
            firstVisibleWidget = i
            break
        }
        // now the last visible widget...
        var lastVisibleWidget = -1
        for (i in count - 1 downTo 0) {
            val run: WidgetRun = mWidgets.get(i)
            if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                continue
            }
            lastVisibleWidget = i
            break
        }
        for (j in 0..1) {
            for (i in 0 until count) {
                val run: WidgetRun = mWidgets.get(i)
                if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    continue
                }
                numVisibleWidgets++
                if (i > 0 && i >= firstVisibleWidget) {
                    size += run.start.mMargin
                }
                var dimension: Int = run.mDimension.value
                var treatAsFixed = run.mDimensionBehavior != DimensionBehaviour.MATCH_CONSTRAINT
                if (treatAsFixed) {
                    if (orientation == ConstraintWidget.Companion.HORIZONTAL
                        && !run.mWidget.mHorizontalRun!!.mDimension.resolved
                    ) {
                        return
                    }
                    if (orientation == ConstraintWidget.Companion.VERTICAL && !run.mWidget.mVerticalRun!!.mDimension.resolved) {
                        return
                    }
                } else if (run.matchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP && j == 0) {
                    treatAsFixed = true
                    dimension = run.mDimension.wrapValue
                    numMatchConstraints++
                } else if (run.mDimension.resolved) {
                    treatAsFixed = true
                }
                if (!treatAsFixed) { // only for the first pass
                    numMatchConstraints++
                    val weight: Float = run.mWidget.mWeight.get(orientation)
                    if (weight >= 0) {
                        weights += weight
                    }
                } else {
                    size += dimension
                }
                if (i < count - 1 && i < lastVisibleWidget) {
                    size += -run.end.mMargin
                }
            }
            if (size < distance || numMatchConstraints == 0) {
                break // we are good to go!
            }
            // otherwise, let's do another pass with using match_constraints
            numVisibleWidgets = 0
            numMatchConstraints = 0
            size = 0
            weights = 0f
        }
        var position: Int = start.value
        if (isInRtl) {
            position = end.value
        }
        if (size > distance) {
            if (isInRtl) {
                position += (0.5f + (size - distance) / 2f).toInt()
            } else {
                position -= (0.5f + (size - distance) / 2f).toInt()
            }
        }
        var matchConstraintsDimension = 0
        if (numMatchConstraints > 0) {
            matchConstraintsDimension = (0.5f + (distance - size) / numMatchConstraints.toFloat()).toInt()
            var appliedLimits = 0
            for (i in 0 until count) {
                val run: WidgetRun = mWidgets.get(i)
                if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    continue
                }
                if (run.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT && !run.mDimension.resolved) {
                    var dimension = matchConstraintsDimension
                    if (weights > 0) {
                        val weight: Float = run.mWidget.mWeight.get(orientation)
                        dimension = (0.5f + weight * (distance - size) / weights).toInt()
                    }
                    var max: Int
                    var min: Int
                    var value = dimension
                    if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        max = run.mWidget.mMatchConstraintMaxWidth
                        min = run.mWidget.mMatchConstraintMinWidth
                    } else {
                        max = run.mWidget.mMatchConstraintMaxHeight
                        min = run.mWidget.mMatchConstraintMinHeight
                    }
                    if (run.matchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP) {
                        value = java.lang.Math.min(value, run.mDimension.wrapValue)
                    }
                    value = java.lang.Math.max(min, value)
                    if (max > 0) {
                        value = java.lang.Math.min(max, value)
                    }
                    if (value != dimension) {
                        appliedLimits++
                        dimension = value
                    }
                    run.mDimension.resolve(dimension)
                }
            }
            if (appliedLimits > 0) {
                numMatchConstraints -= appliedLimits
                // we have to recompute the sizes
                size = 0
                for (i in 0 until count) {
                    val run: WidgetRun = mWidgets.get(i)
                    if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                        continue
                    }
                    if (i > 0 && i >= firstVisibleWidget) {
                        size += run.start.mMargin
                    }
                    size += run.mDimension.value
                    if (i < count - 1 && i < lastVisibleWidget) {
                        size += -run.end.mMargin
                    }
                }
            }
            if (mChainStyle == ConstraintWidget.Companion.CHAIN_PACKED && appliedLimits == 0) {
                mChainStyle = ConstraintWidget.Companion.CHAIN_SPREAD
            }
        }
        if (size > distance) {
            mChainStyle = ConstraintWidget.Companion.CHAIN_PACKED
        }
        if (numVisibleWidgets > 0 && numMatchConstraints == 0 && firstVisibleWidget == lastVisibleWidget) {
            // only one widget of fixed size to display...
            mChainStyle = ConstraintWidget.Companion.CHAIN_PACKED
        }
        if (mChainStyle == ConstraintWidget.Companion.CHAIN_SPREAD_INSIDE) {
            var gap = 0
            if (numVisibleWidgets > 1) {
                gap = (distance - size) / (numVisibleWidgets - 1)
            } else if (numVisibleWidgets == 1) {
                gap = (distance - size) / 2
            }
            if (numMatchConstraints > 0) {
                gap = 0
            }
            for (i in 0 until count) {
                var index = i
                if (isInRtl) {
                    index = count - (i + 1)
                }
                val run: WidgetRun = mWidgets.get(index)
                if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    run.start.resolve(position)
                    run.end.resolve(position)
                    continue
                }
                if (i > 0) {
                    if (isInRtl) {
                        position -= gap
                    } else {
                        position += gap
                    }
                }
                if (i > 0 && i >= firstVisibleWidget) {
                    if (isInRtl) {
                        position -= run.start.mMargin
                    } else {
                        position += run.start.mMargin
                    }
                }
                if (isInRtl) {
                    run.end.resolve(position)
                } else {
                    run.start.resolve(position)
                }
                var dimension: Int = run.mDimension.value
                if (run.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT
                    && run.matchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
                ) {
                    dimension = run.mDimension.wrapValue
                }
                if (isInRtl) {
                    position -= dimension
                } else {
                    position += dimension
                }
                if (isInRtl) {
                    run.start.resolve(position)
                } else {
                    run.end.resolve(position)
                }
                run.mResolved = true
                if (i < count - 1 && i < lastVisibleWidget) {
                    if (isInRtl) {
                        position -= -run.end.mMargin
                    } else {
                        position += -run.end.mMargin
                    }
                }
            }
        } else if (mChainStyle == ConstraintWidget.Companion.CHAIN_SPREAD) {
            var gap = (distance - size) / (numVisibleWidgets + 1)
            if (numMatchConstraints > 0) {
                gap = 0
            }
            for (i in 0 until count) {
                var index = i
                if (isInRtl) {
                    index = count - (i + 1)
                }
                val run: WidgetRun = mWidgets.get(index)
                if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    run.start.resolve(position)
                    run.end.resolve(position)
                    continue
                }
                if (isInRtl) {
                    position -= gap
                } else {
                    position += gap
                }
                if (i > 0 && i >= firstVisibleWidget) {
                    if (isInRtl) {
                        position -= run.start.mMargin
                    } else {
                        position += run.start.mMargin
                    }
                }
                if (isInRtl) {
                    run.end.resolve(position)
                } else {
                    run.start.resolve(position)
                }
                var dimension: Int = run.mDimension.value
                if (run.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT
                    && run.matchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
                ) {
                    dimension = java.lang.Math.min(dimension, run.mDimension.wrapValue)
                }
                if (isInRtl) {
                    position -= dimension
                } else {
                    position += dimension
                }
                if (isInRtl) {
                    run.start.resolve(position)
                } else {
                    run.end.resolve(position)
                }
                if (i < count - 1 && i < lastVisibleWidget) {
                    if (isInRtl) {
                        position -= -run.end.mMargin
                    } else {
                        position += -run.end.mMargin
                    }
                }
            }
        } else if (mChainStyle == ConstraintWidget.Companion.CHAIN_PACKED) {
            var bias: Float =
                if (orientation == ConstraintWidget.Companion.HORIZONTAL) mWidget.getHorizontalBiasPercent() else mWidget.getVerticalBiasPercent()
            if (isInRtl) {
                bias = 1 - bias
            }
            var gap = (0.5f + (distance - size) * bias).toInt()
            if (gap < 0 || numMatchConstraints > 0) {
                gap = 0
            }
            if (isInRtl) {
                position -= gap
            } else {
                position += gap
            }
            for (i in 0 until count) {
                var index = i
                if (isInRtl) {
                    index = count - (i + 1)
                }
                val run: WidgetRun = mWidgets.get(index)
                if (run.mWidget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    run.start.resolve(position)
                    run.end.resolve(position)
                    continue
                }
                if (i > 0 && i >= firstVisibleWidget) {
                    if (isInRtl) {
                        position -= run.start.mMargin
                    } else {
                        position += run.start.mMargin
                    }
                }
                if (isInRtl) {
                    run.end.resolve(position)
                } else {
                    run.start.resolve(position)
                }
                var dimension: Int = run.mDimension.value
                if (run.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT
                    && run.matchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
                ) {
                    dimension = run.mDimension.wrapValue
                }
                if (isInRtl) {
                    position -= dimension
                } else {
                    position += dimension
                }
                if (isInRtl) {
                    run.start.resolve(position)
                } else {
                    run.end.resolve(position)
                }
                if (i < count - 1 && i < lastVisibleWidget) {
                    if (isInRtl) {
                        position -= -run.end.mMargin
                    } else {
                        position += -run.end.mMargin
                    }
                }
            }
        }
    }

    /**
     * @TODO: add description
     */
    public override fun applyToWidget() {
        for (i in mWidgets.indices) {
            val run: WidgetRun = mWidgets.get(i)
            run.applyToWidget()
        }
    }

    private val firstVisibleWidget: ConstraintWidget?
        private get() {
            for (i in mWidgets.indices) {
                val run: WidgetRun = mWidgets.get(i)
                if (run.mWidget.getVisibility() != ConstraintWidget.Companion.GONE) {
                    return run.mWidget
                }
            }
            return null
        }
    private val lastVisibleWidget: ConstraintWidget?
        private get() {
            for (i in mWidgets.indices.reversed()) {
                val run: WidgetRun = mWidgets.get(i)
                if (run.mWidget.getVisibility() != ConstraintWidget.Companion.GONE) {
                    return run.mWidget
                }
            }
            return null
        }

    public override fun apply() {
        for (run in mWidgets) {
            run.apply()
        }
        val count: Int = mWidgets.size
        if (count < 1) {
            return
        }

        // get the first and last element of the chain
        val firstWidget: ConstraintWidget = mWidgets.get(0).mWidget
        val lastWidget: ConstraintWidget = mWidgets.get(count - 1).mWidget
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            val startAnchor = firstWidget.mLeft
            val endAnchor = lastWidget.mRight
            val startTarget = getTarget(startAnchor!!, ConstraintWidget.Companion.HORIZONTAL)
            var startMargin: Int = startAnchor.margin
            val firstVisibleWidget = firstVisibleWidget
            if (firstVisibleWidget != null) {
                startMargin = firstVisibleWidget.mLeft.getMargin()
            }
            startTarget?.let { addTarget(start, it, startMargin) }
            val endTarget = getTarget(endAnchor!!, ConstraintWidget.Companion.HORIZONTAL)
            var endMargin: Int = endAnchor.margin
            val lastVisibleWidget = lastVisibleWidget
            if (lastVisibleWidget != null) {
                endMargin = lastVisibleWidget.mRight.getMargin()
            }
            if (endTarget != null) {
                addTarget(end, endTarget, -endMargin)
            }
        } else {
            val startAnchor = firstWidget.mTop
            val endAnchor = lastWidget.mBottom
            val startTarget = getTarget(startAnchor!!, ConstraintWidget.Companion.VERTICAL)
            var startMargin: Int = startAnchor.margin
            val firstVisibleWidget = firstVisibleWidget
            if (firstVisibleWidget != null) {
                startMargin = firstVisibleWidget.mTop.getMargin()
            }
            startTarget?.let { addTarget(start, it, startMargin) }
            val endTarget = getTarget(endAnchor!!, ConstraintWidget.Companion.VERTICAL)
            var endMargin: Int = endAnchor.margin
            val lastVisibleWidget = lastVisibleWidget
            if (lastVisibleWidget != null) {
                endMargin = lastVisibleWidget.mBottom.getMargin()
            }
            if (endTarget != null) {
                addTarget(end, endTarget, -endMargin)
            }
        }
        start.updateDelegate = this
        end.updateDelegate = this
    }
}
