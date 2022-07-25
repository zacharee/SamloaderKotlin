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

import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.core.widgets.analyzer.BasicMeasure

/**
 * Simple VirtualLayout that center the first referenced widget onto itself.
 */
class Placeholder : VirtualLayout() {
    /**
     * @TODO: add description
     */
    override fun measure(widthMode: Int, widthSize: Int, heightMode: Int, heightSize: Int) {
        var width = 0
        var height = 0
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom
        if (mWidgetsCount > 0) {
            // grab the first referenced widget size in case we are ourselves in wrap_content
            width += mWidgets.get(0).getWidth()
            height += mWidgets.get(0).getHeight()
        }
        width = java.lang.Math.max(getMinWidth(), width)
        height = java.lang.Math.max(getMinHeight(), height)
        var measuredWidth = 0
        var measuredHeight = 0
        if (widthMode == BasicMeasure.Companion.EXACTLY) {
            measuredWidth = widthSize
        } else if (widthMode == BasicMeasure.Companion.AT_MOST) {
            measuredWidth = java.lang.Math.min(width, widthSize)
        } else if (widthMode == BasicMeasure.Companion.UNSPECIFIED) {
            measuredWidth = width
        }
        if (heightMode == BasicMeasure.Companion.EXACTLY) {
            measuredHeight = heightSize
        } else if (heightMode == BasicMeasure.Companion.AT_MOST) {
            measuredHeight = java.lang.Math.min(height, heightSize)
        } else if (heightMode == BasicMeasure.Companion.UNSPECIFIED) {
            measuredHeight = height
        }
        setMeasure(measuredWidth, measuredHeight)
        setWidth(measuredWidth)
        setHeight(measuredHeight)
        needsCallbackFromSolver(mWidgetsCount > 0)
    }

    override fun addToSolver(system: LinearSystem?, optimize: Boolean) {
        super.addToSolver(system!!, optimize)
        if (mWidgetsCount > 0) {
            val widget: ConstraintWidget = mWidgets.get(0)!!
            widget.resetAllConstraints()
            widget.connect(ConstraintAnchor.Type.LEFT, this, ConstraintAnchor.Type.LEFT)
            widget.connect(ConstraintAnchor.Type.RIGHT, this, ConstraintAnchor.Type.RIGHT)
            widget.connect(ConstraintAnchor.Type.TOP, this, ConstraintAnchor.Type.TOP)
            widget.connect(ConstraintAnchor.Type.BOTTOM, this, ConstraintAnchor.Type.BOTTOM)
        }
    }
}
