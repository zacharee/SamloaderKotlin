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

import androidx.constraintlayout.core.widgets.ConstraintWidget
import androidx.constraintlayout.core.widgets.Guideline

internal class GuidelineReference(widget: ConstraintWidget) : WidgetRun(widget) {
    init {
        widget.mHorizontalRun!!.clear()
        widget.mVerticalRun!!.clear()
        orientation = (widget as Guideline).orientation
    }

    public override fun clear() {
        start.clear()
    }

    public override fun reset() {
        start.resolved = false
        end.resolved = false
    }

    public override fun supportsWrapComputation(): Boolean {
        return false
    }

    private fun addDependency(
        node: DependencyNode
    ) {
        start.mDependencies.add(node)
        node.mTargets.add(start)
    }

    override fun update(dependency: Dependency?) {
        if (!start.readyToSolve) {
            return
        }
        if (start.resolved) {
            return
        }
        // ready to solve, centering.
        val startTarget = start.mTargets.get(0)!!
        val guideline = mWidget as Guideline
        val startPos = (0.5f + startTarget.value * guideline.relativePercent).toInt()
        start.resolve(startPos)
    }

    public override fun apply() {
        val guideline = mWidget as Guideline
        val relativeBegin = guideline.relativeBegin
        val relativeEnd = guideline.relativeEnd
        val percent = guideline.relativePercent
        if (guideline.orientation == ConstraintWidget.Companion.VERTICAL) {
            if (relativeBegin != -1) {
                start.mTargets.add(mWidget.parent!!.mHorizontalRun!!.start)
                mWidget.parent!!.mHorizontalRun!!.start.mDependencies.add(start)
                start.mMargin = relativeBegin
            } else if (relativeEnd != -1) {
                start.mTargets.add(mWidget.parent!!.mHorizontalRun!!.end)
                mWidget.parent!!.mHorizontalRun!!.end.mDependencies.add(start)
                start.mMargin = -relativeEnd
            } else {
                start.delegateToWidgetRun = true
                start.mTargets.add(mWidget.parent!!.mHorizontalRun!!.end)
                mWidget.parent!!.mHorizontalRun!!.end.mDependencies.add(start)
            }
            // FIXME -- if we move the DependencyNode directly
            //              in the ConstraintAnchor we'll be good.
            addDependency(mWidget.mHorizontalRun!!.start)
            addDependency(mWidget.mHorizontalRun!!.end)
        } else {
            if (relativeBegin != -1) {
                start.mTargets.add(mWidget.parent!!.mVerticalRun!!.start)
                mWidget.parent!!.mVerticalRun!!.start.mDependencies.add(start)
                start.mMargin = relativeBegin
            } else if (relativeEnd != -1) {
                start.mTargets.add(mWidget.parent!!.mVerticalRun!!.end)
                mWidget.parent!!.mVerticalRun!!.end.mDependencies.add(start)
                start.mMargin = -relativeEnd
            } else {
                start.delegateToWidgetRun = true
                start.mTargets.add(mWidget.parent!!.mVerticalRun!!.end)
                mWidget.parent!!.mVerticalRun!!.end.mDependencies.add(start)
            }
            // FIXME -- if we move the DependencyNode directly
            //              in the ConstraintAnchor we'll be good.
            addDependency(mWidget.mVerticalRun!!.start)
            addDependency(mWidget.mVerticalRun!!.end)
        }
    }

    public override fun applyToWidget() {
        val guideline = mWidget as Guideline
        if (guideline.orientation == ConstraintWidget.Companion.VERTICAL) {
            mWidget.x = (start.value)
        } else {
            mWidget.y = (start.value)
        }
    }
}
