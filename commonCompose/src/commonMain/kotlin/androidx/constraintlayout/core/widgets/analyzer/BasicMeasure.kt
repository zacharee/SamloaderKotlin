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

import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.core.widgets.*
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer
import kotlinx.datetime.Clock
import kotlin.math.max
import kotlin.math.min

/**
 * Implements basic measure for linear resolution
 */
class BasicMeasure(private val mConstraintWidgetContainer: ConstraintWidgetContainer) {
    private val mVariableDimensionsWidgets: ArrayList<ConstraintWidget> = ArrayList()
    private val mMeasure = Measure()

    /**
     * @TODO: add description
     */
    fun updateHierarchy(layout: ConstraintWidgetContainer) {
        mVariableDimensionsWidgets.clear()
        val childCount: Int = layout.mChildren.size
        for (i in 0 until childCount) {
            val widget: ConstraintWidget = layout.mChildren.get(i)
            if (widget.horizontalDimensionBehaviour
                == DimensionBehaviour.MATCH_CONSTRAINT
                || widget.verticalDimensionBehaviour
                == DimensionBehaviour.MATCH_CONSTRAINT
            ) {
                mVariableDimensionsWidgets.add(widget)
            }
        }
        layout.invalidateGraph()
    }

    private fun measureChildren(layout: ConstraintWidgetContainer) {
        val childCount: Int = layout.mChildren.size
        val optimize = layout.optimizeFor(Optimizer.OPTIMIZATION_GRAPH)
        val measurer = layout.measurer!!
        for (i in 0 until childCount) {
            val child: ConstraintWidget = layout.mChildren.get(i)
            if (child is Guideline) {
                continue
            }
            if (child is Barrier) {
                continue
            }
            if (child.isInVirtualLayout) {
                continue
            }
            if (optimize && child.mHorizontalRun != null && child.mVerticalRun != null && child.mHorizontalRun!!.mDimension.resolved
                && child.mVerticalRun!!.mDimension.resolved
            ) {
                continue
            }
            val widthBehavior: DimensionBehaviour? = child.getDimensionBehaviour(ConstraintWidget.Companion.HORIZONTAL)
            val heightBehavior: DimensionBehaviour? = child.getDimensionBehaviour(ConstraintWidget.Companion.VERTICAL)
            var skip =
                widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT && child.mMatchConstraintDefaultWidth != ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP && heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT && child.mMatchConstraintDefaultHeight != ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
            if (!skip && layout.optimizeFor(Optimizer.OPTIMIZATION_DIRECT)
                && child !is VirtualLayout
            ) {
                if (widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT && child.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD && heightBehavior != DimensionBehaviour.MATCH_CONSTRAINT && !child.isInHorizontalChain) {
                    skip = true
                }
                if (heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT && child.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD && widthBehavior != DimensionBehaviour.MATCH_CONSTRAINT && !child.isInHorizontalChain) {
                    skip = true
                }

                // Don't measure yet -- let the direct solver have a shot at it.
                if ((widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT
                            || heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT)
                    && child.dimensionRatio > 0
                ) {
                    skip = true
                }
            }
            if (skip) {
                // we don't need to measure here as the dimension of the widget
                // will be completely computed by the solver.
                continue
            }
            measure(measurer, child, Measure.SELF_DIMENSIONS)
            if (layout.mMetrics != null) {
                layout.mMetrics!!.measuredWidgets++
            }
        }
        measurer.didMeasures()
    }

    private fun solveLinearSystem(
        layout: ConstraintWidgetContainer,
        reason: String,
        pass: Int,
        w: Int,
        h: Int
    ) {
        val startLayout = if (LinearSystem.Companion.MEASURE) {
            Clock.System.now().run { epochSeconds * 1_000 + nanosecondsOfSecond }
        } else {
            0L
        }
        val minWidth: Int = layout.minWidth
        val minHeight: Int = layout.minHeight
        layout.minWidth = (0)
        layout.minHeight = (0)
        layout.width = (w)
        layout.height = (h)
        layout.minWidth = (minWidth)
        layout.minHeight = (minHeight)
        if (DEBUG) {
            println("### Solve <$reason> ###")
        }
        mConstraintWidgetContainer.setPass(pass)
        mConstraintWidgetContainer.layout()
        if (LinearSystem.Companion.MEASURE && layout.mMetrics != null) {
            val endLayout: Long = Clock.System.now().run { epochSeconds * 1_000 + nanosecondsOfSecond }
            layout.mMetrics!!.measuresLayoutDuration += endLayout - startLayout
        }
    }

    /**
     * Called by ConstraintLayout onMeasure()
     */
    fun solverMeasure(
        layout: ConstraintWidgetContainer,
        optimizationLevel: Int,
        paddingX: Int, paddingY: Int,
        widthMode: Int, widthSize: Int,
        heightMode: Int, heightSize: Int,
        lastMeasureWidth: Int,
        lastMeasureHeight: Int
    ): Long {
        var widthSize = widthSize
        var heightSize = heightSize
        val measurer: Measurer = layout.measurer!!
        var layoutTime: Long = 0
        val childCount: Int = layout.mChildren.size
        val startingWidth: Int = layout.width
        val startingHeight: Int = layout.height
        val optimizeWrap = Optimizer.enabled(optimizationLevel, Optimizer.OPTIMIZATION_GRAPH_WRAP)
        var optimize = (optimizeWrap
                || Optimizer.enabled(optimizationLevel, Optimizer.OPTIMIZATION_GRAPH))
        if (optimize) {
            for (i in 0 until childCount) {
                val child: ConstraintWidget = layout.mChildren.get(i)
                val matchWidth = (child.horizontalDimensionBehaviour
                        == DimensionBehaviour.MATCH_CONSTRAINT)
                val matchHeight = (child.verticalDimensionBehaviour
                        == DimensionBehaviour.MATCH_CONSTRAINT)
                val ratio = matchWidth && matchHeight && child.dimensionRatio > 0
                if (child.isInHorizontalChain && ratio) {
                    optimize = false
                    break
                }
                if (child.isInVerticalChain && ratio) {
                    optimize = false
                    break
                }
                if (child is VirtualLayout) {
                    optimize = false
                    break
                }
                if (child.isInHorizontalChain
                    || child.isInVerticalChain
                ) {
                    optimize = false
                    break
                }
            }
        }
        if (optimize && LinearSystem.Companion.sMetrics != null) {
            LinearSystem.Companion.sMetrics!!.measures++
        }
        var allSolved = false
        optimize = optimize and (widthMode == EXACTLY && heightMode == EXACTLY || optimizeWrap)
        var computations = 0
        if (optimize) {
            // For non-optimizer this doesn't seem to be a problem.
            // For both cases, having the width address max size early seems to work
            //  (which makes sense).
            // Putting it specific to optimizer to reduce unnecessary risk.
            widthSize = min(layout.maxWidth, widthSize)
            heightSize = min(layout.maxHeight, heightSize)
            if (widthMode == EXACTLY && layout.width != widthSize) {
                layout.width = (widthSize)
                layout.invalidateGraph()
            }
            if (heightMode == EXACTLY && layout.height != heightSize) {
                layout.height = (heightSize)
                layout.invalidateGraph()
            }
            if (widthMode == EXACTLY && heightMode == EXACTLY) {
                allSolved = layout.directMeasure(optimizeWrap)
                computations = 2
            } else {
                allSolved = layout.directMeasureSetup(optimizeWrap)
                if (widthMode == EXACTLY) {
                    allSolved = allSolved and layout.directMeasureWithOrientation(
                        optimizeWrap,
                        ConstraintWidget.Companion.HORIZONTAL
                    )
                    computations++
                }
                if (heightMode == EXACTLY) {
                    allSolved = allSolved and layout.directMeasureWithOrientation(
                        optimizeWrap,
                        ConstraintWidget.Companion.VERTICAL
                    )
                    computations++
                }
            }
            if (allSolved) {
                layout.updateFromRuns(widthMode == EXACTLY, heightMode == EXACTLY)
            }
        } else {
            if (false) {
                layout.mHorizontalRun?.clear()
                layout.mVerticalRun?.clear()
                for (child in layout.children) {
                    child.mHorizontalRun!!.clear()
                    child.mVerticalRun!!.clear()
                }
            }
        }
        if (!allSolved || computations != 2) {
            val optimizations = layout.optimizationLevel
            if (childCount > 0) {
                measureChildren(layout)
            }
            if (LinearSystem.Companion.MEASURE) {
                layoutTime = Clock.System.now().run { epochSeconds * 1_000 + nanosecondsOfSecond }
            }
            updateHierarchy(layout)

            // let's update the size dependent widgets if any...
            val sizeDependentWidgetsCount: Int = mVariableDimensionsWidgets.size

            // let's solve the linear system.
            if (childCount > 0) {
                solveLinearSystem(layout, "First pass", 0, startingWidth, startingHeight)
            }
            if (DEBUG) {
                println("size dependent widgets: $sizeDependentWidgetsCount")
            }
            if (sizeDependentWidgetsCount > 0) {
                var needSolverPass = false
                val containerWrapWidth = (layout.horizontalDimensionBehaviour
                        == DimensionBehaviour.WRAP_CONTENT)
                val containerWrapHeight = (layout.verticalDimensionBehaviour
                        == DimensionBehaviour.WRAP_CONTENT)
                var minWidth: Int = max(
                    layout.width,
                    mConstraintWidgetContainer.minWidth
                )
                var minHeight: Int = max(
                    layout.height,
                    mConstraintWidgetContainer.minHeight
                )

                ////////////////////////////////////////////////////////////////////////////////////
                // Let's first apply sizes for VirtualLayouts if any
                ////////////////////////////////////////////////////////////////////////////////////
                for (i in 0 until sizeDependentWidgetsCount) {
                    val widget: ConstraintWidget = mVariableDimensionsWidgets.get(i)
                    if (widget !is VirtualLayout) {
                        continue
                    }
                    val preWidth = widget.width
                    val preHeight = widget.height
                    needSolverPass = needSolverPass or measure(measurer, widget, Measure.TRY_GIVEN_DIMENSIONS)
                    if (layout.mMetrics != null) {
                        layout.mMetrics!!.measuredMatchWidgets++
                    }
                    val measuredWidth = widget.width
                    val measuredHeight = widget.height
                    if (measuredWidth != preWidth) {
                        widget.width = measuredWidth
                        if (containerWrapWidth && widget.right > minWidth) {
                            val w: Int = (widget.right
                                    + widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.margin)
                            minWidth = max(minWidth, w)
                        }
                        needSolverPass = true
                    }
                    if (measuredHeight != preHeight) {
                        widget.height = measuredHeight
                        if (containerWrapHeight && widget.bottom > minHeight) {
                            val h: Int = (widget.bottom
                                    + widget.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.margin)
                            minHeight = max(minHeight, h)
                        }
                        needSolverPass = true
                    }
                    needSolverPass = needSolverPass or widget.needSolverPass()
                }
                ////////////////////////////////////////////////////////////////////////////////////
                val maxIterations = 2
                for (j in 0 until maxIterations) {
                    for (i in 0 until sizeDependentWidgetsCount) {
                        val widget: ConstraintWidget = mVariableDimensionsWidgets.get(i)
                        if (widget is Helper && widget !is VirtualLayout
                            || widget is Guideline
                        ) {
                            continue
                        }
                        if (widget.visibility == ConstraintWidget.Companion.GONE) {
                            continue
                        }
                        if (optimize && widget.mHorizontalRun!!.mDimension.resolved
                            && widget.mVerticalRun!!.mDimension.resolved
                        ) {
                            continue
                        }
                        if (widget is VirtualLayout) {
                            continue
                        }
                        val preWidth = widget.width
                        val preHeight = widget.height
                        val preBaselineDistance = widget.baselineDistance
                        var measureStrategy = Measure.TRY_GIVEN_DIMENSIONS
                        if (j == maxIterations - 1) {
                            measureStrategy = Measure.USE_GIVEN_DIMENSIONS
                        }
                        var hasMeasure = measure(measurer, widget, measureStrategy)
                        if (DO_NOT_USE && !widget.hasDependencies()) {
                            hasMeasure = false
                        }
                        needSolverPass = needSolverPass or hasMeasure
                        if (DEBUG && hasMeasure) {
                            println(
                                "{#} Needs Solver pass as measure true for "
                                        + widget.debugName
                            )
                        }
                        if (layout.mMetrics != null) {
                            layout.mMetrics!!.measuredMatchWidgets++
                        }
                        val measuredWidth = widget.width
                        val measuredHeight = widget.height
                        if (measuredWidth != preWidth) {
                            widget.width = measuredWidth
                            if (containerWrapWidth && widget.right > minWidth) {
                                val w: Int = (widget.right
                                        + widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.margin)
                                minWidth = max(minWidth, w)
                            }
                            if (DEBUG) {
                                println(
                                    "{#} Needs Solver pass as Width for "
                                            + widget.debugName + " changed: "
                                            + measuredWidth + " != " + preWidth
                                )
                            }
                            needSolverPass = true
                        }
                        if (measuredHeight != preHeight) {
                            widget.height = measuredHeight
                            if (containerWrapHeight && widget.bottom > minHeight) {
                                val h: Int = (widget.bottom
                                        + widget.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.margin)
                                minHeight = max(minHeight, h)
                            }
                            if (DEBUG) {
                                println(
                                    "{#} Needs Solver pass as Height for "
                                            + widget.debugName + " changed: "
                                            + measuredHeight + " != " + preHeight
                                )
                            }
                            needSolverPass = true
                        }
                        if (widget.hasBaseline()
                            && preBaselineDistance != widget.baselineDistance
                        ) {
                            if (DEBUG) {
                                println(
                                    "{#} Needs Solver pass as Baseline for "
                                            + widget.debugName + " changed: "
                                            + widget.baselineDistance + " != "
                                            + preBaselineDistance
                                )
                            }
                            needSolverPass = true
                        }
                    }
                    needSolverPass = if (needSolverPass) {
                        solveLinearSystem(
                            layout, "intermediate pass",
                            1 + j, startingWidth, startingHeight
                        )
                        false
                    } else {
                        break
                    }
                }
            }
            layout.optimizationLevel = optimizations
        }
        if (LinearSystem.Companion.MEASURE) {
            layoutTime = Clock.System.now().run { epochSeconds * 1_000 + nanosecondsOfSecond } - layoutTime
        }
        return layoutTime
    }

    /**
     * Convenience function to fill in the measure spec
     *
     * @param measurer        the measurer callback
     * @param widget          the widget to measure
     * @param measureStrategy how to use the current ConstraintWidget dimensions during the measure
     * @return true if needs another solver pass
     */
    private fun measure(measurer: Measurer, widget: ConstraintWidget, measureStrategy: Int): Boolean {
        mMeasure.horizontalBehavior = widget.horizontalDimensionBehaviour
        mMeasure.verticalBehavior = widget.verticalDimensionBehaviour
        mMeasure.horizontalDimension = widget.width
        mMeasure.verticalDimension = widget.height
        mMeasure.measuredNeedsSolverPass = false
        mMeasure.measureStrategy = measureStrategy
        val horizontalMatchConstraints = (mMeasure.horizontalBehavior
                == DimensionBehaviour.MATCH_CONSTRAINT)
        val verticalMatchConstraints = (mMeasure.verticalBehavior
                == DimensionBehaviour.MATCH_CONSTRAINT)
        val horizontalUseRatio = horizontalMatchConstraints && widget.dimensionRatio > 0
        val verticalUseRatio = verticalMatchConstraints && widget.dimensionRatio > 0
        if (horizontalUseRatio) {
            if (widget.mResolvedMatchConstraintDefault[ConstraintWidget.Companion.HORIZONTAL]
                == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO_RESOLVED
            ) {
                mMeasure.horizontalBehavior = DimensionBehaviour.FIXED
            }
        }
        if (verticalUseRatio) {
            if (widget.mResolvedMatchConstraintDefault[ConstraintWidget.Companion.VERTICAL]
                == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO_RESOLVED
            ) {
                mMeasure.verticalBehavior = DimensionBehaviour.FIXED
            }
        }
        measurer.measure(widget, mMeasure)
        widget.width = mMeasure.measuredWidth
        widget.height = mMeasure.measuredHeight
        widget.hasBaseline = mMeasure.measuredHasBaseline
        widget.baselineDistance = mMeasure.measuredBaseline
        mMeasure.measureStrategy = Measure.SELF_DIMENSIONS
        return mMeasure.measuredNeedsSolverPass
    }

    interface Measurer {
        /**
         * @TODO: add description
         */
        fun measure(widget: ConstraintWidget?, measure: Measure)

        /**
         * @TODO: add description
         */
        fun didMeasures()
    }

    class Measure {
        var horizontalBehavior: DimensionBehaviour? = null
        var verticalBehavior: DimensionBehaviour? = null
        var horizontalDimension = 0
        var verticalDimension = 0
        var measuredWidth = 0
        var measuredHeight = 0
        var measuredBaseline = 0
        var measuredHasBaseline = false
        var measuredNeedsSolverPass = false
        var measureStrategy = 0

        companion object {
            var SELF_DIMENSIONS = 0
            var TRY_GIVEN_DIMENSIONS = 1
            var USE_GIVEN_DIMENSIONS = 2
        }
    }

    companion object {
        private const val DEBUG = false
        private const val DO_NOT_USE = false
        private const val MODE_SHIFT = 30
        const val UNSPECIFIED = 0
        const val EXACTLY = 1 shl MODE_SHIFT
        const val AT_MOST = 2 shl MODE_SHIFT
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
        const val FIXED = -3
    }
}
