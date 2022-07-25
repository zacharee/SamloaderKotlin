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
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer
import kotlin.math.max
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal

class RunGroup(run: WidgetRun?, dir: Int) {
    var position = 0
    var dual = false
    var mFirstRun: WidgetRun? = null
    var mLastRun: WidgetRun? = null
    var mRuns: ArrayList<WidgetRun> = ArrayList()
    var mGroupIndex = 0
    var mDirection: Int

    init {
        mGroupIndex = index
        index++
        mFirstRun = run
        mLastRun = run
        mDirection = dir
    }

    fun add(run: WidgetRun) {
        mRuns.add(run)
        mLastRun = run
    }

    private fun traverseStart(node: DependencyNode?, startPosition: Long): Long {
        val run = node!!.mRun
        if (run is HelperReferences) {
            return startPosition
        }
        var position = startPosition

        // first, compute stuff dependent on this node.
        val count = node.mDependencies.size
        for (i in 0 until count) {
            val dependency = node.mDependencies[i]
            if (dependency is DependencyNode) {
                val nextNode = dependency
                if (nextNode.mRun === run) {
                    // skip our own sibling node
                    continue
                }
                position = max(
                    position,
                    traverseStart(nextNode, startPosition + nextNode.mMargin)
                )
            }
        }
        if (node === run.start) {
            // let's go for our sibling
            val dimension: Long = run.wrapDimension
            position = max(position, traverseStart(run.end, startPosition + dimension))
            position = max(position, startPosition + dimension - run.end.mMargin)
        }
        return position
    }

    private fun traverseEnd(node: DependencyNode?, startPosition: Long): Long {
        val run = node!!.mRun
        if (run is HelperReferences) {
            return startPosition
        }
        var position = startPosition

        // first, compute stuff dependent on this node.
        val count = node.mDependencies.size
        for (i in 0 until count) {
            val dependency = node.mDependencies[i]
            if (dependency is DependencyNode) {
                val nextNode = dependency
                if (nextNode.mRun === run) {
                    // skip our own sibling node
                    continue
                }
                position = min(
                    position,
                    traverseEnd(nextNode, startPosition + nextNode.mMargin)
                )
            }
        }
        if (node === run.end) {
            // let's go for our sibling
            val dimension: Long = run.wrapDimension
            position = min(position, traverseEnd(run.start, startPosition - dimension))
            position = min(position, startPosition - dimension - run.start.mMargin)
        }
        return position
    }

    fun computeWrapSize(container: ConstraintWidgetContainer, orientation: Int): Long {
        if (mFirstRun is ChainRun) {
            val chainRun = mFirstRun as ChainRun
            if (chainRun.orientation != orientation) {
                return 0
            }
        } else {
            if (orientation == ConstraintWidget.HORIZONTAL) {
                if (mFirstRun !is HorizontalWidgetRun) {
                    return 0
                }
            } else {
                if (mFirstRun !is VerticalWidgetRun) {
                    return 0
                }
            }
        }
        val containerStart: DependencyNode =
            if (orientation == ConstraintWidget.HORIZONTAL) container.mHorizontalRun!!.start else container.mVerticalRun!!.start
        val containerEnd: DependencyNode =
            if (orientation == ConstraintWidget.HORIZONTAL) container.mHorizontalRun!!.end else container.mVerticalRun!!.end
        val runWithStartTarget: Boolean = mFirstRun!!.start.mTargets.contains(containerStart)
        val runWithEndTarget: Boolean = mFirstRun!!.end.mTargets.contains(containerEnd)
        var dimension = mFirstRun!!.wrapDimension
        if (runWithStartTarget && runWithEndTarget) {
            val maxPosition = traverseStart(mFirstRun!!.start, 0)
            val minPosition = traverseEnd(mFirstRun!!.end, 0)

            // to compute the gaps, we subtract the margins
            var endGap = maxPosition - dimension
            if (endGap >= -mFirstRun!!.end.mMargin) {
                endGap += mFirstRun!!.end.mMargin.toLong()
            }
            var startGap: Long = -minPosition - dimension - mFirstRun!!.start.mMargin
            if (startGap >= mFirstRun!!.start.mMargin) {
                startGap -= mFirstRun!!.start.mMargin.toLong()
            }
            val bias: Float = mFirstRun!!.mWidget.getBiasPercent(orientation)
            var gap: Long = 0
            if (bias > 0) {
                gap = (startGap / bias + endGap / (1f - bias)).toLong()
            }
            startGap = (0.5f + gap * bias).toLong()
            endGap = (0.5f + gap * (1f - bias)).toLong()
            val runDimension = startGap + dimension + endGap
            dimension = mFirstRun!!.start.mMargin + runDimension - mFirstRun!!.end.mMargin
        } else if (runWithStartTarget) {
            val maxPosition = traverseStart(mFirstRun!!.start, mFirstRun!!.start.mMargin.toLong())
            val runDimension: Long = mFirstRun!!.start.mMargin + dimension
            dimension = max(maxPosition, runDimension)
        } else if (runWithEndTarget) {
            val minPosition = traverseEnd(mFirstRun!!.end, mFirstRun!!.end.mMargin.toLong())
            val runDimension: Long = -mFirstRun!!.end.mMargin + dimension
            dimension = max(-minPosition, runDimension)
        } else {
            dimension = mFirstRun!!.start.mMargin.toLong() + mFirstRun!!.wrapDimension - mFirstRun!!.end.mMargin
        }
        return dimension
    }

    private fun defineTerminalWidget(run: WidgetRun?, orientation: Int): Boolean {
        if (!run!!.mWidget.isTerminalWidget.get(orientation)) {
            return false
        }
        for (dependency in run.start.mDependencies) {
            if (dependency is DependencyNode) {
                if (dependency.mRun === run) {
                    continue
                }
                if (dependency === dependency.mRun.start) {
                    if (run is ChainRun) {
                        for (widgetChainRun in run.mWidgets) {
                            defineTerminalWidget(widgetChainRun, orientation)
                        }
                    } else {
                        if (run !is HelperReferences) {
                            run.mWidget.isTerminalWidget[orientation] = false
                        }
                    }
                    defineTerminalWidget(dependency.mRun, orientation)
                }
            }
        }
        for (dependency in run.end.mDependencies) {
            if (dependency is DependencyNode) {
                if (dependency.mRun === run) {
                    continue
                }
                if (dependency === dependency.mRun.start) {
                    if (run is ChainRun) {
                        for (widgetChainRun in run.mWidgets) {
                            defineTerminalWidget(widgetChainRun, orientation)
                        }
                    } else {
                        if (run !is HelperReferences) {
                            run.mWidget.isTerminalWidget[orientation] = false
                        }
                    }
                    defineTerminalWidget(dependency.mRun, orientation)
                }
            }
        }
        return false
    }

    fun defineTerminalWidgets(horizontalCheck: Boolean, verticalCheck: Boolean) {
        if (horizontalCheck && mFirstRun is HorizontalWidgetRun) {
            defineTerminalWidget(mFirstRun, ConstraintWidget.HORIZONTAL)
        }
        if (verticalCheck && mFirstRun is VerticalWidgetRun) {
            defineTerminalWidget(mFirstRun, ConstraintWidget.VERTICAL)
        }
    }

    @ThreadLocal
    companion object {
        const val START = 0
        const val END = 1
        const val BASELINE = 2
        var index = 0
    }
}
