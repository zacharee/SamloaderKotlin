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

class DependencyGraph(private val mWidgetcontainer: ConstraintWidgetContainer) {
    private var mNeedBuildGraph = true
    private var mNeedRedoMeasures = true
    private val mContainer: ConstraintWidgetContainer
    private val mRuns: java.util.ArrayList<WidgetRun?> = java.util.ArrayList<WidgetRun>()

    // TODO: Unused, should we delete?
    private val mRunGroups: java.util.ArrayList<RunGroup> = java.util.ArrayList<RunGroup>()
    private var mMeasurer: BasicMeasure.Measurer? = null
    private val mMeasure = BasicMeasure.Measure()
    fun setMeasurer(measurer: BasicMeasure.Measurer?) {
        mMeasurer = measurer
    }

    private fun computeWrap(container: ConstraintWidgetContainer, orientation: Int): Int {
        val count: Int = mGroups.size
        var wrapSize: Long = 0
        for (i in 0 until count) {
            val run: RunGroup = mGroups.get(i)
            val size = run.computeWrapSize(container, orientation)
            wrapSize = java.lang.Math.max(wrapSize, size)
        }
        return wrapSize.toInt()
    }

    /**
     * Find and mark terminal widgets (trailing widgets) -- they are the only
     * ones we need to care for wrap_content checks
     */
    fun defineTerminalWidgets(
        horizontalBehavior: DimensionBehaviour,
        verticalBehavior: DimensionBehaviour
    ) {
        if (mNeedBuildGraph) {
            buildGraph()
            if (USE_GROUPS) {
                var hasBarrier = false
                for (widget in mWidgetcontainer.mChildren) {
                    widget.isTerminalWidget[ConstraintWidget.Companion.HORIZONTAL] = true
                    widget.isTerminalWidget[ConstraintWidget.Companion.VERTICAL] = true
                    if (widget is Barrier) {
                        hasBarrier = true
                    }
                }
                if (!hasBarrier) {
                    for (group in mGroups) {
                        group.defineTerminalWidgets(
                            horizontalBehavior == DimensionBehaviour.WRAP_CONTENT,
                            verticalBehavior == DimensionBehaviour.WRAP_CONTENT
                        )
                    }
                }
            }
        }
    }

    /**
     * Try to measure the layout by solving the graph of constraints directly
     *
     * @param optimizeWrap use the wrap_content optimizer
     * @return true if all widgets have been resolved
     */
    fun directMeasure(optimizeWrap: Boolean): Boolean {
        var optimizeWrap = optimizeWrap
        optimizeWrap = optimizeWrap and USE_GROUPS
        if (mNeedBuildGraph || mNeedRedoMeasures) {
            for (widget in mWidgetcontainer.mChildren) {
                widget.ensureWidgetRuns()
                widget.measured = false
                widget.mHorizontalRun!!.reset()
                widget.mVerticalRun!!.reset()
            }
            mWidgetcontainer.ensureWidgetRuns()
            mWidgetcontainer.measured = false
            mWidgetcontainer.mHorizontalRun.reset()
            mWidgetcontainer.mVerticalRun.reset()
            mNeedRedoMeasures = false
        }
        val avoid = basicMeasureWidgets(mContainer)
        if (avoid) {
            return false
        }
        mWidgetcontainer.setX(0)
        mWidgetcontainer.setY(0)
        val originalHorizontalDimension: DimensionBehaviour =
            mWidgetcontainer.getDimensionBehaviour(ConstraintWidget.Companion.HORIZONTAL)
        val originalVerticalDimension: DimensionBehaviour =
            mWidgetcontainer.getDimensionBehaviour(ConstraintWidget.Companion.VERTICAL)
        if (mNeedBuildGraph) {
            buildGraph()
        }
        val x1: Int = mWidgetcontainer.getX()
        val y1: Int = mWidgetcontainer.getY()
        mWidgetcontainer.mHorizontalRun.start.resolve(x1)
        mWidgetcontainer.mVerticalRun.start.resolve(y1)

        // Let's do the easy steps first -- anything that can be immediately measured
        // Whatever is left for the dimension will be match_constraints.
        measureWidgets()

        // If we have to support wrap, let's see if we can compute it directly
        if (originalHorizontalDimension == DimensionBehaviour.WRAP_CONTENT
            || originalVerticalDimension == DimensionBehaviour.WRAP_CONTENT
        ) {
            if (optimizeWrap) {
                for (run in mRuns) {
                    if (!run.supportsWrapComputation()) {
                        optimizeWrap = false
                        break
                    }
                }
            }
            if (optimizeWrap && originalHorizontalDimension == DimensionBehaviour.WRAP_CONTENT) {
                mWidgetcontainer.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
                mWidgetcontainer.setWidth(computeWrap(mWidgetcontainer, ConstraintWidget.Companion.HORIZONTAL))
                mWidgetcontainer.mHorizontalRun.mDimension.resolve(mWidgetcontainer.getWidth())
            }
            if (optimizeWrap && originalVerticalDimension == DimensionBehaviour.WRAP_CONTENT) {
                mWidgetcontainer.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
                mWidgetcontainer.setHeight(computeWrap(mWidgetcontainer, ConstraintWidget.Companion.VERTICAL))
                mWidgetcontainer.mVerticalRun.mDimension.resolve(mWidgetcontainer.getHeight())
            }
        }
        var checkRoot = false

        // Now, depending on our own dimension behavior, we may want to solve
        // one dimension before the other
        if (mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.HORIZONTAL)
            == DimensionBehaviour.FIXED
            || mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.HORIZONTAL)
            == DimensionBehaviour.MATCH_PARENT
        ) {

            // solve horizontal dimension
            val x2: Int = x1 + mWidgetcontainer.getWidth()
            mWidgetcontainer.mHorizontalRun.end.resolve(x2)
            mWidgetcontainer.mHorizontalRun.mDimension.resolve(x2 - x1)
            measureWidgets()
            if (mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.VERTICAL) == DimensionBehaviour.FIXED
                || mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.VERTICAL) == DimensionBehaviour.MATCH_PARENT
            ) {
                val y2: Int = y1 + mWidgetcontainer.getHeight()
                mWidgetcontainer.mVerticalRun.end.resolve(y2)
                mWidgetcontainer.mVerticalRun.mDimension.resolve(y2 - y1)
            }
            measureWidgets()
            checkRoot = true
        } else {
            // we'll bail out to the solver...
        }

        // Let's apply what we did resolve
        for (run in mRuns) {
            if (run.mWidget === mWidgetcontainer && !run.mResolved) {
                continue
            }
            run.applyToWidget()
        }
        var allResolved = true
        for (run in mRuns) {
            if (!checkRoot && run.mWidget === mWidgetcontainer) {
                continue
            }
            if (!run.start.resolved) {
                allResolved = false
                break
            }
            if (!run.end.resolved && run !is GuidelineReference) {
                allResolved = false
                break
            }
            if (!run.mDimension.resolved
                && run !is ChainRun && run !is GuidelineReference
            ) {
                allResolved = false
                break
            }
        }
        mWidgetcontainer.setHorizontalDimensionBehaviour(originalHorizontalDimension)
        mWidgetcontainer.setVerticalDimensionBehaviour(originalVerticalDimension)
        return allResolved
    }

    /**
     * @TODO: add description
     */
    fun directMeasureSetup(optimizeWrap: Boolean): Boolean {
        if (mNeedBuildGraph) {
            for (widget in mWidgetcontainer.mChildren) {
                widget.ensureWidgetRuns()
                widget.measured = false
                widget.mHorizontalRun!!.mDimension.resolved = false
                widget.mHorizontalRun.mResolved = false
                widget.mHorizontalRun!!.reset()
                widget.mVerticalRun!!.mDimension.resolved = false
                widget.mVerticalRun.mResolved = false
                widget.mVerticalRun!!.reset()
            }
            mWidgetcontainer.ensureWidgetRuns()
            mWidgetcontainer.measured = false
            mWidgetcontainer.mHorizontalRun.mDimension.resolved = false
            mWidgetcontainer.mHorizontalRun.mResolved = false
            mWidgetcontainer.mHorizontalRun.reset()
            mWidgetcontainer.mVerticalRun.mDimension.resolved = false
            mWidgetcontainer.mVerticalRun.mResolved = false
            mWidgetcontainer.mVerticalRun.reset()
            buildGraph()
        }
        val avoid = basicMeasureWidgets(mContainer)
        if (avoid) {
            return false
        }
        mWidgetcontainer.setX(0)
        mWidgetcontainer.setY(0)
        mWidgetcontainer.mHorizontalRun.start.resolve(0)
        mWidgetcontainer.mVerticalRun.start.resolve(0)
        return true
    }

    /**
     * @TODO: add description
     */
    fun directMeasureWithOrientation(optimizeWrap: Boolean, orientation: Int): Boolean {
        var optimizeWrap = optimizeWrap
        optimizeWrap = optimizeWrap and USE_GROUPS
        val originalHorizontalDimension: DimensionBehaviour =
            mWidgetcontainer.getDimensionBehaviour(ConstraintWidget.Companion.HORIZONTAL)
        val originalVerticalDimension: DimensionBehaviour =
            mWidgetcontainer.getDimensionBehaviour(ConstraintWidget.Companion.VERTICAL)
        val x1: Int = mWidgetcontainer.getX()
        val y1: Int = mWidgetcontainer.getY()

        // If we have to support wrap, let's see if we can compute it directly
        if (optimizeWrap && (originalHorizontalDimension == DimensionBehaviour.WRAP_CONTENT
                    || originalVerticalDimension == DimensionBehaviour.WRAP_CONTENT)
        ) {
            for (run in mRuns) {
                if (run.orientation == orientation
                    && !run.supportsWrapComputation()
                ) {
                    optimizeWrap = false
                    break
                }
            }
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                if (optimizeWrap && originalHorizontalDimension == DimensionBehaviour.WRAP_CONTENT) {
                    mWidgetcontainer.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
                    mWidgetcontainer.setWidth(computeWrap(mWidgetcontainer, ConstraintWidget.Companion.HORIZONTAL))
                    mWidgetcontainer.mHorizontalRun.mDimension.resolve(mWidgetcontainer.getWidth())
                }
            } else {
                if (optimizeWrap && originalVerticalDimension == DimensionBehaviour.WRAP_CONTENT) {
                    mWidgetcontainer.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
                    mWidgetcontainer.setHeight(computeWrap(mWidgetcontainer, ConstraintWidget.Companion.VERTICAL))
                    mWidgetcontainer.mVerticalRun.mDimension.resolve(mWidgetcontainer.getHeight())
                }
            }
        }
        var checkRoot = false

        // Now, depending on our own dimension behavior, we may want to solve
        // one dimension before the other
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            if (mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.HORIZONTAL) == DimensionBehaviour.FIXED
                || mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.HORIZONTAL) == DimensionBehaviour.MATCH_PARENT
            ) {
                val x2: Int = x1 + mWidgetcontainer.getWidth()
                mWidgetcontainer.mHorizontalRun.end.resolve(x2)
                mWidgetcontainer.mHorizontalRun.mDimension.resolve(x2 - x1)
                checkRoot = true
            }
        } else {
            if (mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.VERTICAL) == DimensionBehaviour.FIXED
                || mWidgetcontainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.VERTICAL) == DimensionBehaviour.MATCH_PARENT
            ) {
                val y2: Int = y1 + mWidgetcontainer.getHeight()
                mWidgetcontainer.mVerticalRun.end.resolve(y2)
                mWidgetcontainer.mVerticalRun.mDimension.resolve(y2 - y1)
                checkRoot = true
            }
        }
        measureWidgets()

        // Let's apply what we did resolve
        for (run in mRuns) {
            if (run.orientation != orientation) {
                continue
            }
            if (run.mWidget === mWidgetcontainer && !run.mResolved) {
                continue
            }
            run.applyToWidget()
        }
        var allResolved = true
        for (run in mRuns) {
            if (run.orientation != orientation) {
                continue
            }
            if (!checkRoot && run.mWidget === mWidgetcontainer) {
                continue
            }
            if (!run.start.resolved) {
                allResolved = false
                break
            }
            if (!run.end.resolved) {
                allResolved = false
                break
            }
            if (run !is ChainRun && !run.mDimension.resolved) {
                allResolved = false
                break
            }
        }
        mWidgetcontainer.setHorizontalDimensionBehaviour(originalHorizontalDimension)
        mWidgetcontainer.setVerticalDimensionBehaviour(originalVerticalDimension)
        return allResolved
    }

    /**
     * Convenience function to fill in the measure spec
     *
     * @param widget the widget to measure
     */
    private fun measure(
        widget: ConstraintWidget,
        horizontalBehavior: DimensionBehaviour,
        horizontalDimension: Int,
        verticalBehavior: DimensionBehaviour,
        verticalDimension: Int
    ) {
        mMeasure.horizontalBehavior = horizontalBehavior
        mMeasure.verticalBehavior = verticalBehavior
        mMeasure.horizontalDimension = horizontalDimension
        mMeasure.verticalDimension = verticalDimension
        mMeasurer!!.measure(widget, mMeasure)
        widget.width = mMeasure.measuredWidth
        widget.height = mMeasure.measuredHeight
        widget.hasBaseline = mMeasure.measuredHasBaseline
        widget.baselineDistance = mMeasure.measuredBaseline
    }

    private fun basicMeasureWidgets(constraintWidgetContainer: ConstraintWidgetContainer): Boolean {
        for (widget in constraintWidgetContainer.mChildren) {
            var horizontal: DimensionBehaviour = widget.mListDimensionBehaviors[ConstraintWidget.Companion.HORIZONTAL]
            var vertical: DimensionBehaviour = widget.mListDimensionBehaviors[ConstraintWidget.Companion.VERTICAL]
            if (widget.visibility == ConstraintWidget.Companion.GONE) {
                widget.measured = true
                continue
            }

            // Basic validation
            // TODO: might move this earlier in the process
            if (widget.mMatchConstraintPercentWidth < 1 && horizontal == DimensionBehaviour.MATCH_CONSTRAINT) {
                widget.mMatchConstraintDefaultWidth = ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
            }
            if (widget.mMatchConstraintPercentHeight < 1 && vertical == DimensionBehaviour.MATCH_CONSTRAINT) {
                widget.mMatchConstraintDefaultHeight = ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
            }
            if (widget.dimensionRatio > 0) {
                if (horizontal == DimensionBehaviour.MATCH_CONSTRAINT
                    && (vertical == DimensionBehaviour.WRAP_CONTENT || vertical == DimensionBehaviour.FIXED)
                ) {
                    widget.mMatchConstraintDefaultWidth = ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
                } else if (vertical == DimensionBehaviour.MATCH_CONSTRAINT
                    && (horizontal == DimensionBehaviour.WRAP_CONTENT || horizontal == DimensionBehaviour.FIXED)
                ) {
                    widget.mMatchConstraintDefaultHeight = ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
                } else if (horizontal == DimensionBehaviour.MATCH_CONSTRAINT && vertical == DimensionBehaviour.MATCH_CONSTRAINT) {
                    if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) {
                        widget.mMatchConstraintDefaultWidth = ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
                    }
                    if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) {
                        widget.mMatchConstraintDefaultHeight = ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
                    }
                }
            }
            if (horizontal == DimensionBehaviour.MATCH_CONSTRAINT
                && widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
            ) {
                if (widget.mLeft.mTarget == null || widget.mRight.mTarget == null) {
                    horizontal = DimensionBehaviour.WRAP_CONTENT
                }
            }
            if (vertical == DimensionBehaviour.MATCH_CONSTRAINT
                && widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
            ) {
                if (widget.mTop.mTarget == null || widget.mBottom.mTarget == null) {
                    vertical = DimensionBehaviour.WRAP_CONTENT
                }
            }
            widget.mHorizontalRun!!.mDimensionBehavior = horizontal
            widget.mHorizontalRun!!.matchConstraintsType = widget.mMatchConstraintDefaultWidth
            widget.mVerticalRun!!.mDimensionBehavior = vertical
            widget.mVerticalRun!!.matchConstraintsType = widget.mMatchConstraintDefaultHeight
            if (horizontal == DimensionBehaviour.MATCH_PARENT || horizontal == DimensionBehaviour.FIXED || horizontal == DimensionBehaviour.WRAP_CONTENT && (vertical == DimensionBehaviour.MATCH_PARENT || vertical == DimensionBehaviour.FIXED || vertical) == DimensionBehaviour.WRAP_CONTENT) {
                var width = widget.width
                if (horizontal == DimensionBehaviour.MATCH_PARENT) {
                    width = (constraintWidgetContainer.getWidth()
                            - widget.mLeft!!.mMargin - widget.mRight!!.mMargin)
                    horizontal = DimensionBehaviour.FIXED
                }
                var height = widget.height
                if (vertical == DimensionBehaviour.MATCH_PARENT) {
                    height = (constraintWidgetContainer.getHeight()
                            - widget.mTop.mMargin - widget.mBottom.mMargin)
                    vertical = DimensionBehaviour.FIXED
                }
                measure(widget, horizontal, width, vertical, height)
                widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                widget.mVerticalRun!!.mDimension.resolve(widget.height)
                widget.measured = true
                continue
            }
            if (horizontal == DimensionBehaviour.MATCH_CONSTRAINT && (vertical == DimensionBehaviour.WRAP_CONTENT || vertical == DimensionBehaviour.FIXED)) {
                if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO) {
                    if (vertical == DimensionBehaviour.WRAP_CONTENT) {
                        measure(widget, DimensionBehaviour.WRAP_CONTENT, 0, DimensionBehaviour.WRAP_CONTENT, 0)
                    }
                    val height = widget.height
                    val width = (height * widget.mDimensionRatio + 0.5f).toInt()
                    measure(widget, DimensionBehaviour.FIXED, width, DimensionBehaviour.FIXED, height)
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                    continue
                } else if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP) {
                    measure(widget, DimensionBehaviour.WRAP_CONTENT, 0, vertical, 0)
                    widget.mHorizontalRun!!.mDimension.wrapValue = widget.width
                    continue
                } else if (widget.mMatchConstraintDefaultWidth
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
                ) {
                    if (constraintWidgetContainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.HORIZONTAL) == DimensionBehaviour.FIXED
                        || constraintWidgetContainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.HORIZONTAL)
                        == DimensionBehaviour.MATCH_PARENT
                    ) {
                        val percent = widget.mMatchConstraintPercentWidth
                        val width: Int = (0.5f + percent * constraintWidgetContainer.getWidth()).toInt()
                        val height = widget.height
                        measure(widget, DimensionBehaviour.FIXED, width, vertical, height)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                } else {
                    // let's verify we have both constraints
                    if (widget.mListAnchors[ConstraintWidget.Companion.ANCHOR_LEFT].mTarget == null
                        || widget.mListAnchors[ConstraintWidget.Companion.ANCHOR_RIGHT].mTarget == null
                    ) {
                        measure(widget, DimensionBehaviour.WRAP_CONTENT, 0, vertical, 0)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                }
            }
            if (vertical == DimensionBehaviour.MATCH_CONSTRAINT
                && (horizontal == DimensionBehaviour.WRAP_CONTENT || horizontal == DimensionBehaviour.FIXED)
            ) {
                if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO) {
                    if (horizontal == DimensionBehaviour.WRAP_CONTENT) {
                        measure(widget, DimensionBehaviour.WRAP_CONTENT, 0, DimensionBehaviour.WRAP_CONTENT, 0)
                    }
                    val width = widget.width
                    var ratio = widget.mDimensionRatio
                    if (widget.dimensionRatioSide == ConstraintWidget.Companion.UNKNOWN) {
                        ratio = 1f / ratio
                    }
                    val height = (width * ratio + 0.5f).toInt()
                    measure(widget, DimensionBehaviour.FIXED, width, DimensionBehaviour.FIXED, height)
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                    continue
                } else if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP) {
                    measure(widget, horizontal, 0, DimensionBehaviour.WRAP_CONTENT, 0)
                    widget.mVerticalRun!!.mDimension.wrapValue = widget.height
                    continue
                } else if (widget.mMatchConstraintDefaultHeight
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
                ) {
                    if (constraintWidgetContainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.VERTICAL) == DimensionBehaviour.FIXED
                        || constraintWidgetContainer.mListDimensionBehaviors.get(ConstraintWidget.Companion.VERTICAL)
                        == DimensionBehaviour.MATCH_PARENT
                    ) {
                        val percent = widget.mMatchConstraintPercentHeight
                        val width = widget.width
                        val height: Int = (0.5f + percent * constraintWidgetContainer.getHeight()).toInt()
                        measure(widget, horizontal, width, DimensionBehaviour.FIXED, height)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                } else {
                    // let's verify we have both constraints
                    if (widget.mListAnchors[ConstraintWidget.Companion.ANCHOR_TOP].mTarget == null
                        || widget.mListAnchors[ConstraintWidget.Companion.ANCHOR_BOTTOM].mTarget
                        == null
                    ) {
                        measure(widget, DimensionBehaviour.WRAP_CONTENT, 0, vertical, 0)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                }
            }
            if (horizontal == DimensionBehaviour.MATCH_CONSTRAINT && vertical == DimensionBehaviour.MATCH_CONSTRAINT) {
                if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
                    || widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
                ) {
                    measure(widget, DimensionBehaviour.WRAP_CONTENT, 0, DimensionBehaviour.WRAP_CONTENT, 0)
                    widget.mHorizontalRun!!.mDimension.wrapValue = widget.width
                    widget.mVerticalRun!!.mDimension.wrapValue = widget.height
                } else if ((widget.mMatchConstraintDefaultHeight
                            == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT) && (widget.mMatchConstraintDefaultWidth
                            == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT) && constraintWidgetContainer.mListDimensionBehaviors.get(
                        ConstraintWidget.Companion.HORIZONTAL
                    ) == DimensionBehaviour.FIXED && constraintWidgetContainer.mListDimensionBehaviors.get(
                        ConstraintWidget.Companion.VERTICAL
                    ) == DimensionBehaviour.FIXED
                ) {
                    val horizPercent = widget.mMatchConstraintPercentWidth
                    val vertPercent = widget.mMatchConstraintPercentHeight
                    val width: Int = (0.5f + horizPercent * constraintWidgetContainer.getWidth()).toInt()
                    val height: Int = (0.5f + vertPercent * constraintWidgetContainer.getHeight()).toInt()
                    measure(widget, DimensionBehaviour.FIXED, width, DimensionBehaviour.FIXED, height)
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                }
            }
        }
        return false
    }

    /**
     * @TODO: add description
     */
    fun measureWidgets() {
        for (widget in mWidgetcontainer.mChildren) {
            if (widget.measured) {
                continue
            }
            val horiz: DimensionBehaviour = widget.mListDimensionBehaviors[ConstraintWidget.Companion.HORIZONTAL]
            val vert: DimensionBehaviour = widget.mListDimensionBehaviors[ConstraintWidget.Companion.VERTICAL]
            val horizMatchConstraintsType = widget.mMatchConstraintDefaultWidth
            val vertMatchConstraintsType = widget.mMatchConstraintDefaultHeight
            val horizWrap = (horiz == DimensionBehaviour.WRAP_CONTENT
                    || (horiz == DimensionBehaviour.MATCH_CONSTRAINT
                    && horizMatchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP))
            val vertWrap = (vert == DimensionBehaviour.WRAP_CONTENT
                    || (vert == DimensionBehaviour.MATCH_CONSTRAINT
                    && vertMatchConstraintsType == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP))
            val horizResolved: Boolean = widget.mHorizontalRun!!.mDimension.resolved
            val vertResolved: Boolean = widget.mVerticalRun!!.mDimension.resolved
            if (horizResolved && vertResolved) {
                measure(
                    widget,
                    DimensionBehaviour.FIXED,
                    widget.mHorizontalRun!!.mDimension.value,
                    DimensionBehaviour.FIXED,
                    widget.mVerticalRun!!.mDimension.value
                )
                widget.measured = true
            } else if (horizResolved && vertWrap) {
                measure(
                    widget,
                    DimensionBehaviour.FIXED,
                    widget.mHorizontalRun!!.mDimension.value,
                    DimensionBehaviour.WRAP_CONTENT,
                    widget.mVerticalRun!!.mDimension.value
                )
                if (vert == DimensionBehaviour.MATCH_CONSTRAINT) {
                    widget.mVerticalRun!!.mDimension.wrapValue = widget.height
                } else {
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                }
            } else if (vertResolved && horizWrap) {
                measure(
                    widget,
                    DimensionBehaviour.WRAP_CONTENT,
                    widget.mHorizontalRun!!.mDimension.value,
                    DimensionBehaviour.FIXED,
                    widget.mVerticalRun!!.mDimension.value
                )
                if (horiz == DimensionBehaviour.MATCH_CONSTRAINT) {
                    widget.mHorizontalRun!!.mDimension.wrapValue = widget.width
                } else {
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.measured = true
                }
            }
            if (widget.measured && widget.mVerticalRun!!.mBaselineDimension != null) {
                widget.mVerticalRun!!.mBaselineDimension.resolve(widget.baselineDistance)
            }
        }
    }

    /**
     * Invalidate the graph of constraints
     */
    fun invalidateGraph() {
        mNeedBuildGraph = true
    }

    /**
     * Mark the widgets as needing to be remeasured
     */
    fun invalidateMeasures() {
        mNeedRedoMeasures = true
    }

    var mGroups: java.util.ArrayList<RunGroup> = java.util.ArrayList<RunGroup>()

    init {
        mContainer = mWidgetcontainer
    }

    /**
     * @TODO: add description
     */
    fun buildGraph() {
        // First, let's identify the overall dependency graph
        buildGraph(mRuns)
        if (USE_GROUPS) {
            mGroups.clear()
            // Then get the horizontal and vertical groups
            RunGroup.Companion.index = 0
            findGroup(mWidgetcontainer.mHorizontalRun, ConstraintWidget.Companion.HORIZONTAL, mGroups)
            findGroup(mWidgetcontainer.mVerticalRun, ConstraintWidget.Companion.VERTICAL, mGroups)
        }
        mNeedBuildGraph = false
    }

    /**
     * @TODO: add description
     */
    fun buildGraph(runs: java.util.ArrayList<WidgetRun?>) {
        runs.clear()
        mContainer.mHorizontalRun.clear()
        mContainer.mVerticalRun.clear()
        runs.add(mContainer.mHorizontalRun)
        runs.add(mContainer.mVerticalRun)
        var chainRuns: java.util.HashSet<ChainRun?>? = null
        for (widget in mContainer.mChildren) {
            if (widget is Guideline) {
                runs.add(GuidelineReference(widget))
                continue
            }
            if (widget.isInHorizontalChain) {
                if (widget.horizontalChainRun == null) {
                    // build the horizontal chain
                    widget.horizontalChainRun = ChainRun(widget, ConstraintWidget.Companion.HORIZONTAL)
                }
                if (chainRuns == null) {
                    chainRuns = java.util.HashSet<ChainRun>()
                }
                chainRuns.add(widget.horizontalChainRun)
            } else {
                runs.add(widget.mHorizontalRun)
            }
            if (widget.isInVerticalChain) {
                if (widget.verticalChainRun == null) {
                    // build the vertical chain
                    widget.verticalChainRun = ChainRun(widget, ConstraintWidget.Companion.VERTICAL)
                }
                if (chainRuns == null) {
                    chainRuns = java.util.HashSet<ChainRun>()
                }
                chainRuns.add(widget.verticalChainRun)
            } else {
                runs.add(widget.mVerticalRun)
            }
            if (widget is HelperWidget) {
                runs.add(HelperReferences(widget))
            }
        }
        if (chainRuns != null) {
            runs.addAll(chainRuns)
        }
        for (run in runs) {
            run.clear()
        }
        for (run in runs) {
            if (run.mWidget === mContainer) {
                continue
            }
            run.apply()
        }
        if (DEBUG) {
            displayGraph()
        }
    }

    private fun displayGraph() {
        var content = "digraph {\n"
        for (run in mRuns) {
            content = generateDisplayGraph(run, content)
        }
        content += "\n}\n"
        println("content:<<\n$content\n>>")
    }

    private fun applyGroup(
        node: DependencyNode?,
        orientation: Int,
        direction: Int,
        end: DependencyNode?,
        groups: java.util.ArrayList<RunGroup>,
        group: RunGroup?
    ) {
        var group = group
        val run = node!!.mRun
        if (run!!.mRunGroup != null || run === mWidgetcontainer.mHorizontalRun || run === mWidgetcontainer.mVerticalRun) {
            return
        }
        if (group == null) {
            group = RunGroup(run, direction)
            groups.add(group)
        }
        run!!.mRunGroup = group
        group.add(run)
        for (dependent in run.start.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(
                    dependent,
                    orientation, RunGroup.Companion.START, end, groups, group
                )
            }
        }
        for (dependent in run.end.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(
                    dependent,
                    orientation, RunGroup.Companion.END, end, groups, group
                )
            }
        }
        if (orientation == ConstraintWidget.Companion.VERTICAL && run is VerticalWidgetRun) {
            for (dependent in run.baseline.mDependencies) {
                if (dependent is DependencyNode) {
                    applyGroup(
                        dependent,
                        orientation, RunGroup.Companion.BASELINE, end, groups, group
                    )
                }
            }
        }
        for (target in run.start.mTargets) {
            if (target === end) {
                group.dual = true
            }
            applyGroup(target, orientation, RunGroup.Companion.START, end, groups, group)
        }
        for (target in run.end.mTargets) {
            if (target === end) {
                group.dual = true
            }
            applyGroup(target, orientation, RunGroup.Companion.END, end, groups, group)
        }
        if (orientation == ConstraintWidget.Companion.VERTICAL && run is VerticalWidgetRun) {
            for (target in run.baseline.mTargets) {
                applyGroup(target, orientation, RunGroup.Companion.BASELINE, end, groups, group)
            }
        }
    }

    private fun findGroup(run: WidgetRun, orientation: Int, groups: java.util.ArrayList<RunGroup>) {
        for (dependent in run.start.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(dependent, orientation, RunGroup.Companion.START, run.end, groups, null)
            } else if (dependent is WidgetRun) {
                applyGroup(dependent.start, orientation, RunGroup.Companion.START, run.end, groups, null)
            }
        }
        for (dependent in run.end.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(dependent, orientation, RunGroup.Companion.END, run.start, groups, null)
            } else if (dependent is WidgetRun) {
                applyGroup(dependent.end, orientation, RunGroup.Companion.END, run.start, groups, null)
            }
        }
        if (orientation == ConstraintWidget.Companion.VERTICAL) {
            for (dependent in (run as VerticalWidgetRun).baseline.mDependencies) {
                if (dependent is DependencyNode) {
                    applyGroup(dependent, orientation, RunGroup.Companion.BASELINE, null, groups, null)
                }
            }
        }
    }

    private fun generateDisplayNode(
        node: DependencyNode?,
        centeredConnection: Boolean,
        content: String
    ): String {
        var content = content
        val contentBuilder: java.lang.StringBuilder = java.lang.StringBuilder(content)
        for (target in node!!.mTargets) {
            var constraint = """
                
                ${node.name()}
                """.trimIndent()
            constraint += " -> " + target!!.name()
            if (node.mMargin > 0 || centeredConnection || node.mRun is HelperReferences) {
                constraint += "["
                if (node.mMargin > 0) {
                    constraint += "label=\"" + node.mMargin + "\""
                    if (centeredConnection) {
                        constraint += ","
                    }
                }
                if (centeredConnection) {
                    constraint += " style=dashed "
                }
                if (node.mRun is HelperReferences) {
                    constraint += " style=bold,color=gray "
                }
                constraint += "]"
            }
            constraint += "\n"
            contentBuilder.append(constraint)
        }
        content = contentBuilder.toString()
        //        for (DependencyNode dependency : node.dependencies) {
//            content = generateDisplayNode(dependency, content);
//        }
        return content
    }

    private fun nodeDefinition(run: WidgetRun): String {
        val orientation: Int =
            if (run is VerticalWidgetRun) ConstraintWidget.Companion.VERTICAL else ConstraintWidget.Companion.HORIZONTAL
        val name: String = run.mWidget.getDebugName()
        val definition: java.lang.StringBuilder = java.lang.StringBuilder(name)
        val behaviour: DimensionBehaviour =
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) run.mWidget.getHorizontalDimensionBehaviour() else run.mWidget.getVerticalDimensionBehaviour()
        val runGroup = run.mRunGroup
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            definition.append("_HORIZONTAL")
        } else {
            definition.append("_VERTICAL")
        }
        definition.append(" [shape=none, label=<")
        definition.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"2\">")
        definition.append("  <TR>")
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            definition.append("    <TD ")
            if (run.start.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"LEFT\" BORDER=\"1\">L</TD>")
        } else {
            definition.append("    <TD ")
            if (run.start.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"TOP\" BORDER=\"1\">T</TD>")
        }
        definition.append("    <TD BORDER=\"1\" ")
        if (run.mDimension.resolved && !run.mWidget.measured) {
            definition.append(" BGCOLOR=\"green\" ")
        } else if (run.mDimension.resolved) {
            definition.append(" BGCOLOR=\"lightgray\" ")
        } else if (run.mWidget.measured) {
            definition.append(" BGCOLOR=\"yellow\" ")
        }
        if (behaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
            definition.append("style=\"dashed\"")
        }
        definition.append(">")
        definition.append(name)
        if (runGroup != null) {
            definition.append(" [")
            definition.append(runGroup.mGroupIndex + 1)
            definition.append("/")
            definition.append(RunGroup.Companion.index)
            definition.append("]")
        }
        definition.append(" </TD>")
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            definition.append("    <TD ")
            if (run.end.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"RIGHT\" BORDER=\"1\">R</TD>")
        } else {
            definition.append("    <TD ")
            if ((run as VerticalWidgetRun).baseline.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"BASELINE\" BORDER=\"1\">b</TD>")
            definition.append("    <TD ")
            if (run.end.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"BOTTOM\" BORDER=\"1\">B</TD>")
        }
        definition.append("  </TR></TABLE>")
        definition.append(">];\n")
        return definition.toString()
    }

    private fun generateChainDisplayGraph(chain: ChainRun, content: String): String {
        val orientation: Int = chain.orientation
        val subgroup: java.lang.StringBuilder = java.lang.StringBuilder("subgraph ")
        subgroup.append("cluster_")
        subgroup.append(chain.mWidget.getDebugName())
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            subgroup.append("_h")
        } else {
            subgroup.append("_v")
        }
        subgroup.append(" {\n")
        var definitions = ""
        for (run in chain.mWidgets) {
            subgroup.append(run.mWidget.getDebugName())
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                subgroup.append("_HORIZONTAL")
            } else {
                subgroup.append("_VERTICAL")
            }
            subgroup.append(";\n")
            definitions = generateDisplayGraph(run, definitions)
        }
        subgroup.append("}\n")
        return content + definitions + subgroup
    }

    private fun isCenteredConnection(start: DependencyNode?, end: DependencyNode?): Boolean {
        var startTargets = 0
        var endTargets = 0
        for (s in start!!.mTargets) {
            if (s !== end) {
                startTargets++
            }
        }
        for (e in end!!.mTargets) {
            if (e !== start) {
                endTargets++
            }
        }
        return startTargets > 0 && endTargets > 0
    }

    private fun generateDisplayGraph(root: WidgetRun, content: String): String {
        var content = content
        val start = root.start
        val end = root.end
        val sb: java.lang.StringBuilder = java.lang.StringBuilder(content)
        if (root !is HelperReferences && start!!.mDependencies.isEmpty()
            && end!!.mDependencies.isEmpty() && start.mTargets.isEmpty()
            && end.mTargets.isEmpty()
        ) {
            return content
        }
        sb.append(nodeDefinition(root))
        val centeredConnection = isCenteredConnection(start, end)
        content = generateDisplayNode(start, centeredConnection, content)
        content = generateDisplayNode(end, centeredConnection, content)
        if (root is VerticalWidgetRun) {
            val baseline = root.baseline
            content = generateDisplayNode(baseline, centeredConnection, content)
        }
        if ((root is HorizontalWidgetRun || root is ChainRun) && (root as ChainRun).orientation == ConstraintWidget.Companion.HORIZONTAL) {
            val behaviour: DimensionBehaviour = root.mWidget.getHorizontalDimensionBehaviour()
            if (behaviour == DimensionBehaviour.FIXED
                || behaviour == DimensionBehaviour.WRAP_CONTENT
            ) {
                if (!start!!.mTargets.isEmpty() && end!!.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(end.name())
                    sb.append(" -> ")
                    sb.append(start.name())
                    sb.append("\n")
                } else if (start.mTargets.isEmpty() && !end!!.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(start.name())
                    sb.append(" -> ")
                    sb.append(end.name())
                    sb.append("\n")
                }
            } else {
                if (behaviour == DimensionBehaviour.MATCH_CONSTRAINT && root.mWidget.getDimensionRatio() > 0) {
                    sb.append("\n")
                    sb.append(root.mWidget.getDebugName())
                    sb.append("_HORIZONTAL -> ")
                    sb.append(root.mWidget.getDebugName())
                    sb.append("_VERTICAL;\n")
                }
            }
        } else if ((root is VerticalWidgetRun || root is ChainRun) && (root as ChainRun).orientation == ConstraintWidget.Companion.VERTICAL) {
            val behaviour: DimensionBehaviour = root.mWidget.getVerticalDimensionBehaviour()
            if (behaviour == DimensionBehaviour.FIXED
                || behaviour == DimensionBehaviour.WRAP_CONTENT
            ) {
                if (!start!!.mTargets.isEmpty() && end!!.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(end.name())
                    sb.append(" -> ")
                    sb.append(start.name())
                    sb.append("\n")
                } else if (start.mTargets.isEmpty() && !end!!.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(start.name())
                    sb.append(" -> ")
                    sb.append(end.name())
                    sb.append("\n")
                }
            } else {
                if (behaviour == DimensionBehaviour.MATCH_CONSTRAINT && root.mWidget.getDimensionRatio() > 0) {
                    sb.append("\n")
                    sb.append(root.mWidget.getDebugName())
                    sb.append("_VERTICAL -> ")
                    sb.append(root.mWidget.getDebugName())
                    sb.append("_HORIZONTAL;\n")
                }
            }
        }
        return if (root is ChainRun) {
            generateChainDisplayGraph(root, content)
        } else sb.toString()
    }

    companion object {
        private const val USE_GROUPS = true
        private const val DEBUG = false
    }
}
