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

class VerticalWidgetRun(widget: ConstraintWidget) : WidgetRun(widget) {
    var baseline = DependencyNode(this)
    var mBaselineDimension: DimensionDependency? = null

    init {
        start.mType = DependencyNode.Type.TOP
        end.mType = DependencyNode.Type.BOTTOM
        baseline.mType = DependencyNode.Type.BASELINE
        orientation = ConstraintWidget.Companion.VERTICAL
    }

    override fun toString(): String {
        return "VerticalRun " + mWidget.getDebugName()
    }

    public override fun clear() {
        mRunGroup = null
        start.clear()
        end.clear()
        baseline.clear()
        mDimension.clear()
        mResolved = false
    }

    public override fun reset() {
        mResolved = false
        start.clear()
        start.resolved = false
        end.clear()
        end.resolved = false
        baseline.clear()
        baseline.resolved = false
        mDimension.resolved = false
    }

    public override fun supportsWrapComputation(): Boolean {
        return if (super.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
            if (super.mWidget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) {
                true
            } else false
        } else true
    }

    override fun update(dependency: Dependency) {
        when (mRunType) {
            RunType.START -> {
                updateRunStart(dependency)
            }

            RunType.END -> {
                updateRunEnd(dependency)
            }

            RunType.CENTER -> {
                updateRunCenter(dependency, mWidget.mTop, mWidget.mBottom, ConstraintWidget.Companion.VERTICAL)
                return
            }

            else -> {}
        }
        if (FORCE_USE || dependency === mDimension) {
            if (mDimension.readyToSolve && !mDimension.resolved) {
                if (mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                    when (mWidget.mMatchConstraintDefaultHeight) {
                        ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO -> {
                            if (mWidget.mHorizontalRun!!.mDimension.resolved) {
                                var size = 0
                                val ratioSide: Int = mWidget.getDimensionRatioSide()
                                when (ratioSide) {
                                    ConstraintWidget.Companion.HORIZONTAL -> {
                                        size = (0.5f + mWidget.mHorizontalRun!!.mDimension.value
                                                * mWidget.getDimensionRatio()).toInt()
                                    }

                                    ConstraintWidget.Companion.VERTICAL -> {
                                        size = (0.5f + mWidget.mHorizontalRun!!.mDimension.value
                                                / mWidget.getDimensionRatio()).toInt()
                                    }

                                    ConstraintWidget.Companion.UNKNOWN -> {
                                        size = (0.5f + mWidget.mHorizontalRun!!.mDimension.value
                                                / mWidget.getDimensionRatio()).toInt()
                                    }

                                    else -> {}
                                }
                                mDimension.resolve(size)
                            }
                        }

                        ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT -> {
                            val parent: ConstraintWidget = mWidget.getParent()
                            if (parent != null) {
                                if (parent.mVerticalRun!!.mDimension.resolved) {
                                    val percent: Float = mWidget.mMatchConstraintPercentHeight
                                    val targetDimensionValue: Int = parent.mVerticalRun!!.mDimension.value
                                    val size = (0.5f + targetDimensionValue * percent).toInt()
                                    mDimension.resolve(size)
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
        if (!(start.readyToSolve && end.readyToSolve)) {
            return
        }
        if (start.resolved && end.resolved && mDimension.resolved) {
            return
        }
        if (!mDimension.resolved && mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT && mWidget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD && !mWidget.isInVerticalChain()) {
            val startTarget: DependencyNode = start.mTargets.get(0)
            val endTarget: DependencyNode = end.mTargets.get(0)
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
                val startTarget: DependencyNode = start.mTargets.get(0)
                val endTarget: DependencyNode = end.mTargets.get(0)
                val startPos: Int = startTarget.value + start.mMargin
                val endPos: Int = endTarget.value + end.mMargin
                val availableSpace = endPos - startPos
                if (availableSpace < mDimension.wrapValue) {
                    mDimension.resolve(availableSpace)
                } else {
                    mDimension.resolve(mDimension.wrapValue)
                }
            }
        }
        if (!mDimension.resolved) {
            return
        }
        // ready to solve, centering.
        if (start.mTargets.size > 0 && end.mTargets.size > 0) {
            val startTarget: DependencyNode = start.mTargets.get(0)
            val endTarget: DependencyNode = end.mTargets.get(0)
            var startPos: Int = startTarget.value + start.mMargin
            var endPos: Int = endTarget.value + end.mMargin
            var bias: Float = mWidget.getVerticalBiasPercent()
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
    }

    public override fun apply() {
        if (mWidget.measured) {
            mDimension.resolve(mWidget.getHeight())
        }
        if (!mDimension.resolved) {
            super.mDimensionBehavior = mWidget.getVerticalDimensionBehaviour()
            if (mWidget.hasBaseline()) {
                mBaselineDimension = BaselineDimensionDependency(this)
            }
            if (super.mDimensionBehavior != DimensionBehaviour.MATCH_CONSTRAINT) {
                if (mDimensionBehavior == DimensionBehaviour.MATCH_PARENT) {
                    val parent: ConstraintWidget = mWidget.getParent()
                    if (parent != null && parent.verticalDimensionBehaviour == DimensionBehaviour.FIXED) {
                        val resolvedDimension: Int = (parent.height
                                - mWidget.mTop.getMargin() - mWidget.mBottom.getMargin())
                        addTarget(start, parent.mVerticalRun!!.start, mWidget.mTop.getMargin())
                        addTarget(end, parent.mVerticalRun!!.end, -mWidget.mBottom.getMargin())
                        mDimension.resolve(resolvedDimension)
                        return
                    }
                }
                if (mDimensionBehavior == DimensionBehaviour.FIXED) {
                    mDimension.resolve(mWidget.getHeight())
                }
            }
        } else {
            if (mDimensionBehavior == DimensionBehaviour.MATCH_PARENT) {
                val parent: ConstraintWidget = mWidget.getParent()
                if (parent != null && parent.verticalDimensionBehaviour == DimensionBehaviour.FIXED) {
                    addTarget(start, parent.mVerticalRun!!.start, mWidget.mTop.getMargin())
                    addTarget(end, parent.mVerticalRun!!.end, -mWidget.mBottom.getMargin())
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
            if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).mTarget != null
                && mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).mTarget
                != null
            ) { // <-s-e->
                if (mWidget.isInVerticalChain()) {
                    start.mMargin = mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).getMargin()
                    end.mMargin = -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).getMargin()
                } else {
                    val startTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP)!!)
                    if (startTarget != null) {
                        addTarget(
                            start, startTarget,
                            mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).getMargin()
                        )
                    }
                    val endTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM)!!)
                    if (endTarget != null) {
                        addTarget(
                            end, endTarget,
                            -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).getMargin()
                        )
                    }
                    start.delegateToWidgetRun = true
                    end.delegateToWidgetRun = true
                }
                if (mWidget.hasBaseline()) {
                    addTarget(baseline, start, mWidget.getBaselineDistance())
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).mTarget != null) { // <-s-e
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP)!!)
                if (target != null) {
                    addTarget(
                        start, target,
                        mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).getMargin()
                    )
                    addTarget(end, start, mDimension.value)
                    if (mWidget.hasBaseline()) {
                        addTarget(baseline, start, mWidget.getBaselineDistance())
                    }
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).mTarget
                != null
            ) {   //   s-e->
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM)!!)
                if (target != null) {
                    addTarget(
                        end, target,
                        -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).getMargin()
                    )
                    addTarget(start, end, -mDimension.value)
                }
                if (mWidget.hasBaseline()) {
                    addTarget(baseline, start, mWidget.getBaselineDistance())
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BASELINE).mTarget
                != null
            ) {
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BASELINE)!!)
                if (target != null) {
                    addTarget(baseline, target, 0)
                    addTarget(start, baseline, -mWidget.getBaselineDistance())
                    addTarget(end, start, mDimension.value)
                }
            } else {
                // no connections, nothing to do.
                if (mWidget !is Helper && mWidget.getParent() != null && mWidget.getAnchor(ConstraintAnchor.Type.CENTER).mTarget == null) {
                    val top: DependencyNode = mWidget.getParent().mVerticalRun.start
                    addTarget(start, top, mWidget.getY())
                    addTarget(end, start, mDimension.value)
                    if (mWidget.hasBaseline()) {
                        addTarget(baseline, start, mWidget.getBaselineDistance())
                    }
                }
            }
        } else {
            if (!mDimension.resolved && mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                when (mWidget.mMatchConstraintDefaultHeight) {
                    ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO -> {
                        if (!mWidget.isInVerticalChain()) {
                            if (mWidget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO) {
                                // need to look into both side
                                // do nothing here --
                                //    let the HorizontalWidgetRun::update() deal with it.
                                break
                            }
                            // we have a ratio, but we depend on the other side computation
                            val targetDimension: DependencyNode = mWidget.mHorizontalRun!!.mDimension
                            mDimension.mTargets.add(targetDimension)
                            targetDimension.mDependencies.add(mDimension)
                            mDimension.delegateToWidgetRun = true
                            mDimension.mDependencies.add(start)
                            mDimension.mDependencies.add(end)
                        }
                    }

                    ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT -> {

                        // we need to look up the parent dimension
                        val parent: ConstraintWidget = mWidget.getParent() ?: break
                        val targetDimension: DependencyNode = parent.mVerticalRun!!.mDimension
                        mDimension.mTargets.add(targetDimension)
                        targetDimension.mDependencies.add(mDimension)
                        mDimension.delegateToWidgetRun = true
                        mDimension.mDependencies.add(start)
                        mDimension.mDependencies.add(end)
                    }

                    ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD -> {}
                    else -> {}
                }
            } else {
                mDimension.addDependency(this)
            }
            if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).mTarget != null
                && mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).mTarget
                != null
            ) { // <-s-d-e->
                if (mWidget.isInVerticalChain()) {
                    start.mMargin = mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).getMargin()
                    end.mMargin = -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).getMargin()
                } else {
                    val startTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP)!!)
                    val endTarget = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM)!!)
                    if (false) {
                        if (startTarget != null) {
                            addTarget(
                                start, startTarget,
                                mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).getMargin()
                            )
                        }
                        if (endTarget != null) {
                            addTarget(
                                end, endTarget,
                                -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM)
                                    .getMargin()
                            )
                        }
                    } else {
                        startTarget?.addDependency(this)
                        endTarget?.addDependency(this)
                    }
                    mRunType = RunType.CENTER
                }
                if (mWidget.hasBaseline()) {
                    addTarget(baseline, start, 1, mBaselineDimension!!)
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).mTarget
                != null
            ) { // <-s<-d<-e
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP)!!)
                if (target != null) {
                    addTarget(
                        start, target,
                        mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_TOP).getMargin()
                    )
                    addTarget(end, start, 1, mDimension)
                    if (mWidget.hasBaseline()) {
                        addTarget(baseline, start, 1, mBaselineDimension!!)
                    }
                    if (mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                        if (mWidget.getDimensionRatio() > 0) {
                            if (mWidget.mHorizontalRun!!.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                                mWidget.mHorizontalRun!!.mDimension.mDependencies.add(mDimension)
                                mDimension.mTargets.add(mWidget.mHorizontalRun!!.mDimension)
                                mDimension.updateDelegate = this
                            }
                        }
                    }
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).mTarget
                != null
            ) {   //   s->d->e->
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM)!!)
                if (target != null) {
                    addTarget(
                        end, target,
                        -mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BOTTOM).getMargin()
                    )
                    addTarget(start, end, -1, mDimension)
                    if (mWidget.hasBaseline()) {
                        addTarget(baseline, start, 1, mBaselineDimension!!)
                    }
                }
            } else if (mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BASELINE).mTarget != null) {
                val target = getTarget(mWidget.mListAnchors.get(ConstraintWidget.Companion.ANCHOR_BASELINE)!!)
                if (target != null) {
                    addTarget(baseline, target, 0)
                    addTarget(start, baseline, -1, mBaselineDimension!!)
                    addTarget(end, start, 1, mDimension)
                }
            } else {
                // no connections, nothing to do.
                if (mWidget !is Helper && mWidget.getParent() != null) {
                    val top: DependencyNode = mWidget.getParent().mVerticalRun.start
                    addTarget(start, top, mWidget.getY())
                    addTarget(end, start, 1, mDimension)
                    if (mWidget.hasBaseline()) {
                        addTarget(baseline, start, 1, mBaselineDimension!!)
                    }
                    if (mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                        if (mWidget.getDimensionRatio() > 0) {
                            if (mWidget.mHorizontalRun!!.mDimensionBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                                mWidget.mHorizontalRun!!.mDimension.mDependencies.add(mDimension)
                                mDimension.mTargets.add(mWidget.mHorizontalRun!!.mDimension)
                                mDimension.updateDelegate = this
                            }
                        }
                    }
                }
            }

            // if dimension has no dependency, mark it as ready to solve
            if (mDimension.mTargets.size == 0) {
                mDimension.readyToSolve = true
            }
        }
    }

    /**
     * @TODO: add description
     */
    public override fun applyToWidget() {
        if (start.resolved) {
            mWidget.setY(start.value)
        }
    }

    companion object {
        private const val FORCE_USE = true
    }
}
