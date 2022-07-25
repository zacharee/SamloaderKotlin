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

import androidx.constraintlayout.core.widgets.ConstraintAnchor
import androidx.constraintlayout.core.widgets.ConstraintWidget
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour
import kotlin.math.max
import kotlin.math.min

abstract class WidgetRun(var mWidget: ConstraintWidget) : Dependency {
    var matchConstraintsType = 0
    var mRunGroup: RunGroup? = null
    var mDimensionBehavior: DimensionBehaviour? = null
    var mDimension = DimensionDependency(this)
    var orientation: Int = ConstraintWidget.HORIZONTAL
    var isResolved = false
    var start = DependencyNode(this)
    var end = DependencyNode(this)
    protected var mRunType = RunType.NONE
    abstract fun clear()
    abstract fun apply()
    abstract fun applyToWidget()
    abstract fun reset()
    abstract fun supportsWrapComputation(): Boolean
    val isDimensionResolved: Boolean
        get() = mDimension.resolved

    /**
     * @TODO: add description
     */
    val isCenterConnection: Boolean
        get() {
            var connections = 0
            var count = start.mTargets.size
            for (i in 0 until count) {
                val dependency = start.mTargets[i]
                if (dependency!!.mRun !== this) {
                    connections++
                }
            }
            count = end.mTargets.size
            for (i in 0 until count) {
                val dependency = end.mTargets[i]
                if (dependency!!.mRun !== this) {
                    connections++
                }
            }
            return connections >= 2
        }

    /**
     * @TODO: add description
     */
    fun wrapSize(direction: Int): Long {
        if (mDimension.resolved) {
            var size: Long = mDimension.value.toLong()
            if (isCenterConnection) { //start.targets.size() > 0 && end.targets.size() > 0) {
                size += (start.mMargin - end.mMargin).toLong()
            } else {
                if (direction == RunGroup.START) {
                    size += start.mMargin.toLong()
                } else {
                    size -= end.mMargin.toLong()
                }
            }
            return size
        }
        return 0
    }

    protected fun getTarget(anchor: ConstraintAnchor): DependencyNode? {
        if (anchor.target == null) {
            return null
        }
        var target: DependencyNode? = null
        val targetWidget: ConstraintWidget = anchor.target!!.owner
        when (anchor.target!!.type) {
            ConstraintAnchor.Type.LEFT -> {
                val run = targetWidget.mHorizontalRun
                target = run!!.start
            }

            ConstraintAnchor.Type.RIGHT -> {
                val run = targetWidget.mHorizontalRun
                target = run!!.end
            }

            ConstraintAnchor.Type.TOP -> {
                val run = targetWidget.mVerticalRun
                target = run!!.start
            }

            ConstraintAnchor.Type.BASELINE -> {
                val run = targetWidget.mVerticalRun
                target = run!!.baseline
            }

            ConstraintAnchor.Type.BOTTOM -> {
                val run = targetWidget.mVerticalRun
                target = run!!.end
            }

            else -> {}
        }
        return target
    }

    protected fun updateRunCenter(
        dependency: Dependency?,
        startAnchor: ConstraintAnchor,
        endAnchor: ConstraintAnchor,
        orientation: Int
    ) {
        val startTarget = getTarget(startAnchor)
        val endTarget = getTarget(endAnchor)
        if (!(startTarget!!.resolved && endTarget!!.resolved)) {
            return
        }
        var startPos = startTarget.value + startAnchor.margin
        var endPos = endTarget.value - endAnchor.margin
        val distance = endPos - startPos
        if (!mDimension.resolved
            && mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT
        ) {
            resolveDimension(orientation, distance)
        }
        if (!mDimension.resolved) {
            return
        }
        if (mDimension.value == distance) {
            start.resolve(startPos)
            end.resolve(endPos)
            return
        }

        // Otherwise, we have to center
        var bias =
            if (orientation == ConstraintWidget.HORIZONTAL) mWidget.horizontalBiasPercent else mWidget.verticalBiasPercent
        if (startTarget === endTarget) {
            startPos = startTarget.value
            endPos = endTarget.value
            // TODO: taking advantage of bias here would be a nice feature to support,
            // but for now let's stay compatible with 1.1
            bias = 0.5f
        }
        val availableDistance: Int = endPos - startPos - mDimension.value
        start.resolve((0.5f + startPos + availableDistance * bias).toInt())
        end.resolve(start.value + mDimension.value)
    }

    private fun resolveDimension(orientation: Int, distance: Int) {
        when (matchConstraintsType) {
            ConstraintWidget.MATCH_CONSTRAINT_SPREAD -> {
                mDimension.resolve(getLimitedDimension(distance, orientation))
            }

            ConstraintWidget.MATCH_CONSTRAINT_PERCENT -> {
                val parent = mWidget.parent
                if (parent != null) {
                    val run: WidgetRun =
                        if (orientation == ConstraintWidget.HORIZONTAL) parent.mHorizontalRun!! else parent.mVerticalRun!!
                    if (run.mDimension.resolved) {
                        val percent =
                            if (orientation == ConstraintWidget.HORIZONTAL) mWidget.mMatchConstraintPercentWidth else mWidget.mMatchConstraintPercentHeight
                        val targetDimensionValue: Int = run.mDimension.value
                        val size = (0.5f + targetDimensionValue * percent).toInt()
                        mDimension.resolve(getLimitedDimension(size, orientation))
                    }
                }
            }

            ConstraintWidget.MATCH_CONSTRAINT_WRAP -> {
                val wrapValue = getLimitedDimension(mDimension.wrapValue, orientation)
                mDimension.resolve(min(wrapValue, distance))
            }

            ConstraintWidget.MATCH_CONSTRAINT_RATIO -> {
                if ((mWidget.mHorizontalRun!!.mDimensionBehavior
                            == DimensionBehaviour.MATCH_CONSTRAINT) && mWidget.mHorizontalRun!!.matchConstraintsType == ConstraintWidget.MATCH_CONSTRAINT_RATIO && (mWidget.mVerticalRun!!.mDimensionBehavior
                            == DimensionBehaviour.MATCH_CONSTRAINT) && mWidget.mVerticalRun!!.matchConstraintsType == ConstraintWidget.MATCH_CONSTRAINT_RATIO
                ) {
                    // pof
                } else {
                    val run: WidgetRun? =
                        if (orientation == ConstraintWidget.HORIZONTAL) mWidget.mVerticalRun else mWidget.mHorizontalRun
                    if (run!!.mDimension.resolved) {
                        val ratio = mWidget.dimensionRatio
                        val value = if (orientation == ConstraintWidget.VERTICAL) {
                            (0.5f + run.mDimension.value / ratio).toInt()
                        } else {
                            (0.5f + ratio * run.mDimension.value).toInt()
                        }
                        mDimension.resolve(value)
                    }
                }
            }

            else -> {}
        }
    }

    protected fun updateRunStart(dependency: Dependency?) {}
    protected fun updateRunEnd(dependency: Dependency?) {}

    /**
     * @TODO: add description
     */
    override fun update(dependency: Dependency?) {}
    protected fun getLimitedDimension(dimension: Int, orientation: Int): Int {
        var dimension = dimension
        if (orientation == ConstraintWidget.HORIZONTAL) {
            val max = mWidget.mMatchConstraintMaxWidth
            val min = mWidget.mMatchConstraintMinWidth
            var value: Int = max(min, dimension)
            if (max > 0) {
                value = min(max, dimension)
            }
            if (value != dimension) {
                dimension = value
            }
        } else {
            val max = mWidget.mMatchConstraintMaxHeight
            val min = mWidget.mMatchConstraintMinHeight
            var value: Int = max(min, dimension)
            if (max > 0) {
                value = min(max, dimension)
            }
            if (value != dimension) {
                dimension = value
            }
        }
        return dimension
    }

    protected fun getTarget(anchor: ConstraintAnchor, orientation: Int): DependencyNode? {
        if (anchor.target == null) {
            return null
        }
        var target: DependencyNode? = null
        val targetWidget: ConstraintWidget = anchor.target!!.owner
        val run: WidgetRun? =
            if (orientation == ConstraintWidget.HORIZONTAL) targetWidget.mHorizontalRun else targetWidget.mVerticalRun
        when (anchor.target!!.type) {
            ConstraintAnchor.Type.TOP, ConstraintAnchor.Type.LEFT -> {
                target = run!!.start
            }

            ConstraintAnchor.Type.BOTTOM, ConstraintAnchor.Type.RIGHT -> {
                target = run!!.end
            }

            else -> {}
        }
        return target
    }

    protected fun addTarget(
        node: DependencyNode,
        target: DependencyNode,
        margin: Int
    ) {
        node.mTargets.add(target)
        node.mMargin = margin
        target.mDependencies.add(node)
    }

    protected fun addTarget(
        node: DependencyNode,
        target: DependencyNode,
        marginFactor: Int,
        dimensionDependency: DimensionDependency
    ) {
        node.mTargets.add(target)
        node.mTargets.add(mDimension)
        node.mMarginFactor = marginFactor
        node.mMarginDependency = dimensionDependency
        target.mDependencies.add(node)
        dimensionDependency.mDependencies.add(node)
    }

    /**
     * @TODO: add description
     */
    open val wrapDimension: Long
        get() = if (mDimension.resolved) {
            mDimension.value.toLong()
        } else 0

    enum class RunType {
        NONE, START, END, CENTER
    }
}
