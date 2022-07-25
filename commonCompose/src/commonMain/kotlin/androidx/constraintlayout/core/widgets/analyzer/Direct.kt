/*
 * Copyright (C) 2020 The Android Open Source Project
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

/**
 * Direct resolution engine
 *
 * This walks through the graph of dependencies and infer final position. This allows
 * us to skip the linear solver in many situations, as well as skipping intermediate measure passes.
 *
 * Widgets are solved independently in horizontal and vertical. Any widgets not fully resolved
 * will be computed later on by the linear solver.
 */
object Direct {
    private val DEBUG: Boolean = LinearSystem.Companion.FULL_DEBUG
    private const val APPLY_MATCH_PARENT = false
    private val sMeasure = BasicMeasure.Measure()
    private const val EARLY_TERMINATION = true // feature flag -- remove after release.
    private var sHcount = 0
    private var sVcount = 0

    /**
     * Walk the dependency graph and solves it.
     *
     * @param layout   the container we want to optimize
     * @param measurer the measurer used to measure the widget
     */
    fun solvingPass(
        layout: ConstraintWidgetContainer,
        measurer: BasicMeasure.Measurer?
    ) {
        val horizontal: DimensionBehaviour = layout.getHorizontalDimensionBehaviour()
        val vertical: DimensionBehaviour = layout.getVerticalDimensionBehaviour()
        sHcount = 0
        sVcount = 0
        var time: Long = 0
        if (DEBUG) {
            time = java.lang.System.nanoTime()
            println(
                "#### SOLVING PASS (horiz " + horizontal
                        + ", vert " + vertical + ") ####"
            )
        }
        layout.resetFinalResolution()
        val children: java.util.ArrayList<ConstraintWidget> = layout.getChildren()
        val count: Int = children.size
        if (DEBUG) {
            println("#### SOLVING PASS on $count widgeets ####")
        }
        for (i in 0 until count) {
            val child: ConstraintWidget = children.get(i)
            child.resetFinalResolution()
        }
        val isRtl = layout.isRtl

        // First, let's solve the horizontal dependencies, as it's a lot more common to have
        // a container with a fixed horizontal dimension (e.g. match_parent) than the opposite.

        // If we know our size, we can fully set the entire dimension, but if not we can
        // still solve what we can starting from the left.
        if (horizontal == DimensionBehaviour.FIXED) {
            layout.setFinalHorizontal(0, layout.getWidth())
        } else {
            layout.setFinalLeft(0)
        }
        if (DEBUG) {
            println("\n### Let's solve horizontal dependencies ###\n")
        }

        // Then let's first try to solve horizontal guidelines,
        // as they only depends on the container
        var hasGuideline = false
        var hasBarrier = false
        for (i in 0 until count) {
            val child: ConstraintWidget = children.get(i)
            if (child is Guideline) {
                val guideline = child
                if (guideline.orientation == Guideline.Companion.VERTICAL) {
                    if (guideline.relativeBegin != -1) {
                        guideline.setFinalValue(guideline.relativeBegin)
                    } else if (guideline.relativeEnd != -1
                        && layout.isResolvedHorizontally()
                    ) {
                        guideline.setFinalValue(layout.getWidth() - guideline.relativeEnd)
                    } else if (layout.isResolvedHorizontally()) {
                        val position: Int = (0.5f + guideline.relativePercent * layout.getWidth()).toInt()
                        guideline.setFinalValue(position)
                    }
                    hasGuideline = true
                }
            } else if (child is Barrier) {
                if (child.orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    hasBarrier = true
                }
            }
        }
        if (hasGuideline) {
            if (DEBUG) {
                println("\n#### VERTICAL GUIDELINES CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children.get(i)
                if (child is Guideline) {
                    val guideline = child
                    if (guideline.orientation == Guideline.Companion.VERTICAL) {
                        horizontalSolvingPass(0, guideline, measurer, isRtl)
                    }
                }
            }
            if (DEBUG) {
                println("### Done solving guidelines.")
            }
        }
        if (DEBUG) {
            println("\n#### HORIZONTAL SOLVING PASS ####")
        }

        // Now let's resolve what we can in the dependencies of the container
        horizontalSolvingPass(0, layout, measurer, isRtl)

        // Finally, let's go through barriers, as they depends on widgets that may have been solved.
        if (hasBarrier) {
            if (DEBUG) {
                println("\n#### HORIZONTAL BARRIER CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children.get(i)
                if (child is Barrier) {
                    val barrier = child
                    if (barrier.orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        solveBarrier(0, barrier, measurer, ConstraintWidget.Companion.HORIZONTAL, isRtl)
                    }
                }
            }
            if (DEBUG) {
                println("#### DONE HORIZONTAL BARRIER CHECKS ####")
            }
        }
        if (DEBUG) {
            println("\n### Let's solve vertical dependencies now ###\n")
        }

        // Now we are done with the horizontal axis, let's see what we can do vertically
        if (vertical == DimensionBehaviour.FIXED) {
            layout.setFinalVertical(0, layout.getHeight())
        } else {
            layout.setFinalTop(0)
        }

        // Same thing as above -- let's start with guidelines...
        hasGuideline = false
        hasBarrier = false
        for (i in 0 until count) {
            val child: ConstraintWidget = children.get(i)
            if (child is Guideline) {
                val guideline = child
                if (guideline.orientation == Guideline.Companion.HORIZONTAL) {
                    if (guideline.relativeBegin != -1) {
                        guideline.setFinalValue(guideline.relativeBegin)
                    } else if (guideline.relativeEnd != -1 && layout.isResolvedVertically()) {
                        guideline.setFinalValue(layout.getHeight() - guideline.relativeEnd)
                    } else if (layout.isResolvedVertically()) {
                        val position: Int = (0.5f + guideline.relativePercent * layout.getHeight()).toInt()
                        guideline.setFinalValue(position)
                    }
                    hasGuideline = true
                }
            } else if (child is Barrier) {
                if (child.orientation == ConstraintWidget.Companion.VERTICAL) {
                    hasBarrier = true
                }
            }
        }
        if (hasGuideline) {
            if (DEBUG) {
                println("\n#### HORIZONTAL GUIDELINES CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children.get(i)
                if (child is Guideline) {
                    val guideline = child
                    if (guideline.orientation == Guideline.Companion.HORIZONTAL) {
                        verticalSolvingPass(1, guideline, measurer)
                    }
                }
            }
            if (DEBUG) {
                println("\n### Done solving guidelines.")
            }
        }
        if (DEBUG) {
            println("\n#### VERTICAL SOLVING PASS ####")
        }

        // ...then solve the vertical dependencies...
        verticalSolvingPass(0, layout, measurer)

        // ...then deal with any barriers left.
        if (hasBarrier) {
            if (DEBUG) {
                println("#### VERTICAL BARRIER CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children.get(i)
                if (child is Barrier) {
                    val barrier = child
                    if (barrier.orientation == ConstraintWidget.Companion.VERTICAL) {
                        solveBarrier(0, barrier, measurer, ConstraintWidget.Companion.VERTICAL, isRtl)
                    }
                }
            }
        }
        if (DEBUG) {
            println("\n#### LAST PASS ####")
        }
        // We can do a last pass to see any widget that could still be measured
        for (i in 0 until count) {
            val child: ConstraintWidget = children.get(i)
            if (child.isMeasureRequested && canMeasure(0, child)) {
                ConstraintWidgetContainer.Companion.measure(
                    0, child,
                    measurer, sMeasure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
                )
                if (child is Guideline) {
                    if (child.orientation == Guideline.Companion.HORIZONTAL) {
                        verticalSolvingPass(0, child, measurer)
                    } else {
                        horizontalSolvingPass(0, child, measurer, isRtl)
                    }
                } else {
                    horizontalSolvingPass(0, child, measurer, isRtl)
                    verticalSolvingPass(0, child, measurer)
                }
            }
        }
        if (DEBUG) {
            time = java.lang.System.nanoTime() - time
            println("\n*** THROUGH WITH DIRECT PASS in $time ns ***\n")
            println("hcount: " + sHcount + " vcount: " + sVcount)
        }
    }

    /**
     * Ask the barrier if it's resolved, and if so do a solving pass
     */
    private fun solveBarrier(
        level: Int,
        barrier: Barrier,
        measurer: BasicMeasure.Measurer?,
        orientation: Int,
        isRtl: Boolean
    ) {
        if (barrier.allSolved()) {
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                horizontalSolvingPass(level + 1, barrier, measurer, isRtl)
            } else {
                verticalSolvingPass(level + 1, barrier, measurer)
            }
        }
    }

    /**
     * Small utility function to indent logs depending on the level
     *
     * @return a formatted string for the indentation
     */
    fun ls(level: Int): String {
        val builder: java.lang.StringBuilder = java.lang.StringBuilder()
        for (i in 0 until level) {
            builder.append("  ")
        }
        builder.append("+-($level) ")
        return builder.toString()
    }

    /**
     * Does an horizontal solving pass for the given widget. This will walk through the widget's
     * horizontal dependencies and if they can be resolved directly, do so.
     *
     * @param layout   the widget we want to solve the dependencies
     * @param measurer the measurer object to measure the widgets.
     */
    private fun horizontalSolvingPass(
        level: Int,
        layout: ConstraintWidget?,
        measurer: BasicMeasure.Measurer?,
        isRtl: Boolean
    ) {
        if (EARLY_TERMINATION && layout!!.isHorizontalSolvingPassDone) {
            if (DEBUG) {
                println(
                    ls(level) + "HORIZONTAL SOLVING PASS ON "
                            + layout.debugName + " ALREADY CALLED"
                )
            }
            return
        }
        sHcount++
        if (DEBUG) {
            println(ls(level) + "HORIZONTAL SOLVING PASS ON " + layout.getDebugName())
        }
        if (layout !is ConstraintWidgetContainer && layout!!.isMeasureRequested
            && canMeasure(level + 1, layout)
        ) {
            val measure = BasicMeasure.Measure()
            ConstraintWidgetContainer.Companion.measure(
                level + 1, layout,
                measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
            )
        }
        val left = layout.getAnchor(ConstraintAnchor.Type.LEFT)
        val right = layout.getAnchor(ConstraintAnchor.Type.RIGHT)
        val l: Int = left.getFinalValue()
        val r: Int = right.getFinalValue()
        if (left.getDependents() != null && left!!.hasFinalValue()) {
            for (first in left.dependents) {
                val widget = first.mOwner
                var x1 = 0
                var x2 = 0
                val canMeasure = canMeasure(level + 1, widget)
                if (widget!!.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.Companion.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
                    )
                }
                val bothConnected =
                    first === widget!!.mLeft && widget!!.mRight.mTarget != null && widget!!.mRight.mTarget.hasFinalValue() || first === widget!!.mRight && widget!!.mLeft.mTarget != null && widget!!.mLeft.mTarget.hasFinalValue()
                if (widget.horizontalDimensionBehaviour
                    != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget!!.isMeasureRequested) {
                        // Widget needs to be measured
                        if (DEBUG) {
                            println(
                                ls(level + 1) + "(L) We didn't measure "
                                        + widget.debugName + ", let's bail"
                            )
                        }
                        continue
                    }
                    if (first === widget!!.mLeft && widget!!.mRight.mTarget == null) {
                        x1 = l + widget!!.mLeft.getMargin()
                        x2 = x1 + widget.width
                        widget!!.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (first === widget!!.mRight && widget!!.mLeft.mTarget == null) {
                        x2 = l - widget!!.mRight.getMargin()
                        x1 = x2 - widget.width
                        widget!!.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (bothConnected && !widget!!.isInHorizontalChain) {
                        solveHorizontalCenterConstraints(level + 1, measurer, widget, isRtl)
                    } else if (APPLY_MATCH_PARENT && widget.horizontalDimensionBehaviour
                        == DimensionBehaviour.MATCH_PARENT
                    ) {
                        widget!!.setFinalHorizontal(0, widget.width)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    }
                } else if ((widget.horizontalDimensionBehaviour
                            == DimensionBehaviour.MATCH_CONSTRAINT) && widget!!.mMatchConstraintMaxWidth >= 0 && widget!!.mMatchConstraintMinWidth >= 0 && (widget.visibility == ConstraintWidget.Companion.GONE
                            || ((widget!!.mMatchConstraintDefaultWidth
                            == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget!!.isInHorizontalChain && !widget!!.isInVirtualLayout
                ) {
                    if (bothConnected && !widget!!.isInHorizontalChain) {
                        solveHorizontalMatchConstraint(level + 1, layout, measurer, widget, isRtl)
                    }
                }
            }
        }
        if (layout is Guideline) {
            return
        }
        if (right.getDependents() != null && right!!.hasFinalValue()) {
            for (first in right.dependents) {
                val widget = first.mOwner
                val canMeasure = canMeasure(level + 1, widget)
                if (widget!!.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.Companion.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
                    )
                }
                var x1 = 0
                var x2 = 0
                val bothConnected =
                    first === widget!!.mLeft && widget!!.mRight.mTarget != null && widget!!.mRight.mTarget.hasFinalValue() || first === widget!!.mRight && widget!!.mLeft.mTarget != null && widget!!.mLeft.mTarget.hasFinalValue()
                if (widget.horizontalDimensionBehaviour
                    != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget!!.isMeasureRequested) {
                        // Widget needs to be measured
                        if (DEBUG) {
                            println(
                                ls(level + 1) + "(R) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first === widget!!.mLeft && widget!!.mRight.mTarget == null) {
                        x1 = r + widget!!.mLeft.getMargin()
                        x2 = x1 + widget.width
                        widget!!.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (first === widget!!.mRight && widget!!.mLeft.mTarget == null) {
                        x2 = r - widget!!.mRight.getMargin()
                        x1 = x2 - widget.width
                        widget!!.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (bothConnected && !widget!!.isInHorizontalChain) {
                        solveHorizontalCenterConstraints(level + 1, measurer, widget, isRtl)
                    }
                } else if ((widget.horizontalDimensionBehaviour
                            == DimensionBehaviour.MATCH_CONSTRAINT) && widget!!.mMatchConstraintMaxWidth >= 0 && widget!!.mMatchConstraintMinWidth >= 0 && (widget.visibility == ConstraintWidget.Companion.GONE
                            || ((widget!!.mMatchConstraintDefaultWidth
                            == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget!!.isInHorizontalChain && !widget!!.isInVirtualLayout
                ) {
                    if (bothConnected && !widget!!.isInHorizontalChain) {
                        solveHorizontalMatchConstraint(level + 1, layout, measurer, widget, isRtl)
                    }
                }
            }
        }
        layout.markHorizontalSolvingPassDone()
    }

    /**
     * Does an vertical solving pass for the given widget. This will walk through the widget's
     * vertical dependencies and if they can be resolved directly, do so.
     *
     * @param layout   the widget we want to solve the dependencies
     * @param measurer the measurer object to measure the widgets.
     */
    private fun verticalSolvingPass(
        level: Int,
        layout: ConstraintWidget?,
        measurer: BasicMeasure.Measurer?
    ) {
        if (EARLY_TERMINATION && layout!!.isVerticalSolvingPassDone) {
            if (DEBUG) {
                println(
                    ls(level) + "VERTICAL SOLVING PASS ON "
                            + layout.debugName + " ALREADY CALLED"
                )
            }
            return
        }
        sVcount++
        if (DEBUG) {
            println(ls(level) + "VERTICAL SOLVING PASS ON " + layout.getDebugName())
        }
        if (layout !is ConstraintWidgetContainer
            && layout!!.isMeasureRequested && canMeasure(level + 1, layout)
        ) {
            val measure = BasicMeasure.Measure()
            ConstraintWidgetContainer.Companion.measure(
                level + 1, layout,
                measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
            )
        }
        val top = layout.getAnchor(ConstraintAnchor.Type.TOP)
        val bottom = layout.getAnchor(ConstraintAnchor.Type.BOTTOM)
        val t: Int = top.getFinalValue()
        val b: Int = bottom.getFinalValue()
        if (top.getDependents() != null && top!!.hasFinalValue()) {
            for (first in top.dependents) {
                val widget = first.mOwner
                var y1 = 0
                var y2 = 0
                val canMeasure = canMeasure(level + 1, widget)
                if (widget!!.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.Companion.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
                    )
                }
                val bothConnected =
                    (first === widget!!.mTop && widget!!.mBottom.mTarget != null && widget!!.mBottom.mTarget.hasFinalValue() || first === widget!!.mBottom && widget!!.mTop.mTarget) != null && widget!!.mTop.mTarget.hasFinalValue()
                if (widget.verticalDimensionBehaviour
                    != DimensionBehaviour.MATCH_CONSTRAINT
                    || canMeasure
                ) {
                    if (widget!!.isMeasureRequested) {
                        // Widget needs to be measured
                        if (DEBUG) {
                            println(
                                ls(level + 1) + "(T) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first === widget!!.mTop && widget!!.mBottom.mTarget == null) {
                        y1 = t + widget!!.mTop.getMargin()
                        y2 = y1 + widget.height
                        widget!!.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (first === widget!!.mBottom && widget!!.mTop.mTarget == null) {
                        y2 = t - widget!!.mBottom.getMargin()
                        y1 = y2 - widget.height
                        widget!!.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (bothConnected && !widget!!.isInVerticalChain) {
                        solveVerticalCenterConstraints(level + 1, measurer, widget)
                    } else if (APPLY_MATCH_PARENT && widget.verticalDimensionBehaviour
                        == DimensionBehaviour.MATCH_PARENT
                    ) {
                        widget!!.setFinalVertical(0, widget.height)
                        verticalSolvingPass(level + 1, widget, measurer)
                    }
                } else if ((widget.verticalDimensionBehaviour
                            == DimensionBehaviour.MATCH_CONSTRAINT) && widget!!.mMatchConstraintMaxHeight >= 0 && widget!!.mMatchConstraintMinHeight >= 0 && (widget.visibility == ConstraintWidget.Companion.GONE
                            || ((widget!!.mMatchConstraintDefaultHeight
                            == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget!!.isInVerticalChain && !widget!!.isInVirtualLayout
                ) {
                    if (bothConnected && !widget!!.isInVerticalChain) {
                        solveVerticalMatchConstraint(level + 1, layout, measurer, widget)
                    }
                }
            }
        }
        if (layout is Guideline) {
            return
        }
        if (bottom.getDependents() != null && bottom!!.hasFinalValue()) {
            for (first in bottom.dependents) {
                val widget = first.mOwner
                val canMeasure = canMeasure(level + 1, widget)
                if (widget!!.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.Companion.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
                    )
                }
                var y1 = 0
                var y2 = 0
                val bothConnected =
                    (first === widget!!.mTop && widget!!.mBottom.mTarget != null && widget!!.mBottom.mTarget.hasFinalValue() || first === widget!!.mBottom && widget!!.mTop.mTarget) != null && widget!!.mTop.mTarget.hasFinalValue()
                if (widget.verticalDimensionBehaviour
                    != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget!!.isMeasureRequested) {
                        // Widget needs to be measured
                        if (DEBUG) {
                            println(
                                ls(level + 1) + "(B) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first === widget!!.mTop && widget!!.mBottom.mTarget == null) {
                        y1 = b + widget!!.mTop.getMargin()
                        y2 = y1 + widget.height
                        widget!!.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (first === widget!!.mBottom && widget!!.mTop.mTarget == null) {
                        y2 = b - widget!!.mBottom.getMargin()
                        y1 = y2 - widget.height
                        widget!!.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (bothConnected && !widget!!.isInVerticalChain) {
                        solveVerticalCenterConstraints(level + 1, measurer, widget)
                    }
                } else if ((widget.verticalDimensionBehaviour
                            == DimensionBehaviour.MATCH_CONSTRAINT) && widget!!.mMatchConstraintMaxHeight >= 0 && widget!!.mMatchConstraintMinHeight >= 0 && (widget.visibility == ConstraintWidget.Companion.GONE
                            || ((widget!!.mMatchConstraintDefaultHeight
                            == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget!!.isInVerticalChain && !widget!!.isInVirtualLayout
                ) {
                    if (bothConnected && !widget!!.isInVerticalChain) {
                        solveVerticalMatchConstraint(level + 1, layout, measurer, widget)
                    }
                }
            }
        }
        val baseline = layout.getAnchor(ConstraintAnchor.Type.BASELINE)
        if (baseline.getDependents() != null && baseline!!.hasFinalValue()) {
            val baselineValue: Int = baseline.finalValue
            for (first in baseline.dependents) {
                val widget = first.mOwner
                val canMeasure = canMeasure(level + 1, widget)
                if (widget!!.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.Companion.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
                    )
                }
                if (widget.verticalDimensionBehaviour
                    != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget!!.isMeasureRequested) {
                        // Widget needs to be measured
                        if (DEBUG) {
                            println(
                                ls(level + 1) + "(B) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first === widget!!.mBaseline) {
                        widget!!.setFinalBaseline(baselineValue + first.margin)
                        verticalSolvingPass(level + 1, widget, measurer)
                    }
                }
            }
        }
        layout.markVerticalSolvingPassDone()
    }

    /**
     * Solve horizontal centering constraints
     */
    private fun solveHorizontalCenterConstraints(
        level: Int,
        measurer: BasicMeasure.Measurer?,
        widget: ConstraintWidget?,
        isRtl: Boolean
    ) {
        // TODO: Handle match constraints here or before calling this
        var x1: Int
        var x2: Int
        var bias = widget.getHorizontalBiasPercent()
        val start: Int = widget!!.mLeft.mTarget.getFinalValue()
        val end: Int = widget.mRight.mTarget.getFinalValue()
        var s1: Int = start + widget.mLeft.getMargin()
        var s2: Int = end - widget.mRight.getMargin()
        if (start == end) {
            bias = 0.5f
            s1 = start
            s2 = end
        }
        val width = widget.width
        var distance = s2 - s1 - width
        if (s1 > s2) {
            distance = s1 - s2 - width
        }
        val d1: Int
        d1 = if (distance > 0) {
            (0.5f + bias * distance).toInt()
        } else {
            (bias * distance).toInt()
        }
        x1 = s1 + d1
        x2 = x1 + width
        if (s1 > s2) {
            x1 = s1 + d1
            x2 = x1 - width
        }
        widget.setFinalHorizontal(x1, x2)
        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
    }

    /**
     * Solve vertical centering constraints
     */
    private fun solveVerticalCenterConstraints(
        level: Int,
        measurer: BasicMeasure.Measurer?,
        widget: ConstraintWidget?
    ) {
        // TODO: Handle match constraints here or before calling this
        var y1: Int
        var y2: Int
        var bias = widget.getVerticalBiasPercent()
        val start: Int = widget!!.mTop.mTarget.getFinalValue()
        val end: Int = widget.mBottom.mTarget.getFinalValue()
        var s1: Int = start + widget.mTop.getMargin()
        var s2: Int = end - widget.mBottom.getMargin()
        if (start == end) {
            bias = 0.5f
            s1 = start
            s2 = end
        }
        val height = widget.height
        var distance = s2 - s1 - height
        if (s1 > s2) {
            distance = s1 - s2 - height
        }
        val d1: Int
        d1 = if (distance > 0) {
            (0.5f + bias * distance).toInt()
        } else {
            (bias * distance).toInt()
        }
        y1 = s1 + d1
        y2 = y1 + height
        if (s1 > s2) {
            y1 = s1 - d1
            y2 = y1 - height
        }
        widget.setFinalVertical(y1, y2)
        verticalSolvingPass(level + 1, widget, measurer)
    }

    /**
     * Solve horizontal match constraints
     */
    private fun solveHorizontalMatchConstraint(
        level: Int,
        layout: ConstraintWidget?,
        measurer: BasicMeasure.Measurer?,
        widget: ConstraintWidget?,
        isRtl: Boolean
    ) {
        val x1: Int
        val x2: Int
        val bias = widget.getHorizontalBiasPercent()
        val s1: Int = widget!!.mLeft.mTarget.getFinalValue() + widget.mLeft.getMargin()
        val s2: Int = widget.mRight.mTarget.getFinalValue() - widget.mRight.getMargin()
        if (s2 >= s1) {
            var width = widget.width
            if (widget.visibility != ConstraintWidget.Companion.GONE) {
                if (widget.mMatchConstraintDefaultWidth
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
                ) {
                    var parentWidth = 0
                    if (layout is ConstraintWidgetContainer) {
                        parentWidth = layout.width
                    } else {
                        parentWidth = layout.getParent().getWidth()
                    }
                    width = (0.5f * widget.horizontalBiasPercent * parentWidth).toInt()
                } else if (widget.mMatchConstraintDefaultWidth
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                ) {
                    width = s2 - s1
                }
                width = java.lang.Math.max(widget.mMatchConstraintMinWidth, width)
                if (widget.mMatchConstraintMaxWidth > 0) {
                    width = java.lang.Math.min(widget.mMatchConstraintMaxWidth, width)
                }
            }
            val distance = s2 - s1 - width
            val d1 = (0.5f + bias * distance).toInt()
            x1 = s1 + d1
            x2 = x1 + width
            widget.setFinalHorizontal(x1, x2)
            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
        }
    }

    /**
     * Solve vertical match constraints
     */
    private fun solveVerticalMatchConstraint(
        level: Int,
        layout: ConstraintWidget?,
        measurer: BasicMeasure.Measurer?,
        widget: ConstraintWidget?
    ) {
        val y1: Int
        val y2: Int
        val bias = widget.getVerticalBiasPercent()
        val s1: Int = widget!!.mTop.mTarget.getFinalValue() + widget.mTop.getMargin()
        val s2: Int = widget.mBottom.mTarget.getFinalValue() - widget.mBottom.getMargin()
        if (s2 >= s1) {
            var height = widget.height
            if (widget.visibility != ConstraintWidget.Companion.GONE) {
                if (widget.mMatchConstraintDefaultHeight
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
                ) {
                    var parentHeight = 0
                    if (layout is ConstraintWidgetContainer) {
                        parentHeight = layout.height
                    } else {
                        parentHeight = layout.getParent().getHeight()
                    }
                    height = (0.5f * bias * parentHeight).toInt()
                } else if (widget.mMatchConstraintDefaultHeight
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                ) {
                    height = s2 - s1
                }
                height = java.lang.Math.max(widget.mMatchConstraintMinHeight, height)
                if (widget.mMatchConstraintMaxHeight > 0) {
                    height = java.lang.Math.min(widget.mMatchConstraintMaxHeight, height)
                }
            }
            val distance = s2 - s1 - height
            val d1 = (0.5f + bias * distance).toInt()
            y1 = s1 + d1
            y2 = y1 + height
            widget.setFinalVertical(y1, y2)
            verticalSolvingPass(level + 1, widget, measurer)
        }
    }

    /**
     * Returns true if the dimensions of the given widget are computable directly
     *
     * @param layout the widget to check
     * @return true if both dimensions are knowable by a single measure pass
     */
    private fun canMeasure(level: Int, layout: ConstraintWidget?): Boolean {
        val horizontalBehaviour: DimensionBehaviour? = layout.getHorizontalDimensionBehaviour()
        val verticalBehaviour: DimensionBehaviour? = layout.getVerticalDimensionBehaviour()
        val parent = if (layout.getParent() != null) layout.getParent() as ConstraintWidgetContainer else null
        val isParentHorizontalFixed = parent != null && parent.getHorizontalDimensionBehaviour()
        == DimensionBehaviour.FIXED
        val isParentVerticalFixed = parent != null && parent.getVerticalDimensionBehaviour()
        == DimensionBehaviour.FIXED
        val isHorizontalFixed =
            horizontalBehaviour == DimensionBehaviour.FIXED || layout!!.isResolvedHorizontally || APPLY_MATCH_PARENT && (horizontalBehaviour
                    == DimensionBehaviour.MATCH_PARENT) && isParentHorizontalFixed || horizontalBehaviour == DimensionBehaviour.WRAP_CONTENT || horizontalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && (layout.mMatchConstraintDefaultWidth
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) && layout.mDimensionRatio == 0f && layout.hasDanglingDimension(
                ConstraintWidget.Companion.HORIZONTAL
            ) || horizontalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && layout.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP && layout.hasResolvedTargets(
                ConstraintWidget.Companion.HORIZONTAL,
                layout.width
            )
        val isVerticalFixed =
            (verticalBehaviour == DimensionBehaviour.FIXED || layout!!.isResolvedVertically || APPLY_MATCH_PARENT && (verticalBehaviour
                    == DimensionBehaviour.MATCH_PARENT) && isParentVerticalFixed || verticalBehaviour == DimensionBehaviour.WRAP_CONTENT || verticalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && (layout.mMatchConstraintDefaultHeight
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) && layout.mDimensionRatio) == 0f && layout!!.hasDanglingDimension(
                ConstraintWidget.Companion.VERTICAL
            ) || verticalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && layout!!.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP && layout.hasResolvedTargets(
                ConstraintWidget.Companion.VERTICAL,
                layout.height
            )
        if (layout.mDimensionRatio > 0 && (isHorizontalFixed || isVerticalFixed)) {
            return true
        }
        if (DEBUG) {
            println(
                ls(level) + "can measure " + layout.getDebugName() + " ? "
                        + (isHorizontalFixed && isVerticalFixed) + "  [ "
                        + isHorizontalFixed + " (horiz " + horizontalBehaviour + ") & "
                        + isVerticalFixed + " (vert " + verticalBehaviour + ") ]"
            )
        }
        return isHorizontalFixed && isVerticalFixed
    }

    /**
     * Try to directly resolve the chain
     *
     * @return true if fully resolved
     */
    fun solveChain(
        container: ConstraintWidgetContainer, system: LinearSystem,
        orientation: Int, offset: Int, chainHead: ChainHead,
        isChainSpread: Boolean, isChainSpreadInside: Boolean,
        isChainPacked: Boolean
    ): Boolean {
        if (LinearSystem.Companion.FULL_DEBUG) {
            println("\n### SOLVE CHAIN ###")
        }
        if (isChainPacked) {
            return false
        }
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            if (!container.isResolvedHorizontally()) {
                return false
            }
        } else {
            if (!container.isResolvedVertically()) {
                return false
            }
        }
        val level = 0 // nested level (used for debugging)
        val isRtl = container.isRtl
        val first = chainHead.first
        val last = chainHead.last
        val firstVisibleWidget = chainHead.firstVisibleWidget
        val lastVisibleWidget = chainHead.lastVisibleWidget
        val head = chainHead.head
        var widget = first
        var next: ConstraintWidget?
        var done = false
        val begin: ConstraintAnchor = first!!.mListAnchors.get(offset)
        val end: ConstraintAnchor = last!!.mListAnchors.get(offset + 1)
        if (begin.mTarget == null || end.mTarget == null) {
            return false
        }
        if (!begin.mTarget.hasFinalValue() || !end.mTarget.hasFinalValue()) {
            return false
        }
        if (firstVisibleWidget == null || lastVisibleWidget == null) {
            return false
        }
        val startPoint: Int = (begin.mTarget.getFinalValue()
                + firstVisibleWidget.mListAnchors.get(offset).getMargin())
        val endPoint: Int = (end.mTarget.getFinalValue()
                - lastVisibleWidget.mListAnchors.get(offset + 1).getMargin())
        val distance = endPoint - startPoint
        if (distance <= 0) {
            return false
        }
        var totalSize = 0
        val measure = BasicMeasure.Measure()
        var numWidgets = 0
        var numVisibleWidgets = 0
        while (!done) {
            val canMeasure = canMeasure(level + 1, widget)
            if (!canMeasure) {
                return false
            }
            if (widget!!.mListDimensionBehaviors.get(orientation)
                == DimensionBehaviour.MATCH_CONSTRAINT
            ) {
                return false
            }
            if (widget.isMeasureRequested) {
                ConstraintWidgetContainer.Companion.measure(
                    level + 1, widget,
                    container.measurer, measure, BasicMeasure.Measure.Companion.SELF_DIMENSIONS
                )
            }
            totalSize += widget.mListAnchors.get(offset).getMargin()
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                totalSize += +widget.width
            } else {
                totalSize += widget.height
            }
            totalSize += widget.mListAnchors.get(offset + 1).getMargin()
            numWidgets++
            if (widget.visibility != ConstraintWidget.Companion.GONE) {
                numVisibleWidgets++
            }


            // go to the next widget
            val nextAnchor: ConstraintAnchor = widget.mListAnchors.get(offset + 1).mTarget
            if (nextAnchor != null) {
                next = nextAnchor.mOwner
                if (next!!.mListAnchors.get(offset).mTarget == null
                    || next.mListAnchors.get(offset).mTarget.mOwner !== widget
                ) {
                    next = null
                }
            } else {
                next = null
            }
            if (next != null) {
                widget = next
            } else {
                done = true
            }
        }
        if (numVisibleWidgets == 0) {
            return false
        }
        if (numVisibleWidgets != numWidgets) {
            return false
        }
        if (distance < totalSize) {
            return false
        }
        var gap = distance - totalSize
        if (isChainSpread) {
            gap = gap / (numVisibleWidgets + 1)
        } else if (isChainSpreadInside) {
            if (numVisibleWidgets > 2) {
                gap = gap / numVisibleWidgets - 1
            }
        }
        if (numVisibleWidgets == 1) {
            val bias: Float
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                bias = head.getHorizontalBiasPercent()
            } else {
                bias = head.getVerticalBiasPercent()
            }
            val p1 = (0.5f + startPoint + gap * bias).toInt()
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                firstVisibleWidget.setFinalHorizontal(p1, p1 + firstVisibleWidget.width)
            } else {
                firstVisibleWidget.setFinalVertical(p1, p1 + firstVisibleWidget.height)
            }
            horizontalSolvingPass(
                level + 1,
                firstVisibleWidget, container.measurer, isRtl
            )
            return true
        }
        if (isChainSpread) {
            done = false
            var current = startPoint + gap
            widget = first
            while (!done) {
                if (widget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        widget!!.setFinalHorizontal(current, current)
                        horizontalSolvingPass(
                            level + 1,
                            widget, container.measurer, isRtl
                        )
                    } else {
                        widget!!.setFinalVertical(current, current)
                        verticalSolvingPass(level + 1, widget, container.measurer)
                    }
                } else {
                    current += widget!!.mListAnchors.get(offset).getMargin()
                    if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        widget.setFinalHorizontal(current, current + widget.width)
                        horizontalSolvingPass(
                            level + 1,
                            widget, container.measurer, isRtl
                        )
                        current += widget.width
                    } else {
                        widget.setFinalVertical(current, current + widget.height)
                        verticalSolvingPass(level + 1, widget, container.measurer)
                        current += widget.height
                    }
                    current += widget.mListAnchors.get(offset + 1).getMargin()
                    current += gap
                }
                widget.addToSolver(system, false)

                // go to the next widget
                val nextAnchor: ConstraintAnchor = widget.mListAnchors.get(offset + 1).mTarget
                if (nextAnchor != null) {
                    next = nextAnchor.mOwner
                    if (next!!.mListAnchors.get(offset).mTarget == null
                        || next.mListAnchors.get(offset).mTarget.mOwner !== widget
                    ) {
                        next = null
                    }
                } else {
                    next = null
                }
                if (next != null) {
                    widget = next
                } else {
                    done = true
                }
            }
        } else if (isChainSpreadInside) {
            if (numVisibleWidgets == 2) {
                if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    firstVisibleWidget.setFinalHorizontal(
                        startPoint,
                        startPoint + firstVisibleWidget.width
                    )
                    lastVisibleWidget.setFinalHorizontal(
                        endPoint - lastVisibleWidget.width,
                        endPoint
                    )
                    horizontalSolvingPass(
                        level + 1,
                        firstVisibleWidget, container.measurer, isRtl
                    )
                    horizontalSolvingPass(
                        level + 1,
                        lastVisibleWidget, container.measurer, isRtl
                    )
                } else {
                    firstVisibleWidget.setFinalVertical(
                        startPoint,
                        startPoint + firstVisibleWidget.height
                    )
                    lastVisibleWidget.setFinalVertical(
                        endPoint - lastVisibleWidget.height,
                        endPoint
                    )
                    verticalSolvingPass(
                        level + 1,
                        firstVisibleWidget, container.measurer
                    )
                    verticalSolvingPass(
                        level + 1,
                        lastVisibleWidget, container.measurer
                    )
                }
                return true
            }
            return false
        }
        return true
    }
}
