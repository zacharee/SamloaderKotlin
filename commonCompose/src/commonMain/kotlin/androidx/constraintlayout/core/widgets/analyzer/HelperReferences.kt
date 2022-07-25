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

import androidx.constraintlayout.core.widgets.Barrier
import androidx.constraintlayout.core.widgets.ConstraintWidget

internal class HelperReferences(widget: ConstraintWidget) : WidgetRun(widget) {
    override fun clear() {
        mRunGroup = null
        start.clear()
    }

    override fun reset() {
        start.resolved = false
    }

    override fun supportsWrapComputation(): Boolean {
        return false
    }

    private fun addDependency(node: DependencyNode) {
        start.mDependencies.add(node)
        node.mTargets.add(start)
    }

    override fun apply() {
        if (mWidget is Barrier) {
            start.delegateToWidgetRun = true
            val barrier = mWidget as Barrier
            val type = barrier.barrierType
            val allowsGoneWidget = barrier.allowsGoneWidget
            when (type) {
                Barrier.LEFT -> {
                    start.mType = DependencyNode.Type.LEFT
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refWidget: ConstraintWidget = barrier.mWidgets.get(i)!!
                        if (!allowsGoneWidget
                            && refWidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target: DependencyNode = refWidget.mHorizontalRun!!.start
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget.mHorizontalRun!!.start)
                    addDependency(mWidget.mHorizontalRun!!.end)
                }

                Barrier.RIGHT -> {
                    start.mType = DependencyNode.Type.RIGHT
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refWidget: ConstraintWidget = barrier.mWidgets.get(i)!!
                        if (!allowsGoneWidget
                            && refWidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target: DependencyNode = refWidget.mHorizontalRun!!.end
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget.mHorizontalRun!!.start)
                    addDependency(mWidget.mHorizontalRun!!.end)
                }

                Barrier.TOP -> {
                    start.mType = DependencyNode.Type.TOP
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refwidget: ConstraintWidget = barrier.mWidgets.get(i)!!
                        if (!allowsGoneWidget
                            && refwidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target: DependencyNode = refwidget.mVerticalRun!!.start
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget.mVerticalRun!!.start)
                    addDependency(mWidget.mVerticalRun!!.end)
                }

                Barrier.BOTTOM -> {
                    start.mType = DependencyNode.Type.BOTTOM
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refwidget: ConstraintWidget = barrier.mWidgets.get(i)!!
                        if (!allowsGoneWidget
                            && refwidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target: DependencyNode = refwidget.mVerticalRun!!.end
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget.mVerticalRun!!.start)
                    addDependency(mWidget.mVerticalRun!!.end)
                }
            }
        }
    }

    override fun update(dependency: Dependency?) {
        val barrier = mWidget as Barrier
        val type = barrier.barrierType
        var min = -1
        var max = 0
        for (node in start.mTargets) {
            val value = node!!.value
            if (min == -1 || value < min) {
                min = value
            }
            if (max < value) {
                max = value
            }
        }
        if (type == Barrier.LEFT || type == Barrier.TOP) {
            start.resolve(min + barrier.margin)
        } else {
            start.resolve(max + barrier.margin)
        }
    }

    override fun applyToWidget() {
        if (mWidget is Barrier) {
            val barrier = mWidget as Barrier
            val type = barrier.barrierType
            if (type == Barrier.LEFT
                || type == Barrier.RIGHT
            ) {
                mWidget.x = (start.value)
            } else {
                mWidget.y = (start.value)
            }
        }
    }
}
