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
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer
import kotlin.native.concurrent.ThreadLocal

/**
 * Represents a group of widget for the grouping mechanism.
 */
class WidgetGroup(orientation: Int) {
    var mWidgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
    var id = -1
    var isAuthoritative = false
    var orientation: Int = ConstraintWidget.HORIZONTAL
    var mResults: ArrayList<MeasureResult>? = null
    private var mMoveTo = -1

    init {
        id = sCount++
        this.orientation = orientation
    }

    /**
     * @TODO: add description
     */
    fun add(widget: ConstraintWidget): Boolean {
        if (mWidgets.contains(widget)) {
            return false
        }
        mWidgets.add(widget)
        return true
    }

    private val orientationString: String
        get() {
            if (orientation == ConstraintWidget.HORIZONTAL) {
                return "Horizontal"
            } else if (orientation == ConstraintWidget.VERTICAL) {
                return "Vertical"
            } else if (orientation == ConstraintWidget.BOTH) {
                return "Both"
            }
            return "Unknown"
        }

    override fun toString(): String {
        var ret = "$orientationString [$id] <"
        for (widget in mWidgets) {
            ret += " " + widget.debugName
        }
        ret += " >"
        return ret
    }

    /**
     * @TODO: add description
     */
    fun moveTo(orientation: Int, widgetGroup: WidgetGroup) {
        if (DEBUG) {
            println(
                "Move all widgets (" + this + ") from "
                        + id + " to " + widgetGroup.id + "(" + widgetGroup + ")"
            )
            println(
                "" +
                        "do not call  " + measureWrap(orientation, ConstraintWidget())
            )
        }
        for (widget in mWidgets) {
            widgetGroup.add(widget)
            if (orientation == ConstraintWidget.HORIZONTAL) {
                widget.horizontalGroup = widgetGroup.id
            } else {
                widget.verticalGroup = widgetGroup.id
            }
        }
        mMoveTo = widgetGroup.id
    }

    /**
     * @TODO: add description
     */
    fun clear() {
        mWidgets.clear()
    }

    private fun measureWrap(orientation: Int, widget: ConstraintWidget): Int {
        val behaviour: DimensionBehaviour? = widget.getDimensionBehaviour(orientation)
        if (behaviour == DimensionBehaviour.WRAP_CONTENT || behaviour == DimensionBehaviour.MATCH_PARENT || behaviour == DimensionBehaviour.FIXED) {
            val dimension = if (orientation == ConstraintWidget.HORIZONTAL) {
                widget.width
            } else {
                widget.height
            }
            return dimension
        }
        return -1
    }

    /**
     * @TODO: add description
     */
    fun measureWrap(system: LinearSystem, orientation: Int): Int {
        val count: Int = mWidgets.size
        return if (count == 0) {
            0
        } else solverMeasure(system, mWidgets, orientation)
        // TODO: add direct wrap computation for simpler cases instead of calling the solver
    }

    private fun solverMeasure(
        system: LinearSystem,
        widgets: ArrayList<ConstraintWidget>,
        orientation: Int
    ): Int {
        val container = widgets[0].parent as ConstraintWidgetContainer
        system.reset()
        val prevDebug: Boolean = LinearSystem.FULL_DEBUG
        container.addToSolver(system, false)
        for (i in widgets.indices) {
            val widget: ConstraintWidget = widgets[i]
            widget.addToSolver(system, false)
        }
        if (orientation == ConstraintWidget.HORIZONTAL) {
            if (container.mHorizontalChainsSize > 0) {
                Chain.applyChainConstraints(container, system, widgets, ConstraintWidget.HORIZONTAL)
            }
        }
        if (orientation == ConstraintWidget.VERTICAL) {
            if (container.mVerticalChainsSize > 0) {
                Chain.applyChainConstraints(container, system, widgets, ConstraintWidget.VERTICAL)
            }
        }
        try {
            system.minimize()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // save results
        mResults = ArrayList()
        for (i in widgets.indices) {
            val widget: ConstraintWidget = widgets[i]
            val result = MeasureResult(widget, system, orientation)
            mResults!!.add(result)
        }
        return if (orientation == ConstraintWidget.HORIZONTAL) {
            val left = system.getObjectVariableValue(container.mLeft)
            val right = system.getObjectVariableValue(container.mRight)
            system.reset()
            right - left
        } else {
            val top = system.getObjectVariableValue(container.mTop)
            val bottom = system.getObjectVariableValue(container.mBottom)
            system.reset()
            bottom - top
        }
    }

    /**
     * @TODO: add description
     */
    fun apply() {
        if (mResults == null) {
            return
        }
        if (!isAuthoritative) {
            return
        }
        for (i in mResults!!.indices) {
            val result: MeasureResult = mResults!![i]
            result.apply()
        }
    }

    /**
     * @TODO: add description
     */
    fun intersectWith(group: WidgetGroup): Boolean {
        for (i in mWidgets.indices) {
            val widget: ConstraintWidget = mWidgets[i]
            if (group.contains(widget)) {
                return true
            }
        }
        return false
    }

    private operator fun contains(widget: ConstraintWidget): Boolean {
        return mWidgets.contains(widget)
    }

    /**
     * @TODO: add description
     */
    fun size(): Int {
        return mWidgets.size
    }

    /**
     * @TODO: add description
     */
    fun cleanup(dependencyLists: ArrayList<WidgetGroup>) {
        val count: Int = mWidgets.size
        if (mMoveTo != -1 && count > 0) {
            for (i in dependencyLists.indices) {
                val group: WidgetGroup = dependencyLists[i]
                if (mMoveTo == group.id) {
                    moveTo(orientation, group)
                }
            }
        }
        if (count == 0) {
            dependencyLists.remove(this)
            return
        }
    }

    class MeasureResult(widget: ConstraintWidget, system: LinearSystem, orientation: Int) {
        var mWidgetRef: ConstraintWidget? = null
        var mLeft: Int
        var mTop: Int
        var mRight: Int
        var mBottom: Int
        var mBaseline: Int
        var mOrientation: Int

        init {
            mWidgetRef = widget
            mLeft = system.getObjectVariableValue(widget.mLeft)
            mTop = system.getObjectVariableValue(widget.mTop)
            mRight = system.getObjectVariableValue(widget.mRight)
            mBottom = system.getObjectVariableValue(widget.mBottom)
            mBaseline = system.getObjectVariableValue(widget.mBaseline)
            mOrientation = orientation
        }

        fun apply() {
            mWidgetRef?.setFinalFrame(mLeft, mTop, mRight, mBottom, mBaseline, mOrientation)
        }
    }

    @ThreadLocal
    companion object {
        private const val DEBUG = false
        var sCount = 0
    }
}
