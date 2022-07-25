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

import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour

/**
 * Class to represent a chain by its main elements.
 */
class ChainHead(var first: ConstraintWidget, private val mOrientation: Int, isRtl: Boolean) {
    var firstVisibleWidget: ConstraintWidget? = null
    var last: ConstraintWidget? = null
    var lastVisibleWidget: ConstraintWidget? = null
    var head: ConstraintWidget? = null
    var firstMatchConstraintWidget: ConstraintWidget? = null
    var lastMatchConstraintWidget: ConstraintWidget? = null
    var mWeightedMatchConstraintsWidgets: java.util.ArrayList<ConstraintWidget>? = null
    var mWidgetsCount = 0
    var mWidgetsMatchCount = 0
    var totalWeight = 0f
    var mVisibleWidgets = 0
    var mTotalSize = 0
    var mTotalMargins = 0
    var mOptimizable = false
    private var mIsRtl = false
    var mHasUndefinedWeights = false
    protected var mHasDefinedWeights = false
    var mHasComplexMatchWeights = false
    protected var mHasRatio = false
    private var mDefined = false

    /**
     * Initialize variables, then determine visible widgets, the head of chain and
     * matched constraint widgets.
     *
     * @param first       first widget in a chain
     * @param orientation orientation of the chain (either Horizontal or Vertical)
     * @param isRtl       Right-to-left layout flag to determine the actual head of the chain
     */
    init {
        mIsRtl = isRtl
    }

    private fun defineChainProperties() {
        val offset = mOrientation * 2
        var lastVisited = first
        mOptimizable = true

        // TraverseChain
        var widget = first
        var next: ConstraintWidget? = first
        var done = false
        while (!done) {
            mWidgetsCount++
            widget.mNextChainWidget[mOrientation] = null
            widget.mListNextMatchConstraintsWidget[mOrientation] = null
            if (widget.visibility != ConstraintWidget.Companion.GONE) {
                mVisibleWidgets++
                if (widget.getDimensionBehaviour(mOrientation)
                    != DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    mTotalSize += widget.getLength(mOrientation)
                }
                mTotalSize += widget.mListAnchors[offset].getMargin()
                mTotalSize += widget.mListAnchors[offset + 1].getMargin()
                mTotalMargins += widget.mListAnchors[offset].getMargin()
                mTotalMargins += widget.mListAnchors[offset + 1].getMargin()
                // Visible widgets linked list.
                if (firstVisibleWidget == null) {
                    firstVisibleWidget = widget
                }
                lastVisibleWidget = widget

                // Match constraint linked list.
                if (widget.mListDimensionBehaviors[mOrientation]
                    == DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    if ((widget.mResolvedMatchConstraintDefault[mOrientation]
                                == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) || (widget.mResolvedMatchConstraintDefault[mOrientation]
                                == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO) || (widget.mResolvedMatchConstraintDefault[mOrientation]
                                == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT)
                    ) {
                        mWidgetsMatchCount++
                        // Note: Might cause an issue if we support MATCH_CONSTRAINT_RATIO_RESOLVED
                        // in chain optimization. (we currently don't)
                        val weight = widget.mWeight[mOrientation]
                        if (weight > 0) {
                            totalWeight += widget.mWeight[mOrientation]
                        }
                        if (isMatchConstraintEqualityCandidate(widget, mOrientation)) {
                            if (weight < 0) {
                                mHasUndefinedWeights = true
                            } else {
                                mHasDefinedWeights = true
                            }
                            if (mWeightedMatchConstraintsWidgets == null) {
                                mWeightedMatchConstraintsWidgets = java.util.ArrayList<ConstraintWidget>()
                            }
                            mWeightedMatchConstraintsWidgets.add(widget)
                        }
                        if (firstMatchConstraintWidget == null) {
                            firstMatchConstraintWidget = widget
                        }
                        if (lastMatchConstraintWidget != null) {
                            lastMatchConstraintWidget!!.mListNextMatchConstraintsWidget[mOrientation] = widget
                        }
                        lastMatchConstraintWidget = widget
                    }
                    if (mOrientation == ConstraintWidget.Companion.HORIZONTAL) {
                        if (widget.mMatchConstraintDefaultWidth
                            != ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                        ) {
                            mOptimizable = false
                        } else if (widget.mMatchConstraintMinWidth != 0
                            || widget.mMatchConstraintMaxWidth != 0
                        ) {
                            mOptimizable = false
                        }
                    } else {
                        if (widget.mMatchConstraintDefaultHeight
                            != ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                        ) {
                            mOptimizable = false
                        } else if (widget.mMatchConstraintMinHeight != 0
                            || widget.mMatchConstraintMaxHeight != 0
                        ) {
                            mOptimizable = false
                        }
                    }
                    if (widget.mDimensionRatio != 0.0f) {
                        //TODO: Improve (Could use ratio optimization).
                        mOptimizable = false
                        mHasRatio = true
                    }
                }
            }
            if (lastVisited !== widget) {
                lastVisited.mNextChainWidget[mOrientation] = widget
            }
            lastVisited = widget

            // go to the next widget
            val nextAnchor: ConstraintAnchor = widget.mListAnchors[offset + 1].mTarget
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
        if (firstVisibleWidget != null) {
            mTotalSize -= firstVisibleWidget!!.mListAnchors[offset].getMargin()
        }
        if (lastVisibleWidget != null) {
            mTotalSize -= lastVisibleWidget!!.mListAnchors[offset + 1].getMargin()
        }
        last = widget
        if (mOrientation == ConstraintWidget.Companion.HORIZONTAL && mIsRtl) {
            head = last
        } else {
            head = first
        }
        mHasComplexMatchWeights = mHasDefinedWeights && mHasUndefinedWeights
    }

    /**
     * @TODO: add description
     */
    fun define() {
        if (!mDefined) {
            defineChainProperties()
        }
        mDefined = true
    }

    companion object {
        /**
         * Returns true if the widget should be part of the match equality rules in the chain
         *
         * @param widget      the widget to test
         * @param orientation current orientation, HORIZONTAL or VERTICAL
         */
        private fun isMatchConstraintEqualityCandidate(
            widget: ConstraintWidget,
            orientation: Int
        ): Boolean {
            return widget.visibility != ConstraintWidget.Companion.GONE && (widget.mListDimensionBehaviors[orientation]
                    == DimensionBehaviour.MATCH_CONSTRAINT) && (widget.mResolvedMatchConstraintDefault[orientation] == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                    || widget.mResolvedMatchConstraintDefault[orientation] == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO)
        }
    }
}
