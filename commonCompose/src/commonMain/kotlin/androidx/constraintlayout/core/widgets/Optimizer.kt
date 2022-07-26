/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.constraintlayout.core.widgets

import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour
import kotlin.native.concurrent.ThreadLocal

/**
 * Implements direct resolution without using the solver
 */
@ThreadLocal
object Optimizer {
    // Optimization levels (mask)
    const val OPTIMIZATION_NONE = 0
    const val OPTIMIZATION_DIRECT = 1
    const val OPTIMIZATION_BARRIER = 1 shl 1
    const val OPTIMIZATION_CHAIN = 1 shl 2
    const val OPTIMIZATION_DIMENSIONS = 1 shl 3
    const val OPTIMIZATION_RATIO = 1 shl 4
    const val OPTIMIZATION_GROUPS = 1 shl 5
    const val OPTIMIZATION_GRAPH = 1 shl 6
    const val OPTIMIZATION_GRAPH_WRAP = 1 shl 7
    const val OPTIMIZATION_CACHE_MEASURES = 1 shl 8
    const val OPTIMIZATION_DEPENDENCY_ORDERING = 1 shl 9
    const val OPTIMIZATION_GROUPING = 1 shl 10
    const val OPTIMIZATION_STANDARD =
        (OPTIMIZATION_DIRECT /* | OPTIMIZATION_GROUPING */ /* | OPTIMIZATION_DEPENDENCY_ORDERING */
                or OPTIMIZATION_CACHE_MEASURES /* | OPTIMIZATION_GRAPH */ /* | OPTIMIZATION_GRAPH_WRAP */ /* | OPTIMIZATION_DIMENSIONS */)

    // Internal use.
    var sFlags = BooleanArray(3)
    const val FLAG_USE_OPTIMIZE = 0 // simple enough to use optimizer
    const val FLAG_CHAIN_DANGLING = 1
    const val FLAG_RECOMPUTE_BOUNDS = 2

    /**
     * Looks at optimizing match_parent
     */
    fun checkMatchParent(
        container: ConstraintWidgetContainer,
        system: LinearSystem,
        widget: ConstraintWidget
    ) {
        widget.mHorizontalResolution = ConstraintWidget.UNKNOWN
        widget.mVerticalResolution = ConstraintWidget.UNKNOWN
        if (container.mListDimensionBehaviors.get(ConstraintWidget.DIMENSION_HORIZONTAL)
            != DimensionBehaviour.WRAP_CONTENT
            && widget.mListDimensionBehaviors[ConstraintWidget.DIMENSION_HORIZONTAL]
            == DimensionBehaviour.MATCH_PARENT
        ) {
            val left: Int = widget.mLeft.mMargin
            val right: Int = container.width - widget.mRight.mMargin
            widget.mLeft.solverVariable = system.createObjectVariable(widget.mLeft)
            widget.mRight.solverVariable = system.createObjectVariable(widget.mRight)
            system.addEquality(widget.mLeft.solverVariable!!, left)
            system.addEquality(widget.mRight.solverVariable!!, right)
            widget.mHorizontalResolution = ConstraintWidget.DIRECT
            widget.setHorizontalDimension(left, right)
        }
        if (container.mListDimensionBehaviors.get(ConstraintWidget.DIMENSION_VERTICAL)
            != DimensionBehaviour.WRAP_CONTENT
            && widget.mListDimensionBehaviors[ConstraintWidget.DIMENSION_VERTICAL]
            == DimensionBehaviour.MATCH_PARENT
        ) {
            val top: Int = widget.mTop.mMargin
            val bottom: Int = container.height - widget.mBottom.mMargin
            widget.mTop.solverVariable = system.createObjectVariable(widget.mTop)
            widget.mBottom.solverVariable = system.createObjectVariable(widget.mBottom)
            system.addEquality(widget.mTop.solverVariable!!, top)
            system.addEquality(widget.mBottom.solverVariable!!, bottom)
            if (widget.mBaselineDistance > 0 || widget.visibility == ConstraintWidget.GONE) {
                widget.mBaseline.solverVariable = system.createObjectVariable(widget.mBaseline)
                system.addEquality(
                    widget.mBaseline.solverVariable!!,
                    top + widget.mBaselineDistance
                )
            }
            widget.mVerticalResolution = ConstraintWidget.DIRECT
            widget.setVerticalDimension(top, bottom)
        }
    }

    /**
     * @TODO: add description
     */
    fun enabled(optimizationLevel: Int, optimization: Int): Boolean {
        return optimizationLevel and optimization == optimization
    }
}
