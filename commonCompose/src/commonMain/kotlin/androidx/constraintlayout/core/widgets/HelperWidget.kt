/*
 * Copyright (C) 2021 The Android Open Source Project
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

import androidx.constraintlayout.core.widgets.analyzer.Grouping
import androidx.constraintlayout.core.widgets.analyzer.WidgetGroup

/**
 * HelperWidget class
 */
open class HelperWidget : ConstraintWidget(), Helper {
    var mWidgets = arrayOfNulls<ConstraintWidget>(4)
    var mWidgetsCount = 0
    override fun updateConstraints(container: ConstraintWidgetContainer?) {
        // nothing here
    }

    /**
     * Add a widget to the helper
     *
     * @param widget a widget
     */
    override fun add(widget: ConstraintWidget?) {
        if (widget === this || widget == null) {
            return
        }
        if (mWidgetsCount + 1 > mWidgets.size) {
            mWidgets = mWidgets.copyOf(mWidgets.size * 2)
        }
        mWidgets[mWidgetsCount] = widget
        mWidgetsCount++
    }

    override fun copy(src: ConstraintWidget, map: HashMap<ConstraintWidget?, ConstraintWidget?>) {
        super.copy(src, map)
        val srcHelper = src as HelperWidget
        mWidgetsCount = 0
        val count = srcHelper.mWidgetsCount
        for (i in 0 until count) {
            add(map.get(srcHelper.mWidgets[i]))
        }
    }

    /**
     * Reset the widgets list contained by this helper
     */
    override fun removeAllIds() {
        mWidgetsCount = 0
        mWidgets.fill(null)
    }

    /**
     * @TODO: add description
     */
    fun addDependents(
        dependencyLists: ArrayList<WidgetGroup>,
        orientation: Int,
        group: WidgetGroup?
    ) {
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]
            group!!.add(widget!!)
        }
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]
            Grouping.findDependents(widget, orientation, dependencyLists, group)
        }
    }

    /**
     * @TODO: add description
     */
    fun findGroupInDependents(orientation: Int): Int {
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]
            if (orientation == ConstraintWidget.Companion.HORIZONTAL && widget!!.horizontalGroup != -1) {
                return widget.horizontalGroup
            }
            if (orientation == ConstraintWidget.Companion.VERTICAL && widget!!.verticalGroup != -1) {
                return widget.verticalGroup
            }
        }
        return -1
    }
}
