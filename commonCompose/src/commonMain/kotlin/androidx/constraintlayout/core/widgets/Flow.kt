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
package androidx.constraintlayout.core.widgets

import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.core.widgets.analyzer.BasicMeasure

/**
 * Implements the Flow virtual layout.
 */
class Flow : VirtualLayout() {
    private var mHorizontalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    private var mVerticalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    private var mFirstHorizontalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    private var mFirstVerticalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    private var mLastHorizontalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    private var mLastVerticalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    private var mHorizontalBias = 0.5f
    private var mVerticalBias = 0.5f
    private var mFirstHorizontalBias = 0.5f
    private var mFirstVerticalBias = 0.5f
    private var mLastHorizontalBias = 0.5f
    private var mLastVerticalBias = 0.5f
    private var mHorizontalGap = 0
    private var mVerticalGap = 0
    private var mHorizontalAlign = HORIZONTAL_ALIGN_CENTER
    private var mVerticalAlign = VERTICAL_ALIGN_CENTER
    private var mWrapMode = WRAP_NONE
    private var mMaxElementsWrap: Int = ConstraintWidget.Companion.UNKNOWN
    private var mOrientation: Int = ConstraintWidget.Companion.HORIZONTAL
    private val mChainList: java.util.ArrayList<WidgetsList> = java.util.ArrayList<WidgetsList>()

    // Aligned management
    private var mAlignedBiggestElementsInRows: Array<ConstraintWidget?>? = null
    private var mAlignedBiggestElementsInCols: Array<ConstraintWidget?>? = null
    private var mAlignedDimensions: IntArray? = null
    private var mDisplayedWidgets: Array<ConstraintWidget?>
    private var mDisplayedWidgetsCount = 0
    override fun copy(src: ConstraintWidget, map: java.util.HashMap<ConstraintWidget?, ConstraintWidget?>) {
        super.copy(src, map)
        val srcFLow = src as Flow
        mHorizontalStyle = srcFLow.mHorizontalStyle
        mVerticalStyle = srcFLow.mVerticalStyle
        mFirstHorizontalStyle = srcFLow.mFirstHorizontalStyle
        mFirstVerticalStyle = srcFLow.mFirstVerticalStyle
        mLastHorizontalStyle = srcFLow.mLastHorizontalStyle
        mLastVerticalStyle = srcFLow.mLastVerticalStyle
        mHorizontalBias = srcFLow.mHorizontalBias
        mVerticalBias = srcFLow.mVerticalBias
        mFirstHorizontalBias = srcFLow.mFirstHorizontalBias
        mFirstVerticalBias = srcFLow.mFirstVerticalBias
        mLastHorizontalBias = srcFLow.mLastHorizontalBias
        mLastVerticalBias = srcFLow.mLastVerticalBias
        mHorizontalGap = srcFLow.mHorizontalGap
        mVerticalGap = srcFLow.mVerticalGap
        mHorizontalAlign = srcFLow.mHorizontalAlign
        mVerticalAlign = srcFLow.mVerticalAlign
        mWrapMode = srcFLow.mWrapMode
        mMaxElementsWrap = srcFLow.mMaxElementsWrap
        mOrientation = srcFLow.mOrientation
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Accessors
    /////////////////////////////////////////////////////////////////////////////////////////////
    fun setOrientation(value: Int) {
        mOrientation = value
    }

    fun setFirstHorizontalStyle(value: Int) {
        mFirstHorizontalStyle = value
    }

    fun setFirstVerticalStyle(value: Int) {
        mFirstVerticalStyle = value
    }

    fun setLastHorizontalStyle(value: Int) {
        mLastHorizontalStyle = value
    }

    fun setLastVerticalStyle(value: Int) {
        mLastVerticalStyle = value
    }

    fun setHorizontalStyle(value: Int) {
        mHorizontalStyle = value
    }

    fun setVerticalStyle(value: Int) {
        mVerticalStyle = value
    }

    fun setHorizontalBias(value: Float) {
        mHorizontalBias = value
    }

    fun setVerticalBias(value: Float) {
        mVerticalBias = value
    }

    fun setFirstHorizontalBias(value: Float) {
        mFirstHorizontalBias = value
    }

    fun setFirstVerticalBias(value: Float) {
        mFirstVerticalBias = value
    }

    fun setLastHorizontalBias(value: Float) {
        mLastHorizontalBias = value
    }

    fun setLastVerticalBias(value: Float) {
        mLastVerticalBias = value
    }

    fun setHorizontalAlign(value: Int) {
        mHorizontalAlign = value
    }

    fun setVerticalAlign(value: Int) {
        mVerticalAlign = value
    }

    fun setWrapMode(value: Int) {
        mWrapMode = value
    }

    fun setHorizontalGap(value: Int) {
        mHorizontalGap = value
    }

    fun setVerticalGap(value: Int) {
        mVerticalGap = value
    }

    fun setMaxElementsWrap(value: Int) {
        mMaxElementsWrap = value
    }

    val maxElementsWrap: Float
        get() = mMaxElementsWrap.toFloat()

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Utility methods
    /////////////////////////////////////////////////////////////////////////////////////////////
    private fun getWidgetWidth(widget: ConstraintWidget?, max: Int): Int {
        if (widget == null) {
            return 0
        }
        if (widget.horizontalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
            if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) {
                return 0
            } else if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT) {
                val value = (widget.mMatchConstraintPercentWidth * max).toInt()
                if (value != widget.width) {
                    widget.isMeasureRequested = true
                    measure(
                        widget, DimensionBehaviour.FIXED, value,
                        widget.verticalDimensionBehaviour, widget.height
                    )
                }
                return value
            } else if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP) {
                return widget.width
            } else if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO) {
                return (widget.height * widget.mDimensionRatio + 0.5f).toInt()
            }
        }
        return widget.width
    }

    private fun getWidgetHeight(widget: ConstraintWidget?, max: Int): Int {
        if (widget == null) {
            return 0
        }
        if (widget.verticalDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT) {
            if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) {
                return 0
            } else if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT) {
                val value = (widget.mMatchConstraintPercentHeight * max).toInt()
                if (value != widget.height) {
                    widget.isMeasureRequested = true
                    measure(
                        widget, widget.horizontalDimensionBehaviour,
                        widget.width, DimensionBehaviour.FIXED, value
                    )
                }
                return value
            } else if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP) {
                return widget.height
            } else if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO) {
                return (widget.width * widget.mDimensionRatio + 0.5f).toInt()
            }
        }
        return widget.height
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    // Measure
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @TODO: add description
     */
    override fun measure(widthMode: Int, widthSize: Int, heightMode: Int, heightSize: Int) {
        if (mWidgetsCount > 0 && !measureChildren()) {
            setMeasure(0, 0)
            needsCallbackFromSolver(false)
            return
        }
        var width = 0
        var height = 0
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val measured = IntArray(2)
        var max = widthSize - paddingLeft - paddingRight
        if (mOrientation == ConstraintWidget.Companion.VERTICAL) {
            max = heightSize - paddingTop - paddingBottom
        }
        if (mOrientation == ConstraintWidget.Companion.HORIZONTAL) {
            if (mHorizontalStyle == ConstraintWidget.Companion.UNKNOWN) {
                mHorizontalStyle = ConstraintWidget.Companion.CHAIN_SPREAD
            }
            if (mVerticalStyle == ConstraintWidget.Companion.UNKNOWN) {
                mVerticalStyle = ConstraintWidget.Companion.CHAIN_SPREAD
            }
        } else {
            if (mHorizontalStyle == ConstraintWidget.Companion.UNKNOWN) {
                mHorizontalStyle = ConstraintWidget.Companion.CHAIN_SPREAD
            }
            if (mVerticalStyle == ConstraintWidget.Companion.UNKNOWN) {
                mVerticalStyle = ConstraintWidget.Companion.CHAIN_SPREAD
            }
        }
        var widgets: Array<ConstraintWidget?> = mWidgets
        var gone = 0
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget = mWidgets.get(i)!!
            if (widget.visibility == ConstraintWidget.Companion.GONE) {
                gone++
            }
        }
        var count: Int = mWidgetsCount
        if (gone > 0) {
            widgets = arrayOfNulls(mWidgetsCount - gone)
            var j = 0
            for (i in 0 until mWidgetsCount) {
                val widget: ConstraintWidget = mWidgets.get(i)!!
                if (widget.visibility != ConstraintWidget.Companion.GONE) {
                    widgets[j] = widget
                    j++
                }
            }
            count = j
        }
        mDisplayedWidgets = widgets
        mDisplayedWidgetsCount = count
        when (mWrapMode) {
            WRAP_ALIGNED -> {
                measureAligned(widgets, count, mOrientation, max, measured)
            }

            WRAP_CHAIN -> {
                measureChainWrap(widgets, count, mOrientation, max, measured)
            }

            WRAP_NONE -> {
                measureNoWrap(widgets, count, mOrientation, max, measured)
            }

            WRAP_CHAIN_NEW -> {
                measureChainWrap_new(widgets, count, mOrientation, max, measured)
            }
        }
        width = measured[ConstraintWidget.Companion.HORIZONTAL] + paddingLeft + paddingRight
        height = measured[ConstraintWidget.Companion.VERTICAL] + paddingTop + paddingBottom
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

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Utility class representing a single chain
    /////////////////////////////////////////////////////////////////////////////////////////////
    private inner class WidgetsList internal constructor(
        orientation: Int,
        left: ConstraintAnchor?, top: ConstraintAnchor?,
        right: ConstraintAnchor?, bottom: ConstraintAnchor?,
        max: Int
    ) {
        private var mOrientation: Int = ConstraintWidget.Companion.HORIZONTAL
        var mBiggest: ConstraintWidget? = null
        var mBiggestDimension = 0
        private var mLeft: ConstraintAnchor?
        private var mTop: ConstraintAnchor?
        private var mRight: ConstraintAnchor?
        private var mBottom: ConstraintAnchor?
        private var mPaddingLeft = 0
        private var mPaddingTop = 0
        private var mPaddingRight = 0
        private var mPaddingBottom = 0
        private var mWidth = 0
        private var mHeight = 0
        private var mStartIndex = 0
        private var mCount = 0
        private var mNbMatchConstraintsWidgets = 0
        private var mMax = 0

        init {
            mOrientation = orientation
            mLeft = left
            mTop = top
            mRight = right
            mBottom = bottom
            mPaddingLeft = paddingLeft
            mPaddingTop = paddingTop
            mPaddingRight = paddingRight
            mPaddingBottom = paddingBottom
            mMax = max
        }

        fun setup(
            orientation: Int, left: ConstraintAnchor?, top: ConstraintAnchor?,
            right: ConstraintAnchor?, bottom: ConstraintAnchor?,
            paddingLeft: Int, paddingTop: Int, paddingRight: Int, paddingBottom: Int,
            max: Int
        ) {
            mOrientation = orientation
            mLeft = left
            mTop = top
            mRight = right
            mBottom = bottom
            mPaddingLeft = paddingLeft
            mPaddingTop = paddingTop
            mPaddingRight = paddingRight
            mPaddingBottom = paddingBottom
            mMax = max
        }

        fun clear() {
            mBiggestDimension = 0
            mBiggest = null
            mWidth = 0
            mHeight = 0
            mStartIndex = 0
            mCount = 0
            mNbMatchConstraintsWidgets = 0
        }

        fun setStartIndex(value: Int) {
            mStartIndex = value
        }

        val width: Int
            get() = if (mOrientation == ConstraintWidget.Companion.HORIZONTAL) {
                mWidth - mHorizontalGap
            } else mWidth
        val height: Int
            get() = if (mOrientation == ConstraintWidget.Companion.VERTICAL) {
                mHeight - mVerticalGap
            } else mHeight

        fun add(widget: ConstraintWidget?) {
            if (mOrientation == ConstraintWidget.Companion.HORIZONTAL) {
                var width = getWidgetWidth(widget, mMax)
                if (widget.getHorizontalDimensionBehaviour()
                    == DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    mNbMatchConstraintsWidgets++
                    width = 0
                }
                var gap = mHorizontalGap
                if (widget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    gap = 0
                }
                mWidth += width + gap
                val height = getWidgetHeight(widget, mMax)
                if (mBiggest == null || mBiggestDimension < height) {
                    mBiggest = widget
                    mBiggestDimension = height
                    mHeight = height
                }
            } else {
                val width = getWidgetWidth(widget, mMax)
                var height = getWidgetHeight(widget, mMax)
                if (widget.getVerticalDimensionBehaviour() == DimensionBehaviour.MATCH_CONSTRAINT) {
                    mNbMatchConstraintsWidgets++
                    height = 0
                }
                var gap = mVerticalGap
                if (widget.getVisibility() == ConstraintWidget.Companion.GONE) {
                    gap = 0
                }
                mHeight += height + gap
                if (mBiggest == null || mBiggestDimension < width) {
                    mBiggest = widget
                    mBiggestDimension = width
                    mWidth = width
                }
            }
            mCount++
        }

        fun createConstraints(isInRtl: Boolean, chainIndex: Int, isLastChain: Boolean) {
            val count = mCount
            for (i in 0 until count) {
                if (mStartIndex + i >= mDisplayedWidgetsCount) {
                    break
                }
                val widget = mDisplayedWidgets[mStartIndex + i]
                widget?.resetAnchors()
            }
            if (count == 0 || mBiggest == null) {
                return
            }
            val singleChain = isLastChain && chainIndex == 0
            var firstVisible = -1
            var lastVisible = -1
            for (i in 0 until count) {
                var index = i
                if (isInRtl) {
                    index = count - 1 - i
                }
                if (mStartIndex + index >= mDisplayedWidgetsCount) {
                    break
                }
                val widget = mDisplayedWidgets[mStartIndex + index]
                if (widget != null && widget.visibility == ConstraintWidget.Companion.VISIBLE) {
                    if (firstVisible == -1) {
                        firstVisible = i
                    }
                    lastVisible = i
                }
            }
            var previous: ConstraintWidget? = null
            if (mOrientation == ConstraintWidget.Companion.HORIZONTAL) {
                val verticalWidget: ConstraintWidget = mBiggest
                verticalWidget.verticalChainStyle = mVerticalStyle
                var padding = mPaddingTop
                if (chainIndex > 0) {
                    padding += mVerticalGap
                }
                verticalWidget.mTop.connect(mTop, padding)
                if (isLastChain) {
                    verticalWidget.mBottom.connect(mBottom, mPaddingBottom)
                }
                if (chainIndex > 0) {
                    val bottom: ConstraintAnchor = mTop.mOwner.mBottom
                    bottom.connect(verticalWidget.mTop, 0)
                }
                var baselineVerticalWidget: ConstraintWidget? = verticalWidget
                if (mVerticalAlign == VERTICAL_ALIGN_BASELINE && !verticalWidget.hasBaseline()) {
                    for (i in 0 until count) {
                        var index = i
                        if (isInRtl) {
                            index = count - 1 - i
                        }
                        if (mStartIndex + index >= mDisplayedWidgetsCount) {
                            break
                        }
                        val widget = mDisplayedWidgets[mStartIndex + index]
                        if (widget!!.hasBaseline()) {
                            baselineVerticalWidget = widget
                            break
                        }
                    }
                }
                for (i in 0 until count) {
                    var index = i
                    if (isInRtl) {
                        index = count - 1 - i
                    }
                    if (mStartIndex + index >= mDisplayedWidgetsCount) {
                        break
                    }
                    val widget = mDisplayedWidgets[mStartIndex + index] ?: continue
                    if (i == 0) {
                        widget.connect(widget.mLeft, mLeft, mPaddingLeft)
                    }

                    // ChainHead is always based on index, not i.
                    // E.g. RTL would have head at the right most widget.
                    if (index == 0) {
                        var style = mHorizontalStyle
                        var bias = if (isInRtl) 1 - mHorizontalBias else mHorizontalBias
                        if (mStartIndex == 0 && mFirstHorizontalStyle != ConstraintWidget.Companion.UNKNOWN) {
                            style = mFirstHorizontalStyle
                            bias = if (isInRtl) 1 - mFirstHorizontalBias else mFirstHorizontalBias
                        } else if (isLastChain && mLastHorizontalStyle != ConstraintWidget.Companion.UNKNOWN) {
                            style = mLastHorizontalStyle
                            bias = if (isInRtl) 1 - mLastHorizontalBias else mLastHorizontalBias
                        }
                        widget.horizontalChainStyle = style
                        widget.horizontalBiasPercent = bias
                    }
                    if (i == count - 1) {
                        widget.connect(widget.mRight, mRight, mPaddingRight)
                    }
                    if (previous != null) {
                        widget.mLeft!!.connect(previous.mRight, mHorizontalGap)
                        if (i == firstVisible) {
                            widget.mLeft!!.setGoneMargin(mPaddingLeft)
                        }
                        previous.mRight!!.connect(widget.mLeft, 0)
                        if (i == lastVisible + 1) {
                            previous.mRight!!.setGoneMargin(mPaddingRight)
                        }
                    }
                    if (widget !== verticalWidget) {
                        if (mVerticalAlign == VERTICAL_ALIGN_BASELINE && baselineVerticalWidget!!.hasBaseline() && widget !== baselineVerticalWidget && widget.hasBaseline()) {
                            widget.mBaseline!!.connect(baselineVerticalWidget.mBaseline, 0)
                        } else {
                            when (mVerticalAlign) {
                                VERTICAL_ALIGN_TOP -> {
                                    widget.mTop.connect(verticalWidget.mTop, 0)
                                }

                                VERTICAL_ALIGN_BOTTOM -> {
                                    widget.mBottom.connect(verticalWidget.mBottom, 0)
                                }

                                VERTICAL_ALIGN_CENTER -> {
                                    if (singleChain) {
                                        widget.mTop.connect(mTop, mPaddingTop)
                                        widget.mBottom.connect(mBottom, mPaddingBottom)
                                    } else {
                                        widget.mTop.connect(verticalWidget.mTop, 0)
                                        widget.mBottom.connect(verticalWidget.mBottom, 0)
                                    }
                                }

                                else -> {
                                    if (singleChain) {
                                        widget.mTop.connect(mTop, mPaddingTop)
                                        widget.mBottom.connect(mBottom, mPaddingBottom)
                                    } else {
                                        widget.mTop.connect(verticalWidget.mTop, 0)
                                        widget.mBottom.connect(verticalWidget.mBottom, 0)
                                    }
                                }
                            }
                        }
                    }
                    previous = widget
                }
            } else {
                val horizontalWidget: ConstraintWidget = mBiggest
                horizontalWidget.horizontalChainStyle = mHorizontalStyle
                var padding = mPaddingLeft
                if (chainIndex > 0) {
                    padding += mHorizontalGap
                }
                if (isInRtl) {
                    horizontalWidget.mRight!!.connect(mRight, padding)
                    if (isLastChain) {
                        horizontalWidget.mLeft!!.connect(mLeft, mPaddingRight)
                    }
                    if (chainIndex > 0) {
                        val left: ConstraintAnchor = mRight.mOwner.mLeft
                        left.connect(horizontalWidget.mRight, 0)
                    }
                } else {
                    horizontalWidget.mLeft!!.connect(mLeft, padding)
                    if (isLastChain) {
                        horizontalWidget.mRight!!.connect(mRight, mPaddingRight)
                    }
                    if (chainIndex > 0) {
                        val right: ConstraintAnchor = mLeft.mOwner.mRight
                        right.connect(horizontalWidget.mLeft, 0)
                    }
                }
                for (i in 0 until count) {
                    if (mStartIndex + i >= mDisplayedWidgetsCount) {
                        break
                    }
                    val widget = mDisplayedWidgets[mStartIndex + i] ?: continue
                    if (i == 0) {
                        widget.connect(widget.mTop, mTop, mPaddingTop)
                        var style = mVerticalStyle
                        var bias = mVerticalBias
                        if (mStartIndex == 0 && mFirstVerticalStyle != ConstraintWidget.Companion.UNKNOWN) {
                            style = mFirstVerticalStyle
                            bias = mFirstVerticalBias
                        } else if (isLastChain && mLastVerticalStyle != ConstraintWidget.Companion.UNKNOWN) {
                            style = mLastVerticalStyle
                            bias = mLastVerticalBias
                        }
                        widget.verticalChainStyle = style
                        widget.verticalBiasPercent = bias
                    }
                    if (i == count - 1) {
                        widget.connect(widget.mBottom, mBottom, mPaddingBottom)
                    }
                    if (previous != null) {
                        widget.mTop.connect(previous.mBottom, mVerticalGap)
                        if (i == firstVisible) {
                            widget.mTop.setGoneMargin(mPaddingTop)
                        }
                        previous.mBottom.connect(widget.mTop, 0)
                        if (i == lastVisible + 1) {
                            previous.mBottom.setGoneMargin(mPaddingBottom)
                        }
                    }
                    if (widget !== horizontalWidget) {
                        if (isInRtl) {
                            when (mHorizontalAlign) {
                                HORIZONTAL_ALIGN_START -> {
                                    widget.mRight!!.connect(horizontalWidget.mRight, 0)
                                }

                                HORIZONTAL_ALIGN_CENTER -> {
                                    widget.mLeft!!.connect(horizontalWidget.mLeft, 0)
                                    widget.mRight!!.connect(horizontalWidget.mRight, 0)
                                }

                                HORIZONTAL_ALIGN_END -> {
                                    widget.mLeft!!.connect(horizontalWidget.mLeft, 0)
                                }
                            }
                        } else {
                            when (mHorizontalAlign) {
                                HORIZONTAL_ALIGN_START -> {
                                    widget.mLeft!!.connect(horizontalWidget.mLeft, 0)
                                }

                                HORIZONTAL_ALIGN_CENTER -> {
                                    if (singleChain) {
                                        widget.mLeft!!.connect(mLeft, mPaddingLeft)
                                        widget.mRight!!.connect(mRight, mPaddingRight)
                                    } else {
                                        widget.mLeft!!.connect(horizontalWidget.mLeft, 0)
                                        widget.mRight!!.connect(horizontalWidget.mRight, 0)
                                    }
                                }

                                HORIZONTAL_ALIGN_END -> {
                                    widget.mRight!!.connect(horizontalWidget.mRight, 0)
                                }
                            }
                        }
                    }
                    previous = widget
                }
            }
        }

        fun measureMatchConstraints(availableSpace: Int) {
            if (mNbMatchConstraintsWidgets == 0) {
                return
            }
            val count = mCount

            // that's completely incorrect and only works for spread with no weights?
            val widgetSize = availableSpace / mNbMatchConstraintsWidgets
            for (i in 0 until count) {
                if (mStartIndex + i >= mDisplayedWidgetsCount) {
                    break
                }
                val widget = mDisplayedWidgets[mStartIndex + i]
                if (mOrientation == ConstraintWidget.Companion.HORIZONTAL) {
                    if (widget != null && widget.horizontalDimensionBehaviour
                        == DimensionBehaviour.MATCH_CONSTRAINT
                    ) {
                        if (widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) {
                            measure(
                                widget, DimensionBehaviour.FIXED, widgetSize,
                                widget.verticalDimensionBehaviour, widget.height
                            )
                        }
                    }
                } else {
                    if (widget != null && widget.verticalDimensionBehaviour
                        == DimensionBehaviour.MATCH_CONSTRAINT
                    ) {
                        if (widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD) {
                            measure(
                                widget, widget.horizontalDimensionBehaviour,
                                widget.width, DimensionBehaviour.FIXED, widgetSize
                            )
                        }
                    }
                }
            }
            recomputeDimensions()
        }

        private fun recomputeDimensions() {
            mWidth = 0
            mHeight = 0
            mBiggest = null
            mBiggestDimension = 0
            val count = mCount
            for (i in 0 until count) {
                if (mStartIndex + i >= mDisplayedWidgetsCount) {
                    break
                }
                val widget = mDisplayedWidgets[mStartIndex + i]
                if (mOrientation == ConstraintWidget.Companion.HORIZONTAL) {
                    val width = widget.getWidth()
                    var gap = mHorizontalGap
                    if (widget.getVisibility() == ConstraintWidget.Companion.GONE) {
                        gap = 0
                    }
                    mWidth += width + gap
                    val height = getWidgetHeight(widget, mMax)
                    if (mBiggest == null || mBiggestDimension < height) {
                        mBiggest = widget
                        mBiggestDimension = height
                        mHeight = height
                    }
                } else {
                    val width = getWidgetWidth(widget, mMax)
                    val height = getWidgetHeight(widget, mMax)
                    var gap = mVerticalGap
                    if (widget.getVisibility() == ConstraintWidget.Companion.GONE) {
                        gap = 0
                    }
                    mHeight += height + gap
                    if (mBiggest == null || mBiggestDimension < width) {
                        mBiggest = widget
                        mBiggestDimension = width
                        mWidth = width
                    }
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    // Measure Chain Wrap
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Measure the virtual layout using a list of chains for the children
     *
     * @param widgets     list of widgets
     * @param orientation the layout orientation (horizontal or vertical)
     * @param max         the maximum available space
     * @param measured    output parameters -- will contain the resulting measure
     */
    private fun measureChainWrap(
        widgets: Array<ConstraintWidget?>,
        count: Int,
        orientation: Int,
        max: Int,
        measured: IntArray
    ) {
        if (count == 0) {
            return
        }
        mChainList.clear()
        var list = WidgetsList(orientation, mLeft, mTop, mRight, mBottom, max)
        mChainList.add(list)
        var nbMatchConstraintsWidgets = 0
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            var width = 0
            for (i in 0 until count) {
                val widget = widgets[i]
                val w = getWidgetWidth(widget, max)
                if (widget.getHorizontalDimensionBehaviour()
                    == DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    nbMatchConstraintsWidgets++
                }
                var doWrap = ((width == max || width + mHorizontalGap + w > max)
                        && list.mBiggest != null)
                if (!doWrap && i > 0 && mMaxElementsWrap > 0 && i % mMaxElementsWrap == 0) {
                    doWrap = true
                }
                if (doWrap) {
                    width = w
                    list = WidgetsList(orientation, mLeft, mTop, mRight, mBottom, max)
                    list.setStartIndex(i)
                    mChainList.add(list)
                } else {
                    if (i > 0) {
                        width += mHorizontalGap + w
                    } else {
                        width = w
                    }
                }
                list.add(widget)
            }
        } else {
            var height = 0
            for (i in 0 until count) {
                val widget = widgets[i]
                val h = getWidgetHeight(widget, max)
                if (widget.getVerticalDimensionBehaviour() == DimensionBehaviour.MATCH_CONSTRAINT) {
                    nbMatchConstraintsWidgets++
                }
                var doWrap = ((height == max || height + mVerticalGap + h > max)
                        && list.mBiggest != null)
                if (!doWrap && i > 0 && mMaxElementsWrap > 0 && i % mMaxElementsWrap == 0) {
                    doWrap = true
                }
                if (doWrap) {
                    height = h
                    list = WidgetsList(orientation, mLeft, mTop, mRight, mBottom, max)
                    list.setStartIndex(i)
                    mChainList.add(list)
                } else {
                    if (i > 0) {
                        height += mVerticalGap + h
                    } else {
                        height = h
                    }
                }
                list.add(widget)
            }
        }
        val listCount: Int = mChainList.size
        var left: ConstraintAnchor? = mLeft
        var top: ConstraintAnchor? = mTop
        var right: ConstraintAnchor? = mRight
        var bottom: ConstraintAnchor? = mBottom
        var paddingLeft = paddingLeft
        var paddingTop = paddingTop
        var paddingRight = paddingRight
        var paddingBottom = paddingBottom
        var maxWidth = 0
        var maxHeight = 0
        val needInternalMeasure = (getHorizontalDimensionBehaviour() == DimensionBehaviour.WRAP_CONTENT
                || getVerticalDimensionBehaviour() == DimensionBehaviour.WRAP_CONTENT)
        if (nbMatchConstraintsWidgets > 0 && needInternalMeasure) {
            // we have to remeasure them.
            for (i in 0 until listCount) {
                val current: WidgetsList = mChainList.get(i)
                if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    current.measureMatchConstraints(max - current.getWidth())
                } else {
                    current.measureMatchConstraints(max - current.getHeight())
                }
            }
        }
        for (i in 0 until listCount) {
            val current: WidgetsList = mChainList.get(i)
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                if (i < listCount - 1) {
                    val next: WidgetsList = mChainList.get(i + 1)
                    bottom = next.mBiggest!!.mTop
                    paddingBottom = 0
                } else {
                    bottom = mBottom
                    paddingBottom = getPaddingBottom()
                }
                val currentBottom = current.mBiggest!!.mBottom
                current.setup(
                    orientation, left, top, right, bottom,
                    paddingLeft, paddingTop, paddingRight, paddingBottom, max
                )
                top = currentBottom
                paddingTop = 0
                maxWidth = java.lang.Math.max(maxWidth, current.getWidth())
                maxHeight += current.getHeight()
                if (i > 0) {
                    maxHeight += mVerticalGap
                }
            } else {
                if (i < listCount - 1) {
                    val next: WidgetsList = mChainList.get(i + 1)
                    right = next.mBiggest!!.mLeft
                    paddingRight = 0
                } else {
                    right = mRight
                    paddingRight = getPaddingRight()
                }
                val currentRight = current.mBiggest!!.mRight
                current.setup(
                    orientation, left, top, right, bottom,
                    paddingLeft, paddingTop, paddingRight, paddingBottom, max
                )
                left = currentRight
                paddingLeft = 0
                maxWidth += current.getWidth()
                maxHeight = java.lang.Math.max(maxHeight, current.getHeight())
                if (i > 0) {
                    maxWidth += mHorizontalGap
                }
            }
        }
        measured[ConstraintWidget.Companion.HORIZONTAL] = maxWidth
        measured[ConstraintWidget.Companion.VERTICAL] = maxHeight
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    // Measure Chain Wrap new
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Measure the virtual layout using a list of chains for the children in new "fixed way"
     *
     * @param widgets     list of widgets
     * @param orientation the layout orientation (horizontal or vertical)
     * @param max         the maximum available space
     * @param measured    output parameters -- will contain the resulting measure
     */
    private fun measureChainWrap_new(
        widgets: Array<ConstraintWidget?>,
        count: Int,
        orientation: Int,
        max: Int,
        measured: IntArray
    ) {
        if (count == 0) {
            return
        }
        mChainList.clear()
        var list = WidgetsList(orientation, mLeft, mTop, mRight, mBottom, max)
        mChainList.add(list)
        var nbMatchConstraintsWidgets = 0
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            var width = 0
            var col = 0
            for (i in 0 until count) {
                col++
                val widget = widgets[i]
                val w = getWidgetWidth(widget, max)
                if (widget.getHorizontalDimensionBehaviour()
                    == DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    nbMatchConstraintsWidgets++
                }
                var doWrap = ((width == max || width + mHorizontalGap + w > max)
                        && list.mBiggest != null)
                if (!doWrap && i > 0 && mMaxElementsWrap > 0 && col > mMaxElementsWrap) {
                    doWrap = true
                }
                if (doWrap) {
                    width = w
                    list = WidgetsList(orientation, mLeft, mTop, mRight, mBottom, max)
                    list.setStartIndex(i)
                    mChainList.add(list)
                } else {
                    col = 0
                    if (i > 0) {
                        width += mHorizontalGap + w
                    } else {
                        width = w
                    }
                }
                list.add(widget)
            }
        } else {
            var height = 0
            var row = 0
            for (i in 0 until count) {
                val widget = widgets[i]
                val h = getWidgetHeight(widget, max)
                if (widget.getVerticalDimensionBehaviour() == DimensionBehaviour.MATCH_CONSTRAINT) {
                    nbMatchConstraintsWidgets++
                }
                var doWrap = ((height == max || height + mVerticalGap + h > max)
                        && list.mBiggest != null)
                if (!doWrap && i > 0 && mMaxElementsWrap > 0 && row > mMaxElementsWrap) {
                    doWrap = true
                }
                if (doWrap) {
                    height = h
                    list = WidgetsList(orientation, mLeft, mTop, mRight, mBottom, max)
                    list.setStartIndex(i)
                    mChainList.add(list)
                } else {
                    row = 0
                    if (i > 0) {
                        height += mVerticalGap + h
                    } else {
                        height = h
                    }
                }
                list.add(widget)
            }
        }
        val listCount: Int = mChainList.size
        var left: ConstraintAnchor? = mLeft
        var top: ConstraintAnchor? = mTop
        var right: ConstraintAnchor? = mRight
        var bottom: ConstraintAnchor? = mBottom
        var paddingLeft = paddingLeft
        var paddingTop = paddingTop
        var paddingRight = paddingRight
        var paddingBottom = paddingBottom
        var maxWidth = 0
        var maxHeight = 0
        val needInternalMeasure = (getHorizontalDimensionBehaviour() == DimensionBehaviour.WRAP_CONTENT
                || getVerticalDimensionBehaviour() == DimensionBehaviour.WRAP_CONTENT)
        if (nbMatchConstraintsWidgets > 0 && needInternalMeasure) {
            // we have to remeasure them.
            for (i in 0 until listCount) {
                val current: WidgetsList = mChainList.get(i)
                if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    current.measureMatchConstraints(max - current.getWidth())
                } else {
                    current.measureMatchConstraints(max - current.getHeight())
                }
            }
        }
        for (i in 0 until listCount) {
            val current: WidgetsList = mChainList.get(i)
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                if (i < listCount - 1) {
                    val next: WidgetsList = mChainList.get(i + 1)
                    bottom = next.mBiggest!!.mTop
                    paddingBottom = 0
                } else {
                    bottom = mBottom
                    paddingBottom = getPaddingBottom()
                }
                val currentBottom = current.mBiggest!!.mBottom
                current.setup(
                    orientation, left, top, right, bottom,
                    paddingLeft, paddingTop, paddingRight, paddingBottom, max
                )
                top = currentBottom
                paddingTop = 0
                maxWidth = java.lang.Math.max(maxWidth, current.getWidth())
                maxHeight += current.getHeight()
                if (i > 0) {
                    maxHeight += mVerticalGap
                }
            } else {
                if (i < listCount - 1) {
                    val next: WidgetsList = mChainList.get(i + 1)
                    right = next.mBiggest!!.mLeft
                    paddingRight = 0
                } else {
                    right = mRight
                    paddingRight = getPaddingRight()
                }
                val currentRight = current.mBiggest!!.mRight
                current.setup(
                    orientation, left, top, right, bottom,
                    paddingLeft, paddingTop, paddingRight, paddingBottom, max
                )
                left = currentRight
                paddingLeft = 0
                maxWidth += current.getWidth()
                maxHeight = java.lang.Math.max(maxHeight, current.getHeight())
                if (i > 0) {
                    maxWidth += mHorizontalGap
                }
            }
        }
        measured[ConstraintWidget.Companion.HORIZONTAL] = maxWidth
        measured[ConstraintWidget.Companion.VERTICAL] = maxHeight
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    // Measure No Wrap
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Measure the virtual layout using a single chain for the children
     *
     * @param widgets     list of widgets
     * @param orientation the layout orientation (horizontal or vertical)
     * @param max         the maximum available space
     * @param measured    output parameters -- will contain the resulting measure
     */
    private fun measureNoWrap(
        widgets: Array<ConstraintWidget?>,
        count: Int,
        orientation: Int,
        max: Int,
        measured: IntArray
    ) {
        if (count == 0) {
            return
        }
        var list: WidgetsList? = null
        if (mChainList.size == 0) {
            list = WidgetsList(orientation, mLeft, mTop, mRight, mBottom, max)
            mChainList.add(list)
        } else {
            list = mChainList.get(0)
            list.clear()
            list.setup(
                orientation, mLeft, mTop, mRight, mBottom,
                paddingLeft, paddingTop, paddingRight, paddingBottom, max
            )
        }
        for (i in 0 until count) {
            val widget = widgets[i]
            list.add(widget)
        }
        measured[ConstraintWidget.Companion.HORIZONTAL] = list.getWidth()
        measured[ConstraintWidget.Companion.VERTICAL] = list.getHeight()
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    // Measure Aligned
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Measure the virtual layout arranging the children in a regular grid
     *
     * @param widgets     list of widgets
     * @param orientation the layout orientation (horizontal or vertical)
     * @param max         the maximum available space
     * @param measured    output parameters -- will contain the resulting measure
     */
    private fun measureAligned(
        widgets: Array<ConstraintWidget?>,
        count: Int,
        orientation: Int,
        max: Int,
        measured: IntArray
    ) {
        var done = false
        var rows = 0
        var cols = 0
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            cols = mMaxElementsWrap
            if (cols <= 0) {
                // let's initialize cols with an acceptable value
                var w = 0
                cols = 0
                for (i in 0 until count) {
                    if (i > 0) {
                        w += mHorizontalGap
                    }
                    val widget = widgets[i] ?: continue
                    w += getWidgetWidth(widget, max)
                    if (w > max) {
                        break
                    }
                    cols++
                }
            }
        } else {
            rows = mMaxElementsWrap
            if (rows <= 0) {
                // let's initialize rows with an acceptable value
                var h = 0
                rows = 0
                for (i in 0 until count) {
                    if (i > 0) {
                        h += mVerticalGap
                    }
                    val widget = widgets[i] ?: continue
                    h += getWidgetHeight(widget, max)
                    if (h > max) {
                        break
                    }
                    rows++
                }
            }
        }
        if (mAlignedDimensions == null) {
            mAlignedDimensions = IntArray(2)
        }
        if (rows == 0 && orientation == ConstraintWidget.Companion.VERTICAL || cols == 0 && orientation == ConstraintWidget.Companion.HORIZONTAL) {
            done = true
        }
        while (!done) {
            // get a num of rows (or cols)
            // get for each row and cols the chain of biggest elements
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                rows = java.lang.Math.ceil((count / cols.toFloat()).toDouble()).toInt()
            } else {
                cols = java.lang.Math.ceil((count / rows.toFloat()).toDouble()).toInt()
            }
            if (mAlignedBiggestElementsInCols == null
                || mAlignedBiggestElementsInCols!!.size < cols
            ) {
                mAlignedBiggestElementsInCols = arrayOfNulls(cols)
            } else {
                java.util.Arrays.fill(mAlignedBiggestElementsInCols, null)
            }
            if (mAlignedBiggestElementsInRows == null
                || mAlignedBiggestElementsInRows!!.size < rows
            ) {
                mAlignedBiggestElementsInRows = arrayOfNulls(rows)
            } else {
                java.util.Arrays.fill(mAlignedBiggestElementsInRows, null)
            }
            for (i in 0 until cols) {
                for (j in 0 until rows) {
                    var index = j * cols + i
                    if (orientation == ConstraintWidget.Companion.VERTICAL) {
                        index = i * rows + j
                    }
                    if (index >= widgets.size) {
                        continue
                    }
                    val widget = widgets[index] ?: continue
                    val w = getWidgetWidth(widget, max)
                    if (mAlignedBiggestElementsInCols!![i] == null
                        || mAlignedBiggestElementsInCols!![i].getWidth() < w
                    ) {
                        mAlignedBiggestElementsInCols!![i] = widget
                    }
                    val h = getWidgetHeight(widget, max)
                    if (mAlignedBiggestElementsInRows!![j] == null
                        || mAlignedBiggestElementsInRows!![j].getHeight() < h
                    ) {
                        mAlignedBiggestElementsInRows!![j] = widget
                    }
                }
            }
            var w = 0
            for (i in 0 until cols) {
                val widget = mAlignedBiggestElementsInCols!![i]
                if (widget != null) {
                    if (i > 0) {
                        w += mHorizontalGap
                    }
                    w += getWidgetWidth(widget, max)
                }
            }
            var h = 0
            for (j in 0 until rows) {
                val widget = mAlignedBiggestElementsInRows!![j]
                if (widget != null) {
                    if (j > 0) {
                        h += mVerticalGap
                    }
                    h += getWidgetHeight(widget, max)
                }
            }
            measured[ConstraintWidget.Companion.HORIZONTAL] = w
            measured[ConstraintWidget.Companion.VERTICAL] = h
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                if (w > max) {
                    if (cols > 1) {
                        cols--
                    } else {
                        done = true
                    }
                } else {
                    done = true
                }
            } else { // VERTICAL
                if (h > max) {
                    if (rows > 1) {
                        rows--
                    } else {
                        done = true
                    }
                } else {
                    done = true
                }
            }
        }
        mAlignedDimensions!![ConstraintWidget.Companion.HORIZONTAL] = cols
        mAlignedDimensions!![ConstraintWidget.Companion.VERTICAL] = rows
    }

    private fun createAlignedConstraints(isInRtl: Boolean) {
        if (mAlignedDimensions == null || mAlignedBiggestElementsInCols == null || mAlignedBiggestElementsInRows == null) {
            return
        }
        for (i in 0 until mDisplayedWidgetsCount) {
            val widget = mDisplayedWidgets[i]
            widget!!.resetAnchors()
        }
        val cols = mAlignedDimensions!![ConstraintWidget.Companion.HORIZONTAL]
        val rows = mAlignedDimensions!![ConstraintWidget.Companion.VERTICAL]
        var previous: ConstraintWidget? = null
        var horizontalBias = mHorizontalBias
        for (i in 0 until cols) {
            var index = i
            if (isInRtl) {
                index = cols - i - 1
                horizontalBias = 1 - mHorizontalBias
            }
            val widget = mAlignedBiggestElementsInCols!![index]
            if (widget == null || widget.visibility == ConstraintWidget.Companion.GONE) {
                continue
            }
            if (i == 0) {
                widget.connect(widget.mLeft, mLeft, paddingLeft)
                widget.horizontalChainStyle = mHorizontalStyle
                widget.horizontalBiasPercent = horizontalBias
            }
            if (i == cols - 1) {
                widget.connect(widget.mRight, mRight, paddingRight)
            }
            if (i > 0 && previous != null) {
                widget.connect(widget.mLeft, previous.mRight, mHorizontalGap)
                previous.connect(previous.mRight, widget.mLeft, 0)
            }
            previous = widget
        }
        for (j in 0 until rows) {
            val widget = mAlignedBiggestElementsInRows!![j]
            if (widget == null || widget.visibility == ConstraintWidget.Companion.GONE) {
                continue
            }
            if (j == 0) {
                widget.connect(widget.mTop, mTop, paddingTop)
                widget.verticalChainStyle = mVerticalStyle
                widget.verticalBiasPercent = mVerticalBias
            }
            if (j == rows - 1) {
                widget.connect(widget.mBottom, mBottom, paddingBottom)
            }
            if (j > 0 && previous != null) {
                widget.connect(widget.mTop, previous.mBottom, mVerticalGap)
                previous.connect(previous.mBottom, widget.mTop, 0)
            }
            previous = widget
        }
        for (i in 0 until cols) {
            for (j in 0 until rows) {
                var index = j * cols + i
                if (mOrientation == ConstraintWidget.Companion.VERTICAL) {
                    index = i * rows + j
                }
                if (index >= mDisplayedWidgets.size) {
                    continue
                }
                val widget = mDisplayedWidgets[index]
                if (widget == null || widget.visibility == ConstraintWidget.Companion.GONE) {
                    continue
                }
                val biggestInCol = mAlignedBiggestElementsInCols!![i]
                val biggestInRow = mAlignedBiggestElementsInRows!![j]
                if (widget !== biggestInCol) {
                    widget.connect(widget.mLeft, biggestInCol!!.mLeft, 0)
                    widget.connect(widget.mRight, biggestInCol.mRight, 0)
                }
                if (widget !== biggestInRow) {
                    widget.connect(widget.mTop, biggestInRow!!.mTop, 0)
                    widget.connect(widget.mBottom, biggestInRow.mBottom, 0)
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    // Add constraints to solver
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Add this widget to the solver
     *
     * @param system   the solver we want to add the widget to
     * @param optimize true if [Optimizer.OPTIMIZATION_GRAPH] is on
     */
    override fun addToSolver(system: LinearSystem?, optimize: Boolean) {
        super.addToSolver(system!!, optimize)
        val isInRtl = getParent() != null && (getParent() as ConstraintWidgetContainer).isRtl
        when (mWrapMode) {
            WRAP_CHAIN -> {
                val count: Int = mChainList.size
                var i = 0
                while (i < count) {
                    val list: WidgetsList = mChainList.get(i)
                    list.createConstraints(isInRtl, i, i == count - 1)
                    i++
                }
            }

            WRAP_NONE -> {
                if (mChainList.size > 0) {
                    val list: WidgetsList = mChainList.get(0)
                    list.createConstraints(isInRtl, 0, true)
                }
            }

            WRAP_ALIGNED -> {
                createAlignedConstraints(isInRtl)
            }

            WRAP_CHAIN_NEW -> {
                val count: Int = mChainList.size
                var i = 0
                while (i < count) {
                    val list: WidgetsList = mChainList.get(i)
                    list.createConstraints(isInRtl, i, i == count - 1)
                    i++
                }
            }
        }
        needsCallbackFromSolver(false)
    }

    companion object {
        const val HORIZONTAL_ALIGN_START = 0
        const val HORIZONTAL_ALIGN_END = 1
        const val HORIZONTAL_ALIGN_CENTER = 2
        const val VERTICAL_ALIGN_TOP = 0
        const val VERTICAL_ALIGN_BOTTOM = 1
        const val VERTICAL_ALIGN_CENTER = 2
        const val VERTICAL_ALIGN_BASELINE = 3
        const val WRAP_NONE = 0
        const val WRAP_CHAIN = 1
        const val WRAP_ALIGNED = 2
        const val WRAP_CHAIN_NEW = 3
    }
}
