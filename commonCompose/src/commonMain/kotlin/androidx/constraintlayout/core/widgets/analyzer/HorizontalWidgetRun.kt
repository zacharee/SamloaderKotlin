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

import androidx.constraintlayout.core.widgets.*
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour
import kotlin.math.max
import kotlin.math.min

class HorizontalWidgetRun(widget: ConstraintWidget) : WidgetRun(widget) {
    init {
        start.mType = DependencyNode.Type.LEFT
        end.mType = DependencyNode.Type.RIGHT
        orientation = ConstraintWidget.Companion.HORIZONTAL
    }

    override fun toString(): String {
        return "HorizontalRun " + mWidget.debugName
    }

    public override fun clear() {
        mRunGroup = null
        start.clear()
        end.clear()
        mDimension.clear()
        isResolved = false
    }

    public override fun reset() {
        isResolved = false
        start.clear()
        start.resolved = false
        end.clear()
        end.resolved = false
        mDimension.resolved = false
    }

    public override fun supportsWrapComputation(): Boolean {
        return if (super.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
            super.mWidget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
        } else true
    }

    public override fun apply() {
        if (mWidget.measured) {
            mDimension.resolve(mWidget.width)
        }
        if (!mDimension.resolved) {
            super.mDimensionBehavior = mWidget.horizontalDimensionBehaviour
            if (super.mDimensionBehavior != DimensionBehaviour.MATCH_CONSTRAINT) {
                if (mDimensionBehavior == DimensionBehaviour.MATCH_PARENT) {
                    val parent = mWidget.parent
                    if (parent != null
                        && (parent.horizontalDimensionBehaviour == DimensionBehaviour.FIXED
                                || parent.horizontalDimensionBehaviour == DimensionBehaviour.MATCH_PARENT)
                    ) {
                        val resolvedDimension: Int = (parent.width
                                - mWidget.mLeft!!.margin - mWidget.mRight!!.margin)
                        addTarget(start, parent.mHorizontalRun!!.start, mWidget.mLeft!!.margin)
                        addTarget(end, parent.mHorizontalRun!!.end, -mWidget.mRight!!.margin)
                        mDimension.resolve(resolvedDimension)
                        return
                    }
                }
                if (mDimensionBehavior == DimensionBehaviour.FIXED) {
                    mDimension.resolve(mWidget.width)
                }
            }
        } else {
            if (mDimensionBehavior == DimensionBehaviour.MATCH_PARENT) {
                val parent = mWidget.parent
                if (parent != null
                    && (parent.horizontalDimensionBehaviour == DimensionBehaviour.FIXED
                            || parent.horizontalDimensionBehaviour == DimensionBehaviour.MATCH_PARENT)
                ) {
                    addTarget(start, parent.mHorizontalRun!!.start, mWidget.mLeft!!.margin)
                    addTarget(end, parent.mHorizontalRun!!.end, -mWidget.mRight!!.margin)
                    return
                }
            }
        }

        // three basic possibilities:
        // <-s-e->
        // <-s-e
        //   s-e->
        // and a variation if the dimension is not yet known:
        // <-s-d-e->
        // <-s<-d<-e
        //   s->d->e->
        if (mDimension.resolved && mWidget.measured) {
            if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.target != null
                && mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.target
                != null
            ) { // <-s-e->
                if (mWidget.isInHorizontalChain) {
                    start.mMargin = mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.margin
                    end.mMargin = -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.margin
                } else {
                    val startTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!)
                    if (startTarget != null) {
                        addTarget(
                            start, startTarget,
                            mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.margin
                        )
                    }
                    val endTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!)
                    if (endTarget != null) {
                        addTarget(
                            end, endTarget,
                            -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.margin
                        )
                    }
                    start.delegateToWidgetRun = true
                    end.delegateToWidgetRun = true
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.target
                != null
            ) { // <-s-e
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!)
                if (target != null) {
                    addTarget(
                        start, target,
                        mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.margin
                    )
                    addTarget(end, start, mDimension.value)
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.target
                != null
            ) {   //   s-e->
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!)
                if (target != null) {
                    addTarget(
                        end, target,
                        -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.margin
                    )
                    addTarget(start, end, -mDimension.value)
                }
            } else {
                // no connections, nothing to do.
                if (mWidget !is Helper && mWidget.parent != null && mWidget.getAnchor(
                        ConstraintAnchor.Type.CENTER
                    )!!.target == null
                ) {
                    val left: DependencyNode = mWidget.parent!!.mHorizontalRun!!.start
                    addTarget(start, left, mWidget.x)
                    addTarget(end, start, mDimension.value)
                }
            }
        } else {
            if (mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                when (mWidget.mMatchConstraintDefaultWidth) {
                    ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO -> {
                        if (mWidget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO) {
                            // need to look into both side
                            start.updateDelegate = this
                            end.updateDelegate = this
                            mWidget.mVerticalRun!!.start.updateDelegate = this
                            mWidget.mVerticalRun!!.end.updateDelegate = this
                            mDimension.updateDelegate = this
                            if (mWidget.isInVerticalChain) {
                                mDimension.mTargets.add(mWidget.mVerticalRun!!.mDimension)
                                mWidget.mVerticalRun!!.mDimension.mDependencies.add(mDimension)
                                mWidget.mVerticalRun!!.mDimension.updateDelegate = this
                                mDimension.mTargets.add(mWidget.mVerticalRun!!.start)
                                mDimension.mTargets.add(mWidget.mVerticalRun!!.end)
                                mWidget.mVerticalRun!!.start.mDependencies.add(mDimension)
                                mWidget.mVerticalRun!!.end.mDependencies.add(mDimension)
                            } else if (mWidget.isInHorizontalChain) {
                                mWidget.mVerticalRun!!.mDimension.mTargets.add(mDimension)
                                mDimension.mDependencies.add(mWidget.mVerticalRun!!.mDimension)
                            } else {
                                mWidget.mVerticalRun!!.mDimension.mTargets.add(mDimension)
                            }
                        }
                        // we have a ratio, but we depend on the other side computation
                        val targetDimension: DependencyNode = mWidget.mVerticalRun!!.mDimension
                        mDimension.mTargets.add(targetDimension)
                        targetDimension.mDependencies.add(mDimension)
                        mWidget.mVerticalRun!!.start.mDependencies.add(mDimension)
                        mWidget.mVerticalRun!!.end.mDependencies.add(mDimension)
                        mDimension.delegateToWidgetRun = true
                        mDimension.mDependencies.add(start)
                        mDimension.mDependencies.add(end)
                        start.mTargets.add(mDimension)
                        end.mTargets.add(mDimension)
                    }

                    ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT -> {

                        // we need to look up the parent dimension
                        val parent = mWidget.parent
                        if (parent != null) {
                            val targetDimension: DependencyNode = parent.mVerticalRun!!.mDimension
                            mDimension.mTargets.add(targetDimension)
                            targetDimension.mDependencies.add(mDimension)
                            mDimension.delegateToWidgetRun = true
                            mDimension.mDependencies.add(start)
                            mDimension.mDependencies.add(end)
                        }
                    }

                    ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD -> {}
                    else -> {}
                }
            }
            if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.target != null
                && mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.target
                != null
            ) { // <-s-d-e->
                if (mWidget.isInHorizontalChain) {
                    start.mMargin = mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.margin
                    end.mMargin = -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.margin
                } else {
                    val startTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!)
                    val endTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!)
                    if (false) {
                        if (startTarget != null) {
                            addTarget(
                                start, startTarget,
                                mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.margin
                            )
                        }
                        if (endTarget != null) {
                            addTarget(
                                end, endTarget,
                                -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)
                                    !!.margin
                            )
                        }
                    } else {
                        startTarget?.addDependency(this)
                        endTarget?.addDependency(this)
                    }
                    mRunType = RunType.CENTER
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.target
                != null
            ) { // <-s<-d<-e
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!)
                if (target != null) {
                    addTarget(
                        start, target,
                        mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_LEFT)!!.margin
                    )
                    addTarget(end, start, 1, mDimension)
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.target
                != null
            ) {   //   s->d->e->
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!)
                if (target != null) {
                    addTarget(
                        end, target,
                        -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_RIGHT)!!.margin
                    )
                    addTarget(start, end, -1, mDimension)
                }
            } else {
                // no connections, nothing to do.
                if (mWidget !is Helper && mWidget.parent != null) {
                    val left: DependencyNode = mWidget.parent!!.mHorizontalRun!!.start
                    addTarget(start, left, mWidget.x)
                    addTarget(end, start, 1, mDimension)
                }
            }
        }
    }

    private fun computeInsetRatio(
        dimensions: IntArray,
        x1: Int,
        x2: Int,
        y1: Int,
        y2: Int,
        ratio: Float,
        side: Int
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        when (side) {
            ConstraintWidget.Companion.UNKNOWN -> {
                val candidateX1 = (0.5f + dy * ratio).toInt()
                val candidateY2 = (0.5f + dx / ratio).toInt()
                if (candidateX1 <= dx && dy <= dy) {
                    dimensions[ConstraintWidget.Companion.HORIZONTAL] = candidateX1
                    dimensions[ConstraintWidget.Companion.VERTICAL] = dy
                } else if (dx <= dx && candidateY2 <= dy) {
                    dimensions[ConstraintWidget.Companion.HORIZONTAL] = dx
                    dimensions[ConstraintWidget.Companion.VERTICAL] = candidateY2
                }
            }

            ConstraintWidget.Companion.HORIZONTAL -> {
                val horizontalSide = (0.5f + dy * ratio).toInt()
                dimensions[ConstraintWidget.Companion.HORIZONTAL] = horizontalSide
                dimensions[ConstraintWidget.Companion.VERTICAL] = dy
            }

            ConstraintWidget.Companion.VERTICAL -> {
                val verticalSide = (0.5f + dx * ratio).toInt()
                dimensions[ConstraintWidget.Companion.HORIZONTAL] = dx
                dimensions[ConstraintWidget.Companion.VERTICAL] = verticalSide
            }

            else -> {}
        }
    }

    override fun update(dependency: Dependency?) {
        when (mRunType) {
            RunType.START -> {
                updateRunStart(dependency)
            }

            RunType.END -> {
                updateRunEnd(dependency)
            }

            RunType.CENTER -> {
                updateRunCenter(dependency, mWidget.mLeft!!, mWidget.mRight!!, ConstraintWidget.Companion.HORIZONTAL)
                return
            }

            else -> {}
        }
        if (!mDimension.resolved) {
            if (mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                when (mWidget.mMatchConstraintDefaultWidth) {
                    ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO -> {
                        if (mWidget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                            || mWidget.mMatchConstraintDefaultHeight
                            == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
                        ) {
                            val secondStart: DependencyNode = mWidget.mVerticalRun!!.start
                            val secondEnd: DependencyNode = mWidget.mVerticalRun!!.end
                            val s1 = mWidget.mLeft!!.target != null
                            val s2 = mWidget.mTop.target != null
                            val e1 = mWidget.mRight!!.target != null
                            val e2 = mWidget.mBottom.target != null
                            val definedSide: Int = mWidget.dimensionRatioSide
                            if (s1 && s2 && e1 && e2) {
                                val ratio: Float = mWidget.dimensionRatio
                                if (secondStart.resolved && secondEnd.resolved) {
                                    if (!(start.readyToSolve && end.readyToSolve)) {
                                        return
                                    }
                                    val x1: Int = start.mTargets.get(0)!!.value + start.mMargin
                                    val x2: Int = end.mTargets.get(0)!!.value - end.mMargin
                                    val y1 = secondStart.value + secondStart.mMargin
                                    val y2 = secondEnd.value - secondEnd.mMargin
                                    computeInsetRatio(
                                        sTempDimensions,
                                        x1, x2, y1, y2, ratio, definedSide
                                    )
                                    mDimension.resolve(sTempDimensions[ConstraintWidget.Companion.HORIZONTAL])
                                    mWidget.mVerticalRun!!.mDimension
                                        .resolve(sTempDimensions[ConstraintWidget.Companion.VERTICAL])
                                    return
                                }
                                if (start.resolved && end.resolved) {
                                    if (!(secondStart.readyToSolve && secondEnd.readyToSolve)) {
                                        return
                                    }
                                    val x1: Int = start.value + start.mMargin
                                    val x2: Int = end.value - end.mMargin
                                    val y1: Int = (secondStart.mTargets[0]!!.value
                                            + secondStart.mMargin)
                                    val y2: Int = secondEnd.mTargets[0]!!.value - secondEnd.mMargin
                                    computeInsetRatio(
                                        sTempDimensions,
                                        x1, x2, y1, y2, ratio, definedSide
                                    )
                                    mDimension.resolve(sTempDimensions[ConstraintWidget.Companion.HORIZONTAL])
                                    mWidget.mVerticalRun!!.mDimension
                                        .resolve(sTempDimensions[ConstraintWidget.Companion.VERTICAL])
                                }
                                if (!(start.readyToSolve && end.readyToSolve
                                            && secondStart.readyToSolve
                                            && secondEnd.readyToSolve)
                                ) {
                                    return
                                }
                                val x1: Int = start.mTargets.get(0)!!.value + start.mMargin
                                val x2: Int = end.mTargets.get(0)!!.value - end.mMargin
                                val y1: Int = secondStart.mTargets[0]!!.value + secondStart.mMargin
                                val y2: Int = secondEnd.mTargets[0]!!.value - secondEnd.mMargin
                                computeInsetRatio(
                                    sTempDimensions,
                                    x1, x2, y1, y2, ratio, definedSide
                                )
                                mDimension.resolve(sTempDimensions[ConstraintWidget.Companion.HORIZONTAL])
                                mWidget.mVerticalRun!!.mDimension.resolve(sTempDimensions[ConstraintWidget.Companion.VERTICAL])
                            } else if (s1 && e1) {
                                if (!(start.readyToSolve && end.readyToSolve)) {
                                    return
                                }
                                val ratio: Float = mWidget.dimensionRatio
                                val x1: Int = start.mTargets.get(0)!!.value + start.mMargin
                                val x2: Int = end.mTargets.get(0)!!.value - end.mMargin
                                when (definedSide) {
                                    ConstraintWidget.Companion.UNKNOWN, ConstraintWidget.Companion.HORIZONTAL -> {
                                        val dx = x2 - x1
                                        var ldx = getLimitedDimension(dx, ConstraintWidget.Companion.HORIZONTAL)
                                        val dy = (0.5f + ldx * ratio).toInt()
                                        val ldy = getLimitedDimension(dy, ConstraintWidget.Companion.VERTICAL)
                                        if (dy != ldy) {
                                            ldx = (0.5f + ldy / ratio).toInt()
                                        }
                                        mDimension.resolve(ldx)
                                        mWidget.mVerticalRun!!.mDimension.resolve(ldy)
                                    }

                                    ConstraintWidget.Companion.VERTICAL -> {
                                        val dx = x2 - x1
                                        var ldx = getLimitedDimension(dx, ConstraintWidget.Companion.HORIZONTAL)
                                        val dy = (0.5f + ldx / ratio).toInt()
                                        val ldy = getLimitedDimension(dy, ConstraintWidget.Companion.VERTICAL)
                                        if (dy != ldy) {
                                            ldx = (0.5f + ldy * ratio).toInt()
                                        }
                                        mDimension.resolve(ldx)
                                        mWidget.mVerticalRun!!.mDimension.resolve(ldy)
                                    }

                                    else -> {}
                                }
                            } else if (s2 && e2) {
                                if (!(secondStart.readyToSolve && secondEnd.readyToSolve)) {
                                    return
                                }
                                val ratio: Float = mWidget.dimensionRatio
                                val y1: Int = secondStart.mTargets[0]!!.value + secondStart.mMargin
                                val y2: Int = secondEnd.mTargets[0]!!.value - secondEnd.mMargin
                                when (definedSide) {
                                    ConstraintWidget.Companion.UNKNOWN, ConstraintWidget.Companion.VERTICAL -> {
                                        val dy = y2 - y1
                                        var ldy = getLimitedDimension(dy, ConstraintWidget.Companion.VERTICAL)
                                        val dx = (0.5f + ldy / ratio).toInt()
                                        val ldx = getLimitedDimension(dx, ConstraintWidget.Companion.HORIZONTAL)
                                        if (dx != ldx) {
                                            ldy = (0.5f + ldx * ratio).toInt()
                                        }
                                        mDimension.resolve(ldx)
                                        mWidget.mVerticalRun!!.mDimension.resolve(ldy)
                                    }

                                    ConstraintWidget.Companion.HORIZONTAL -> {
                                        val dy = y2 - y1
                                        var ldy = getLimitedDimension(dy, ConstraintWidget.Companion.VERTICAL)
                                        val dx = (0.5f + ldy * ratio).toInt()
                                        val ldx = getLimitedDimension(dx, ConstraintWidget.Companion.HORIZONTAL)
                                        if (dx != ldx) {
                                            ldy = (0.5f + ldx / ratio).toInt()
                                        }
                                        mDimension.resolve(ldx)
                                        mWidget.mVerticalRun!!.mDimension.resolve(ldy)
                                    }

                                    else -> {}
                                }
                            }
                        } else {
                            var size = 0
                            val ratioSide: Int = mWidget.dimensionRatioSide
                            when (ratioSide) {
                                ConstraintWidget.Companion.HORIZONTAL -> {
                                    size = (0.5f + mWidget.mVerticalRun!!.mDimension.value
                                            / mWidget.dimensionRatio).toInt()
                                }

                                ConstraintWidget.Companion.VERTICAL -> {
                                    size = (0.5f + mWidget.mVerticalRun!!.mDimension.value
                                            * mWidget.dimensionRatio).toInt()
                                }

                                ConstraintWidget.Companion.UNKNOWN -> {
                                    size = (0.5f + mWidget.mVerticalRun!!.mDimension.value
                                            * mWidget.dimensionRatio).toInt()
                                }

                                else -> {}
                            }
                            mDimension.resolve(size)
                        }
                    }

                    ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT -> {
                        val parent = mWidget.parent
                        if (parent != null) {
                            if (parent.mHorizontalRun!!.mDimension.resolved) {
                                val percent: Float = mWidget.mMatchConstraintPercentWidth
                                val targetDimensionValue: Int = parent.mHorizontalRun!!.mDimension.value
                                val size = (0.5f + targetDimensionValue * percent).toInt()
                                mDimension.resolve(size)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
        if (!(start.readyToSolve && end.readyToSolve)) {
            return
        }
        if (start.resolved && end.resolved && mDimension.resolved) {
            return
        }
        if (!mDimension.resolved && mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT && mWidget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD && !mWidget.isInHorizontalChain) {
            val startTarget = start.mTargets.get(0)!!
            val endTarget = end.mTargets.get(0)!!
            val startPos: Int = startTarget.value + start.mMargin
            val endPos: Int = endTarget.value + end.mMargin
            val distance = endPos - startPos
            start.resolve(startPos)
            end.resolve(endPos)
            mDimension.resolve(distance)
            return
        }
        if (!mDimension.resolved && mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT && matchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP) {
            if (start.mTargets.size > 0 && end.mTargets.size > 0) {
                val startTarget: DependencyNode = start.mTargets.get(0)!!
                val endTarget: DependencyNode = end.mTargets.get(0)!!
                val startPos: Int = startTarget.value + start.mMargin
                val endPos: Int = endTarget.value + end.mMargin
                val availableSpace = endPos - startPos
                var value: Int = min(availableSpace, mDimension.wrapValue)
                val max: Int = mWidget.mMatchConstraintMaxWidth
                val min: Int = mWidget.mMatchConstraintMinWidth
                value = max(min, value)
                if (max > 0) {
                    value = min(max, value)
                }
                mDimension.resolve(value)
            }
        }
        if (!mDimension.resolved) {
            return
        }
        // ready to solve, centering.
        val startTarget: DependencyNode = start.mTargets.get(0)!!
        val endTarget: DependencyNode = end.mTargets.get(0)!!
        var startPos: Int = startTarget.value + start.mMargin
        var endPos: Int = endTarget.value + end.mMargin
        var bias: Float = mWidget.horizontalBiasPercent
        if (startTarget === endTarget) {
            startPos = startTarget.value
            endPos = endTarget.value
            // TODO: this might be a nice feature to support, but I guess for now let's stay
            // compatible with 1.1
            bias = 0.5f
        }
        val distance: Int = endPos - startPos - mDimension.value
        start.resolve((0.5f + startPos + distance * bias).toInt())
        end.resolve(start.value + mDimension.value)
    }

    /**
     * @TODO: add description
     */
    public override fun applyToWidget() {
        if (start.resolved) {
            mWidget.x = (start.value)
        }
    }

    companion object {
        private val sTempDimensions = IntArray(2)
    }
}
