/*
 * Copyright (C) 2015 The Android Open Source Project
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

import androidx.constraintlayout.core.*
import androidx.constraintlayout.core.motion.utils.*
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.widgets.analyzer.ChainRun
import androidx.constraintlayout.core.widgets.analyzer.HorizontalWidgetRun
import androidx.constraintlayout.core.widgets.analyzer.VerticalWidgetRun
import androidx.constraintlayout.core.widgets.analyzer.WidgetRun
import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.core.SolverVariable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Implements a constraint Widget model supporting constraints relations between other widgets.
 *
 *
 * The widget has various anchors (i.e. Left, Top, Right, Bottom, representing their respective
 * sides, as well as Baseline, Center_X and Center_Y). Connecting anchors from one widget to another
 * represents a constraint relation between the two anchors; the [LinearSystem] will then
 * be able to use this model to try to minimize the distances between connected anchors.
 *
 *
 *
 * If opposite anchors are connected (e.g. Left and Right anchors), if they have the same strength,
 * the widget will be equally pulled toward their respective target anchor positions; if the widget
 * has a fixed size, this means that the widget will be centered between the two target anchors. If
 * the widget's size is allowed to adjust, the size of the widget will change to be as large as
 * necessary so that the widget's anchors and the target anchors' distances are zero.
 *
 * Constraints are set by connecting a widget's anchor to another via the
 * [.connect] function.
 */
open class ConstraintWidget {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Graph measurements
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var measured = false
    var run = arrayOfNulls<WidgetRun>(2)
    var horizontalChainRun: ChainRun? = null
    var verticalChainRun: ChainRun? = null
    var mHorizontalRun: HorizontalWidgetRun? = null
    var mVerticalRun: VerticalWidgetRun? = null
    var isTerminalWidget = booleanArrayOf(true, true)
    var mResolvedHasRatio = false
    private var mMeasureRequested = true
    private val mOptimizeWrapO = false
    private val mOptimizeWrapOnResolved = true
    private var mWidthOverride = -1
    private var mHeightOverride = -1
    var frame = WidgetFrame(this)
    var stringId: String? = null

    /**
     * @TODO: add description
     */
    fun getRun(orientation: Int): WidgetRun? {
        if (orientation == HORIZONTAL) {
            return mHorizontalRun
        } else if (orientation == VERTICAL) {
            return mVerticalRun
        }
        return null
    }

    private var mResolvedHorizontal = false
    private var mResolvedVertical = false
    var isHorizontalSolvingPassDone = false
        private set
    var isVerticalSolvingPassDone = false
        private set

    /**
     * @TODO: add description
     */
    fun setFinalFrame(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        baseline: Int,
        orientation: Int
    ) {
        setFrame(left, top, right, bottom)
        baselineDistance = baseline
        if (orientation == HORIZONTAL) {
            mResolvedHorizontal = true
            mResolvedVertical = false
        } else if (orientation == VERTICAL) {
            mResolvedHorizontal = false
            mResolvedVertical = true
        } else if (orientation == BOTH) {
            mResolvedHorizontal = true
            mResolvedVertical = true
        } else {
            mResolvedHorizontal = false
            mResolvedVertical = false
        }
    }

    /**
     * @TODO: add description
     */
    fun setFinalLeft(x1: Int) {
        mLeft?.finalValue = (x1)
        mX = x1
    }

    /**
     * @TODO: add description
     */
    fun setFinalTop(y1: Int) {
        mTop.finalValue = y1
        mY = y1
    }

    /**
     * @TODO: add description
     */
    fun resetSolvingPassFlag() {
        isHorizontalSolvingPassDone = false
        isVerticalSolvingPassDone = false
    }

    /**
     * @TODO: add description
     */
    fun markHorizontalSolvingPassDone() {
        isHorizontalSolvingPassDone = true
    }

    /**
     * @TODO: add description
     */
    fun markVerticalSolvingPassDone() {
        isVerticalSolvingPassDone = true
    }

    /**
     * @TODO: add description
     */
    fun setFinalHorizontal(x1: Int, x2: Int) {
        if (mResolvedHorizontal) {
            return
        }
        mLeft?.finalValue = (x1)
        mRight?.finalValue = (x2)
        mX = x1
        mWidth = x2 - x1
        mResolvedHorizontal = true
        if (LinearSystem.FULL_DEBUG) {
            println(
                "*** SET FINAL HORIZONTAL FOR " + debugName
                        + " : " + x1 + " -> " + x2 + " (width: " + mWidth + ")"
            )
        }
    }

    /**
     * @TODO: add description
     */
    fun setFinalVertical(y1: Int, y2: Int) {
        if (mResolvedVertical) {
            return
        }
        mTop.finalValue = y1
        mBottom.finalValue = y2
        mY = y1
        mHeight = y2 - y1
        if (hasBaseline) {
            mBaseline?.finalValue = (y1 + mBaselineDistance)
        }
        mResolvedVertical = true
        if (LinearSystem.FULL_DEBUG) {
            println(
                "*** SET FINAL VERTICAL FOR " + debugName
                        + " : " + y1 + " -> " + y2 + " (height: " + mHeight + ")"
            )
        }
    }

    /**
     * @TODO: add description
     */
    fun setFinalBaseline(baselineValue: Int) {
        if (!hasBaseline) {
            return
        }
        val y1 = baselineValue - mBaselineDistance
        val y2 = y1 + mHeight
        mY = y1
        mTop.finalValue = y1
        mBottom.finalValue = y2
        mBaseline?.finalValue = (baselineValue)
        mResolvedVertical = true
    }

    open val isResolvedHorizontally: Boolean
        get() = mResolvedHorizontal || mLeft!!.hasFinalValue() && mRight!!.hasFinalValue()
    open val isResolvedVertically: Boolean
        get() = mResolvedVertical || mTop.hasFinalValue() && mBottom.hasFinalValue()

    /**
     * @TODO: add description
     */
    fun resetFinalResolution() {
        mResolvedHorizontal = false
        mResolvedVertical = false
        isHorizontalSolvingPassDone = false
        isVerticalSolvingPassDone = false
        var i = 0
        val mAnchorsSize: Int = mAnchors.size
        while (i < mAnchorsSize) {
            val anchor: ConstraintAnchor = mAnchors.get(i)
            anchor.resetFinalResolution()
            i++
        }
    }

    /**
     * @TODO: add description
     */
    fun ensureMeasureRequested() {
        mMeasureRequested = true
    }

    /**
     * @TODO: add description
     */
    fun hasDependencies(): Boolean {
        var i = 0
        val mAnchorsSize: Int = mAnchors.size
        while (i < mAnchorsSize) {
            val anchor: ConstraintAnchor = mAnchors.get(i)
            if (anchor.hasDependents()) {
                return true
            }
            i++
        }
        return false
    }

    /**
     * @TODO: add description
     */
    fun hasDanglingDimension(orientation: Int): Boolean {
        return if (orientation == HORIZONTAL) {
            val horizontalTargets = (if (mLeft?.target != null) 1 else 0) + if (mRight?.target != null) 1 else 0
            horizontalTargets < 2
        } else {
            val verticalTargets = ((if (mTop?.target != null) 1 else 0)
                    + (if (mBottom?.target != null) 1 else 0) + if (mBaseline?.target != null) 1 else 0)
            verticalTargets < 2
        }
    }

    /**
     * @TODO: add description
     */
    fun hasResolvedTargets(orientation: Int, size: Int): Boolean {
        if (orientation == HORIZONTAL) {
            if (mLeft?.target != null && mLeft?.target!!.hasFinalValue() && mRight?.target != null && mRight?.target!!.hasFinalValue()) {
                return (mRight?.target!!.finalValue - mRight?.margin!!
                        - (mLeft?.target!!.finalValue + mLeft?.margin!!)) >= size
            }
        } else {
            if (mTop?.target != null && mTop?.target!!.hasFinalValue() && mBottom?.target != null && mBottom?.target!!.hasFinalValue()) {
                return (mBottom?.target!!.finalValue - mBottom.margin
                        - (mTop?.target!!.finalValue + mTop.margin)) >= size
            }
        }
        return false
    }

    // Support for direct resolution
    var mHorizontalResolution = UNKNOWN
    var mVerticalResolution = UNKNOWN
    private var mWrapBehaviorInParent = WRAP_BEHAVIOR_INCLUDED
    var mMatchConstraintDefaultWidth = MATCH_CONSTRAINT_SPREAD
    var mMatchConstraintDefaultHeight = MATCH_CONSTRAINT_SPREAD
    var mResolvedMatchConstraintDefault = IntArray(2)
    var mMatchConstraintMinWidth = 0
    var mMatchConstraintMaxWidth = 0
    var mMatchConstraintPercentWidth = 1f
    var mMatchConstraintMinHeight = 0
    var mMatchConstraintMaxHeight = 0
    var mMatchConstraintPercentHeight = 1f
    /**
     * Returns true if width is set to wrap_content
     */
    /**
     * Keep track of wrap_content for width
     */
    var isWidthWrapContent = false
    /**
     * Returns true if height is set to wrap_content
     */
    /**
     * Keep track of wrap_content for height
     */
    var isHeightWrapContent = false
    var mResolvedDimensionRatioSide = UNKNOWN
    var mResolvedDimensionRatio = 1.0f
    private var mMaxDimension = intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE)
    var mCircleConstraintAngle = Float.NaN
    var hasBaseline = false
    var isInPlaceholder = false
    var isInVirtualLayout = false
    var maxHeight: Int
        get() = mMaxDimension[VERTICAL]
        set(maxHeight) {
            mMaxDimension[VERTICAL] = maxHeight
        }
    var maxWidth: Int
        get() = mMaxDimension[HORIZONTAL]
        set(maxWidth) {
            mMaxDimension[HORIZONTAL] = maxWidth
        }
    val isSpreadWidth: Boolean
        get() = mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_SPREAD && dimensionRatio == 0f && mMatchConstraintMinWidth == 0 && mMatchConstraintMaxWidth == 0 && mListDimensionBehaviors[HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT
    val isSpreadHeight: Boolean
        get() = mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_SPREAD && dimensionRatio == 0f && mMatchConstraintMinHeight == 0 && mMatchConstraintMaxHeight == 0 && mListDimensionBehaviors[VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT

    fun setInBarrier(orientation: Int, value: Boolean) {
        mIsInBarrier[orientation] = value
    }

    /**
     * @TODO: add description
     */
    fun isInBarrier(orientation: Int): Boolean {
        return mIsInBarrier[orientation]
    }

    var isMeasureRequested: Boolean
        get() = mMeasureRequested && visibility != GONE
        set(measureRequested) {
            mMeasureRequested = measureRequested
        }

    /**
     * @TODO: add description
     */
    var wrapBehaviorInParent: Int
        get() = mWrapBehaviorInParent
        set(behavior) {
            if (behavior >= 0 && behavior <= WRAP_BEHAVIOR_SKIPPED) {
                mWrapBehaviorInParent = behavior
            }
        }

    /**
     * Keep a cache of the last measure cache as we can bypass remeasures during the onMeasure...
     * the View's measure cache will only be reset in onLayout, so too late for us.
     */
    var lastHorizontalMeasureSpec = 0
        private set
    var lastVerticalMeasureSpec = 0
        private set

    /**
     * @TODO: add description
     */
    fun setLastMeasureSpec(horizontal: Int, vertical: Int) {
        lastHorizontalMeasureSpec = horizontal
        lastVerticalMeasureSpec = vertical
        isMeasureRequested = false
    }

    /**
     * Define how the widget will resize
     */
    enum class DimensionBehaviour {
        FIXED, WRAP_CONTENT, MATCH_CONSTRAINT, MATCH_PARENT
    }

    // The anchors available on the widget
    // note: all anchors should be added to the mAnchors array (see addAnchors())
    var mLeft: ConstraintAnchor = ConstraintAnchor(this, ConstraintAnchor.Type.LEFT)
    var mTop = ConstraintAnchor(this, ConstraintAnchor.Type.TOP)
    var mRight: ConstraintAnchor = ConstraintAnchor(this, ConstraintAnchor.Type.RIGHT)
    var mBottom = ConstraintAnchor(this, ConstraintAnchor.Type.BOTTOM)
    var mBaseline: ConstraintAnchor = ConstraintAnchor(this, ConstraintAnchor.Type.BASELINE)
    var mCenterX = ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_X)
    var mCenterY = ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_Y)
    var mCenter = ConstraintAnchor(this, ConstraintAnchor.Type.CENTER)
    var mListAnchors = arrayOf(mLeft, mRight, mTop, mBottom, mBaseline, mCenter)
    protected var mAnchors: ArrayList<ConstraintAnchor> = ArrayList<ConstraintAnchor>()
    private val mIsInBarrier = BooleanArray(2)
    var mListDimensionBehaviors = arrayOf<DimensionBehaviour?>(DimensionBehaviour.FIXED, DimensionBehaviour.FIXED)
    /**
     * Returns the parent of this widget if there is one
     *
     * @return parent
     */
    /**
     * Set the parent of this widget
     *
     * @param widget parent
     */
    // Parent of this widget
    var parent: ConstraintWidget? = null

    // Dimensions of the widget
    var mWidth = 0
    var mHeight = 0

    /**
     * Return the current ratio of this widget
     *
     * @return the dimension ratio (HORIZONTAL, VERTICAL, or UNKNOWN)
     */
    var dimensionRatio = 0f

    /**
     * Return the current side on which ratio will be applied
     *
     * @return HORIZONTAL, VERTICAL, or UNKNOWN
     */
    var dimensionRatioSide = UNKNOWN
        protected set

    // Origin of the widget
    var mX = 0
    var mY = 0
    var mRelX = 0
    var mRelY = 0

    // Root offset
    protected var mOffsetX = 0
    protected var mOffsetY = 0

    // Baseline distance relative to the top of the widget
    var mBaselineDistance = 0

    // Minimum sizes for the widget
    protected var mMinWidth = 0
    protected var mMinHeight = 0
    /**
     * Return the horizontal percentage bias that is used when two opposite connections
     * exist of the same strength.
     *
     * @return horizontal percentage bias
     */
    /**
     * Set the horizontal bias percent to apply when we have two opposite constraints of
     * equal strength
     *
     * @param horizontalBiasPercent the percentage used
     */
    var horizontalBiasPercent = DEFAULT_BIAS
    /**
     * Return the vertical percentage bias that is used when two opposite connections
     * exist of the same strength.
     *
     * @return vertical percentage bias
     */
    /**
     * Set the vertical bias percent to apply when we have two opposite constraints of
     * equal strength
     *
     * @param verticalBiasPercent the percentage used
     */
    var verticalBiasPercent = DEFAULT_BIAS
    /**
     * Return the companion widget. Typically, this would be the real
     * widget we represent with this instance of ConstraintWidget.
     *
     * @return the companion widget, if set.
     */
    /**
     * Set the companion widget. Typically, this would be the real widget we
     * represent with this instance of ConstraintWidget.
     */
    // The companion widget (typically, the real widget we represent)
    var companionWidget: Any? = null

    // This is used to possibly "skip" a position while inside a container. For example,
    // a container like Table can use this to implement empty cells
    // (the item positioned after the empty cell will have a skip value of 1)
    private var mContainerItemSkip = 0
    /**
     * Returns the current visibility value for this widget
     *
     * @return the visibility (VISIBLE, INVISIBLE, or GONE)
     */
    /**
     * Set the visibility for this widget
     *
     * @param visibility either VISIBLE, INVISIBLE, or GONE
     */
    // Contains the visibility status of the widget (VISIBLE, INVISIBLE, or GONE)
    var visibility = VISIBLE
    /**
     * Returns if this widget is animated. Currently only affects gone behaviour
     *
     * @return true if ConstraintWidget is used in Animation
     */
    /**
     * Set if this widget is animated. Currently only affects gone behaviour
     *
     * @param animated if true the widget must be positioned correctly when not visible
     */
    // Contains if this widget is animated. Currently only affects gone behaviour
    var isAnimated = false
    /**
     * Returns the name of this widget (used for debug purposes)
     *
     * @return the debug name
     */
    /**
     * Set the debug name of this widget
     */
    var debugName: String? = null
    /**
     * Returns the type string if set
     *
     * @return type (null if not set)
     */
    /**
     * Set the type of the widget (as a String)
     *
     * @param type type of the widget
     */
    open var type: String? = null
    var mDistToTop = 0
    var mDistToLeft = 0
    var mDistToRight = 0
    var mDistToBottom = 0
    var mLeftHasCentered = false
    var mRightHasCentered = false
    var mTopHasCentered = false
    var mBottomHasCentered = false
    var mHorizontalWrapVisited = false
    var mVerticalWrapVisited = false
    var mGroupsToSolver = false
    /**
     * get the chain starting from this widget to be packed.
     * The horizontal bias will control how elements of the chain are positioned.
     *
     * @return Horizontal Chain Style
     */
    /**
     * Set the chain starting from this widget to be packed.
     * The horizontal bias will control how elements of the chain are positioned.
     *
     * @param horizontalChainStyle (CHAIN_SPREAD, CHAIN_SPREAD_INSIDE, CHAIN_PACKED)
     */
    // Chain support
    var horizontalChainStyle = CHAIN_SPREAD
    /**
     * Set the chain starting from this widget to be packed.
     * The vertical bias will control how elements of the chain are positioned.
     */
    /**
     * Set the chain starting from this widget to be packed.
     * The vertical bias will control how elements of the chain are positioned.
     *
     * @param verticalChainStyle (CHAIN_SPREAD, CHAIN_SPREAD_INSIDE, CHAIN_PACKED)
     */
    var verticalChainStyle = CHAIN_SPREAD
    var mHorizontalChainFixedPosition = false
    var mVerticalChainFixedPosition = false
    var mWeight = floatArrayOf(UNKNOWN.toFloat(), UNKNOWN.toFloat())
    var mListNextMatchConstraintsWidget = arrayOf<ConstraintWidget?>(null, null)
    var mNextChainWidget = arrayOf<ConstraintWidget?>(null, null)
    var mHorizontalNextWidget: ConstraintWidget? = null
    var mVerticalNextWidget: ConstraintWidget? = null
    // TODO: see if we can make this simpler
    /**
     * @TODO: add description
     */
    open fun reset() {
        mLeft!!.reset()
        mTop.reset()
        mRight!!.reset()
        mBottom.reset()
        mBaseline!!.reset()
        mCenterX.reset()
        mCenterY.reset()
        mCenter.reset()
        parent = null
        mCircleConstraintAngle = Float.NaN
        mWidth = 0
        mHeight = 0
        dimensionRatio = 0f
        dimensionRatioSide = UNKNOWN
        mX = 0
        mY = 0
        mOffsetX = 0
        mOffsetY = 0
        mBaselineDistance = 0
        mMinWidth = 0
        mMinHeight = 0
        horizontalBiasPercent = DEFAULT_BIAS
        verticalBiasPercent = DEFAULT_BIAS
        mListDimensionBehaviors[DIMENSION_HORIZONTAL] = DimensionBehaviour.FIXED
        mListDimensionBehaviors[DIMENSION_VERTICAL] = DimensionBehaviour.FIXED
        companionWidget = null
        mContainerItemSkip = 0
        visibility = VISIBLE
        type = null
        mHorizontalWrapVisited = false
        mVerticalWrapVisited = false
        horizontalChainStyle = CHAIN_SPREAD
        verticalChainStyle = CHAIN_SPREAD
        mHorizontalChainFixedPosition = false
        mVerticalChainFixedPosition = false
        mWeight[DIMENSION_HORIZONTAL] = UNKNOWN.toFloat()
        mWeight[DIMENSION_VERTICAL] = UNKNOWN.toFloat()
        mHorizontalResolution = UNKNOWN
        mVerticalResolution = UNKNOWN
        mMaxDimension[HORIZONTAL] = Int.MAX_VALUE
        mMaxDimension[VERTICAL] = Int.MAX_VALUE
        mMatchConstraintDefaultWidth = MATCH_CONSTRAINT_SPREAD
        mMatchConstraintDefaultHeight = MATCH_CONSTRAINT_SPREAD
        mMatchConstraintPercentWidth = 1f
        mMatchConstraintPercentHeight = 1f
        mMatchConstraintMaxWidth = Int.MAX_VALUE
        mMatchConstraintMaxHeight = Int.MAX_VALUE
        mMatchConstraintMinWidth = 0
        mMatchConstraintMinHeight = 0
        mResolvedHasRatio = false
        mResolvedDimensionRatioSide = UNKNOWN
        mResolvedDimensionRatio = 1f
        mGroupsToSolver = false
        isTerminalWidget[HORIZONTAL] = true
        isTerminalWidget[VERTICAL] = true
        isInVirtualLayout = false
        mIsInBarrier[HORIZONTAL] = false
        mIsInBarrier[VERTICAL] = false
        mMeasureRequested = true
        mResolvedMatchConstraintDefault[HORIZONTAL] = 0
        mResolvedMatchConstraintDefault[VERTICAL] = 0
        mWidthOverride = -1
        mHeightOverride = -1
    }

    ///////////////////////////////////SERIALIZE///////////////////////////////////////////////
    private fun serializeAnchor(ret: StringBuilder, side: String, a: ConstraintAnchor?) {
        if (a?.target == null) {
            return
        }
        ret.append(side)
        ret.append(" : [ '")
        ret.append(a?.target)
        ret.append("',")
        ret.append(a!!.mMargin)
        ret.append(",")
        ret.append(a.mGoneMargin)
        ret.append(",")
        ret.append(" ] ,\n")
    }

    private fun serializeCircle(ret: StringBuilder, a: ConstraintAnchor, angle: Float) {
        if (a?.target == null || angle.isNaN()) {
            return
        }
        ret.append("circle : [ '")
        ret.append(a?.target)
        ret.append("',")
        ret.append(a.mMargin)
        ret.append(",")
        ret.append(angle)
        ret.append(",")
        ret.append(" ] ,\n")
    }

    private fun serializeAttribute(ret: StringBuilder, type: String, value: Float, def: Float) {
        if (value == def) {
            return
        }
        ret.append(type)
        ret.append(" :   ")
        ret.append(value)
        ret.append(",\n")
    }

    private fun serializeAttribute(ret: StringBuilder, type: String, value: Int, def: Int) {
        if (value == def) {
            return
        }
        ret.append(type)
        ret.append(" :   ")
        ret.append(value)
        ret.append(",\n")
    }

    private fun serializeDimensionRatio(
        ret: StringBuilder,
        type: String,
        value: Float,
        whichSide: Int
    ) {
        if (value == 0f) {
            return
        }
        ret.append(type)
        ret.append(" :  [")
        ret.append(value)
        ret.append(",")
        ret.append(whichSide)
        ret.append("")
        ret.append("],\n")
    }

    private fun serializeSize(
        ret: StringBuilder, type: String, size: Int,
        min: Int, max: Int, override: Int,
        matchConstraintMin: Int, matchConstraintDefault: Int,
        matchConstraintPercent: Float,
        weight: Float
    ) {
        ret.append(type)
        ret.append(" :  {\n")
        serializeAttribute(ret, "size", size, Int.MIN_VALUE)
        serializeAttribute(ret, "min", min, 0)
        serializeAttribute(ret, "max", max, Int.MAX_VALUE)
        serializeAttribute(ret, "matchMin", matchConstraintMin, 0)
        serializeAttribute(ret, "matchDef", matchConstraintDefault, MATCH_CONSTRAINT_SPREAD)
        serializeAttribute(ret, "matchPercent", matchConstraintDefault, 1)
        serializeAttribute(ret, "matchConstraintPercent", matchConstraintPercent, 1f)
        serializeAttribute(ret, "weight", weight, 1f)
        serializeAttribute(ret, "override", override, 1)
        ret.append("},\n")
    }

    /**
     * Serialize the anchors for JSON5 output
     * @param ret StringBuilder to be populated
     * @return the same string builder to alow chaining
     */
    fun serialize(ret: StringBuilder): StringBuilder {
        ret.append("{\n")
        serializeAnchor(ret, "left", mLeft)
        serializeAnchor(ret, "top", mTop)
        serializeAnchor(ret, "right", mRight)
        serializeAnchor(ret, "bottom", mBottom)
        serializeAnchor(ret, "baseline", mBaseline)
        serializeAnchor(ret, "centerX", mCenterX)
        serializeAnchor(ret, "centerY", mCenterY)
        serializeCircle(ret, mCenter, mCircleConstraintAngle)
        serializeSize(
            ret, "width",
            mWidth,
            mMinWidth,
            mMaxDimension[HORIZONTAL],
            mWidthOverride,
            mMatchConstraintMinWidth,
            mMatchConstraintDefaultWidth,
            mMatchConstraintPercentWidth,
            mWeight[DIMENSION_HORIZONTAL]
        )
        serializeSize(
            ret, "height",
            mHeight,
            mMinHeight,
            mMaxDimension[VERTICAL],
            mHeightOverride,
            mMatchConstraintMinHeight,
            mMatchConstraintDefaultHeight,
            mMatchConstraintPercentHeight,
            mWeight[DIMENSION_VERTICAL]
        )
        serializeDimensionRatio(ret, "dimensionRatio", dimensionRatio, dimensionRatioSide)
        serializeAttribute(ret, "horizontalBias", horizontalBiasPercent, DEFAULT_BIAS)
        serializeAttribute(ret, "verticalBias", verticalBiasPercent, DEFAULT_BIAS)
        ret.append("}\n")
        return ret
    }

    ///////////////////////////////////END SERIALIZE///////////////////////////////////////////
    var horizontalGroup = -1
    var verticalGroup = -1

    /**
     * @TODO: add description
     */
    fun oppositeDimensionDependsOn(orientation: Int): Boolean {
        val oppositeOrientation = if (orientation == HORIZONTAL) VERTICAL else HORIZONTAL
        val dimensionBehaviour = mListDimensionBehaviors[orientation]
        val oppositeDimensionBehaviour = mListDimensionBehaviors[oppositeOrientation]
        return (dimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT
                && oppositeDimensionBehaviour == DimensionBehaviour.MATCH_CONSTRAINT)
        //&& mDimensionRatio != 0;
    }

    /**
     * @TODO: add description
     */
    fun oppositeDimensionsTied(): Boolean {
        return  /* isInHorizontalChain() || isInVerticalChain() || */(mListDimensionBehaviors[HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT
                && mListDimensionBehaviors[VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT)
    }

    /**
     * @TODO: add description
     */
    fun hasDimensionOverride(): Boolean {
        return mWidthOverride != -1 || mHeightOverride != -1
    }
    /*-----------------------------------------------------------------------*/ // Creation
    /*-----------------------------------------------------------------------*/
    /**
     * Default constructor
     */
    constructor() {
        addAnchors()
    }

    constructor(debugName: String?) {
        addAnchors()
        this.debugName = debugName
    }

    /**
     * Constructor
     *
     * @param x      x position
     * @param y      y position
     * @param width  width of the layout
     * @param height height of the layout
     */
    constructor(x: Int, y: Int, width: Int, height: Int) {
        mX = x
        mY = y
        mWidth = width
        mHeight = height
        addAnchors()
    }

    constructor(debugName: String?, x: Int, y: Int, width: Int, height: Int) : this(x, y, width, height) {
        this.debugName = debugName
    }

    /**
     * Constructor
     *
     * @param width  width of the layout
     * @param height height of the layout
     */
    constructor(width: Int, height: Int) : this(0, 0, width, height) {}

    /**
     * @TODO: add description
     */
    fun ensureWidgetRuns() {
        if (mHorizontalRun == null) {
            mHorizontalRun = HorizontalWidgetRun(this)
        }
        if (mVerticalRun == null) {
            mVerticalRun = VerticalWidgetRun(this)
        }
    }

    constructor(debugName: String?, width: Int, height: Int) : this(width, height) {
        this.debugName = debugName
    }

    /**
     * Reset the solver variables of the anchors
     */
    open fun resetSolverVariables(cache: Cache?) {
        mLeft!!.resetSolverVariable(cache)
        mTop.resetSolverVariable(cache)
        mRight!!.resetSolverVariable(cache)
        mBottom.resetSolverVariable(cache)
        mBaseline!!.resetSolverVariable(cache)
        mCenter.resetSolverVariable(cache)
        mCenterX.resetSolverVariable(cache)
        mCenterY.resetSolverVariable(cache)
    }

    /**
     * Add all the anchors to the mAnchors array
     */
    private fun addAnchors() {
        mAnchors.add(mLeft!!)
        mAnchors.add(mTop)
        mAnchors.add(mRight!!)
        mAnchors.add(mBottom)
        mAnchors.add(mCenterX)
        mAnchors.add(mCenterY)
        mAnchors.add(mCenter)
        mAnchors.add(mBaseline!!)
    }

    /**
     * Returns true if the widget is the root widget
     *
     * @return true if root widget, false otherwise
     */
    val isRoot: Boolean
        get() = parent == null

    /**
     * Set a circular constraint
     *
     * @param target the target widget we will use as the center of the circle
     * @param angle  the angle (from 0 to 360)
     * @param radius the radius used
     */
    fun connectCircularConstraint(target: ConstraintWidget, angle: Float, radius: Int) {
        immediateConnect(
            ConstraintAnchor.Type.CENTER, target, ConstraintAnchor.Type.CENTER,
            radius, 0
        )
        mCircleConstraintAngle = angle
    }

    /**
     * Utility debug function. Sets the names of the anchors in the solver given
     * a widget's name. The given name is used as a prefix, resulting in anchors' names
     * of the form:
     *
     *
     *
     *  * {name}.left
     *  * {name}.top
     *  * {name}.right
     *  * {name}.bottom
     *  * {name}.baseline
     *
     *
     * @param system solver used
     * @param name   name of the widget
     */
    fun setDebugSolverName(system: LinearSystem, name: String?) {
        debugName = name
        val left = system.createObjectVariable(mLeft)
        val top = system.createObjectVariable(mTop)
        val right = system.createObjectVariable(mRight)
        val bottom = system.createObjectVariable(mBottom)
        left!!.name = "$name.left"
        top!!.name = "$name.top"
        right!!.name = "$name.right"
        bottom!!.name = "$name.bottom"
        val baseline = system.createObjectVariable(mBaseline)
        baseline!!.name = "$name.baseline"
    }

    /**
     * Create all the system variables for this widget
     *
     * @DoNotShow
     */
    fun createObjectVariables(system: LinearSystem) {
        system.createObjectVariable(mLeft)
        system.createObjectVariable(mTop)
        system.createObjectVariable(mRight)
        system.createObjectVariable(mBottom)
        if (mBaselineDistance > 0) {
            system.createObjectVariable(mBaseline)
        }
    }

    /**
     * Returns a string representation of the ConstraintWidget
     *
     * @return string representation of the widget
     */
    override fun toString(): String {
        return ((if (type != null) "type: " + type + " " else "")
                + (if (debugName != null) "id: " + debugName + " " else "")
                + "(" + mX + ", " + mY + ") - (" + mWidth + " x " + mHeight + ")")
    }
    /*-----------------------------------------------------------------------*/ // Position
    /*-----------------------------------------------------------------------*/ // The widget position is expressed in two ways:
    // - relative to its direct parent container (getX(), getY())
    // - relative to the root container (getDrawX(), getDrawY())
    // Additionally, getDrawX()/getDrawY() are used when animating the
    // widget position on screen
    /*-----------------------------------------------------------------------*/
    /**
     * Return the x position of the widget, relative to its container
     *
     * @return x position
     */
    /**
     * Set the x position of the widget, relative to its container
     *
     * @param x x position
     */
    var x: Int
        get() = if (parent != null && parent is ConstraintWidgetContainer) {
            (parent as ConstraintWidgetContainer).mPaddingLeft + mX
        } else mX
        set(x) {
            mX = x
        }
    /**
     * Return the y position of the widget, relative to its container
     *
     * @return y position
     */
    /**
     * Set the y position of the widget, relative to its container
     *
     * @param y y position
     */
    var y: Int
        get() = if (parent != null && parent is ConstraintWidgetContainer) {
            (parent as ConstraintWidgetContainer).mPaddingTop + mY
        } else mY
        set(y) {
            mY = y
        }
    /**
     * Return the width of the widget
     *
     * @return width width
     */
    /**
     * Set the width of the widget
     *
     * @param w width
     */
    var width: Int
        get() = if (visibility == GONE) {
            0
        } else mWidth
        set(w) {
            mWidth = w
            if (mWidth < mMinWidth) {
                mWidth = mMinWidth
            }
        }

    /**
     * @TODO: add description
     */
    val optimizerWrapWidth: Int
        get() {
            var w = mWidth
            if (mListDimensionBehaviors[DIMENSION_HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT) {
                if (mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_WRAP) {
                    w = max(mMatchConstraintMinWidth, w)
                } else if (mMatchConstraintMinWidth > 0) {
                    w = mMatchConstraintMinWidth
                    mWidth = w
                } else {
                    w = 0
                }
                if (mMatchConstraintMaxWidth > 0 && mMatchConstraintMaxWidth < w) {
                    w = mMatchConstraintMaxWidth
                }
            }
            return w
        }

    /**
     * @TODO: add description
     */
    val optimizerWrapHeight: Int
        get() {
            var h = mHeight
            if (mListDimensionBehaviors[DIMENSION_VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT) {
                if (mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_WRAP) {
                    h = max(mMatchConstraintMinHeight, h)
                } else if (mMatchConstraintMinHeight > 0) {
                    h = mMatchConstraintMinHeight
                    mHeight = h
                } else {
                    h = 0
                }
                if (mMatchConstraintMaxHeight > 0 && mMatchConstraintMaxHeight < h) {
                    h = mMatchConstraintMaxHeight
                }
            }
            return h
        }
    /**
     * Return the height of the widget
     *
     * @return height height
     */
    /**
     * Set the height of the widget
     *
     * @param h height
     */
    var height: Int
        get() = if (visibility == GONE) {
            0
        } else mHeight
        set(h) {
            mHeight = h
            if (mHeight < mMinHeight) {
                mHeight = mMinHeight
            }
        }

    /**
     * Get a dimension of the widget in a particular orientation.
     *
     * @return The dimension of the specified orientation.
     */
    fun getLength(orientation: Int): Int {
        return if (orientation == HORIZONTAL) {
            width
        } else if (orientation == VERTICAL) {
            height
        } else {
            0
        }
    }

    /**
     * Return the x position of the widget, relative to the root
     * (without animation)
     *
     * @return x position
     */
    protected val rootX: Int
        protected get() = mX + mOffsetX

    /**
     * Return the y position of the widget, relative to the root
     * (without animation)
     */
    protected val rootY: Int
        protected get() = mY + mOffsetY
    /**
     * Return the minimum width of the widget
     *
     * @return minimum width
     */
    /**
     * Set the minimum width of the widget
     *
     * @param w minimum width
     */
    var minWidth: Int
        get() = mMinWidth
        set(w) {
            mMinWidth = if (w < 0) {
                0
            } else {
                w
            }
        }
    /**
     * Return the minimum height of the widget
     *
     * @return minimum height
     */
    /**
     * Set the minimum height of the widget
     *
     * @param h minimum height
     */
    var minHeight: Int
        get() = mMinHeight
        set(h) {
            mMinHeight = if (h < 0) {
                0
            } else {
                h
            }
        }

    /**
     * Return the left position of the widget (similar to [.getX])
     *
     * @return left position of the widget
     */
    val left: Int
        get() = x

    /**
     * Return the top position of the widget (similar to [.getY])
     *
     * @return top position of the widget
     */
    val top: Int
        get() = y

    /**
     * Return the right position of the widget
     *
     * @return right position of the widget
     */
    val right: Int
        get() = x + mWidth

    /**
     * Return the bottom position of the widget
     *
     * @return bottom position of the widget
     */
    val bottom: Int
        get() = y + mHeight

    /**
     * Returns all the horizontal margin of the widget.
     */
    val horizontalMargin: Int
        get() {
            var margin = 0
            if (mLeft != null) {
                margin += mLeft!!.mMargin
            }
            if (mRight != null) {
                margin += mRight!!.mMargin
            }
            return margin
        }

    /**
     * Returns all the vertical margin of the widget
     */
    val verticalMargin: Int
        get() {
            var margin = 0
            if (mLeft != null) {
                margin += mTop.mMargin
            }
            if (mRight != null) {
                margin += mBottom.mMargin
            }
            return margin
        }

    /**
     * Return the percentage bias that is used when two opposite connections exist of the same
     * strength in a particular orientation.
     *
     * @param orientation Orientation [.HORIZONTAL]/[.VERTICAL].
     * @return Respective percentage bias.
     */
    fun getBiasPercent(orientation: Int): Float {
        return if (orientation == HORIZONTAL) {
            horizontalBiasPercent
        } else if (orientation == VERTICAL) {
            verticalBiasPercent
        } else {
            UNKNOWN.toFloat()
        }
    }

    /**
     * Return the baseline distance relative to the top of the widget
     *
     * @return baseline
     */
    /**
     * Set the baseline distance relative to the top of the widget
     *
     * @param baseline the distance of the baseline relative to the widget's top
     */
    var baselineDistance: Int
        get() = mBaselineDistance
        set(baseline) {
            mBaselineDistance = baseline
            hasBaseline = baseline > 0
        }

    /**
     * Return the array of anchors of this widget
     *
     * @return array of anchors
     */
    val anchors: ArrayList<ConstraintAnchor>
        get() = mAnchors

    /**
     * Set both the origin in (x, y) of the widget, relative to its container
     *
     * @param x x position
     * @param y y position
     */
    fun setOrigin(x: Int, y: Int) {
        mX = x
        mY = y
    }

    /**
     * Set the offset of this widget relative to the root widget
     *
     * @param x horizontal offset
     * @param y vertical offset
     */
    open fun setOffset(x: Int, y: Int) {
        mOffsetX = x
        mOffsetY = y
    }

    /**
     * Set the margin to be used when connected to a widget with a visibility of GONE
     *
     * @param type       the anchor to set the margin on
     * @param goneMargin the margin value to use
     */
    fun setGoneMargin(type: ConstraintAnchor.Type?, goneMargin: Int) {
        when (type) {
            ConstraintAnchor.Type.LEFT -> {
                mLeft!!.mGoneMargin = goneMargin
            }

            ConstraintAnchor.Type.TOP -> {
                mTop.mGoneMargin = goneMargin
            }

            ConstraintAnchor.Type.RIGHT -> {
                mRight!!.mGoneMargin = goneMargin
            }

            ConstraintAnchor.Type.BOTTOM -> {
                mBottom.mGoneMargin = goneMargin
            }

            ConstraintAnchor.Type.BASELINE -> {
                mBaseline!!.mGoneMargin = goneMargin
            }

            ConstraintAnchor.Type.CENTER, ConstraintAnchor.Type.CENTER_X, ConstraintAnchor.Type.CENTER_Y, ConstraintAnchor.Type.NONE -> {}

            else -> {}
        }
    }

    /**
     * Set the dimension of a widget in a particular orientation.
     *
     * @param length      Size of the dimension.
     * @param orientation HORIZONTAL or VERTICAL
     */
    fun setLength(length: Int, orientation: Int) {
        if (orientation == HORIZONTAL) {
            width = length
        } else if (orientation == VERTICAL) {
            height = length
        }
    }

    /**
     * Set the horizontal style when MATCH_CONSTRAINT is set
     *
     * @param horizontalMatchStyle MATCH_CONSTRAINT_SPREAD or MATCH_CONSTRAINT_WRAP
     * @param min                  minimum value
     * @param max                  maximum value
     * @param percent              Percent width
     */
    fun setHorizontalMatchStyle(horizontalMatchStyle: Int, min: Int, max: Int, percent: Float) {
        mMatchConstraintDefaultWidth = horizontalMatchStyle
        mMatchConstraintMinWidth = min
        mMatchConstraintMaxWidth = if (max == Int.MAX_VALUE) 0 else max
        mMatchConstraintPercentWidth = percent
        if (percent > 0 && percent < 1 && mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_SPREAD) {
            mMatchConstraintDefaultWidth = MATCH_CONSTRAINT_PERCENT
        }
    }

    /**
     * Set the vertical style when MATCH_CONSTRAINT is set
     *
     * @param verticalMatchStyle MATCH_CONSTRAINT_SPREAD or MATCH_CONSTRAINT_WRAP
     * @param min                minimum value
     * @param max                maximum value
     * @param percent            Percent height
     */
    fun setVerticalMatchStyle(verticalMatchStyle: Int, min: Int, max: Int, percent: Float) {
        mMatchConstraintDefaultHeight = verticalMatchStyle
        mMatchConstraintMinHeight = min
        mMatchConstraintMaxHeight = if (max == Int.MAX_VALUE) 0 else max
        mMatchConstraintPercentHeight = percent
        if (percent > 0 && percent < 1 && mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_SPREAD) {
            mMatchConstraintDefaultHeight = MATCH_CONSTRAINT_PERCENT
        }
    }

    /**
     * Set the ratio of the widget
     *
     * @param ratio given string of format [H|V],[float|x:y] or [float|x:y]
     */
    fun setDimensionRatio(ratio: String?) {
        if (ratio == null || ratio.length == 0) {
            dimensionRatio = 0f
            return
        }
        var dimensionRatioSide = UNKNOWN
        var dimensionRatio = 0f
        val len = ratio.length
        var commaIndex = ratio.indexOf(',')
        if (commaIndex > 0 && commaIndex < len - 1) {
            val dimension = ratio.substring(0, commaIndex)
            if (dimension.equals("W", ignoreCase = true)) {
                dimensionRatioSide = HORIZONTAL
            } else if (dimension.equals("H", ignoreCase = true)) {
                dimensionRatioSide = VERTICAL
            }
            commaIndex++
        } else {
            commaIndex = 0
        }
        val colonIndex = ratio.indexOf(':')
        if (colonIndex >= 0 && colonIndex < len - 1) {
            val nominator = ratio.substring(commaIndex, colonIndex)
            val denominator = ratio.substring(colonIndex + 1)
            if (nominator.length > 0 && denominator.length > 0) {
                try {
                    val nominatorValue = nominator.toFloat()
                    val denominatorValue = denominator.toFloat()
                    if (nominatorValue > 0 && denominatorValue > 0) {
                        dimensionRatio = if (dimensionRatioSide == VERTICAL) {
                            abs(denominatorValue / nominatorValue)
                        } else {
                            abs(nominatorValue / denominatorValue)
                        }
                    }
                } catch (e: NumberFormatException) {
                    // Ignore
                }
            }
        } else {
            val r = ratio.substring(commaIndex)
            if (r.length > 0) {
                try {
                    dimensionRatio = r.toFloat()
                } catch (e: NumberFormatException) {
                    // Ignore
                }
            }
        }
        if (dimensionRatio > 0) {
            this.dimensionRatio = dimensionRatio
            this.dimensionRatioSide = dimensionRatioSide
        }
    }

    /**
     * Set the ratio of the widget
     * The ratio will be applied if at least one of the dimension
     * (width or height) is set to a behaviour
     * of DimensionBehaviour.MATCH_CONSTRAINT
     * -- the dimension's value will be set to the other dimension * ratio.
     *
     * @param ratio              A float value that describes W/H or H/W depending
     * on the provided dimensionRatioSide
     * @param dimensionRatioSide The side the ratio should be calculated on,
     * HORIZONTAL, VERTICAL, or UNKNOWN
     */
    fun setDimensionRatio(ratio: Float, dimensionRatioSide: Int) {
        dimensionRatio = ratio
        this.dimensionRatioSide = dimensionRatioSide
    }

    /**
     * Set both width and height of the widget
     *
     * @param w width
     * @param h height
     */
    fun setDimension(w: Int, h: Int) {
        mWidth = w
        if (mWidth < mMinWidth) {
            mWidth = mMinWidth
        }
        mHeight = h
        if (mHeight < mMinHeight) {
            mHeight = mMinHeight
        }
    }

    /**
     * Set the position+dimension of the widget given left/top/right/bottom
     *
     * @param left   left side position of the widget
     * @param top    top side position of the widget
     * @param right  right side position of the widget
     * @param bottom bottom side position of the widget
     */
    fun setFrame(left: Int, top: Int, right: Int, bottom: Int) {
        var w = right - left
        var h = bottom - top
        mX = left
        mY = top
        if (visibility == GONE) {
            mWidth = 0
            mHeight = 0
            return
        }

        // correct dimensional instability caused by rounding errors
        if (mListDimensionBehaviors[DIMENSION_HORIZONTAL]
            == DimensionBehaviour.FIXED && w < mWidth
        ) {
            w = mWidth
        }
        if (mListDimensionBehaviors[DIMENSION_VERTICAL]
            == DimensionBehaviour.FIXED && h < mHeight
        ) {
            h = mHeight
        }
        mWidth = w
        mHeight = h
        if (mHeight < mMinHeight) {
            mHeight = mMinHeight
        }
        if (mWidth < mMinWidth) {
            mWidth = mMinWidth
        }
        if (mMatchConstraintMaxWidth > 0
            && mListDimensionBehaviors[HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT
        ) {
            mWidth = min(mWidth, mMatchConstraintMaxWidth)
        }
        if (mMatchConstraintMaxHeight > 0
            && mListDimensionBehaviors[VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT
        ) {
            mHeight = min(mHeight, mMatchConstraintMaxHeight)
        }
        if (w != mWidth) {
            mWidthOverride = mWidth
        }
        if (h != mHeight) {
            mHeightOverride = mHeight
        }
        if (LinearSystem.FULL_DEBUG) {
            println(
                "update from solver " + debugName
                        + " " + mX + ":" + mY + " - " + mWidth + " x " + mHeight
            )
        }
    }

    /**
     * Set the position+dimension of the widget based on starting/ending positions on one dimension.
     *
     * @param start       Left/Top side position of the widget.
     * @param end         Right/Bottom side position of the widget.
     * @param orientation Orientation being set (HORIZONTAL/VERTICAL).
     */
    fun setFrame(start: Int, end: Int, orientation: Int) {
        if (orientation == HORIZONTAL) {
            setHorizontalDimension(start, end)
        } else if (orientation == VERTICAL) {
            setVerticalDimension(start, end)
        }
    }

    /**
     * Set the positions for the horizontal dimension only
     *
     * @param left  left side position of the widget
     * @param right right side position of the widget
     */
    fun setHorizontalDimension(left: Int, right: Int) {
        mX = left
        mWidth = right - left
        if (mWidth < mMinWidth) {
            mWidth = mMinWidth
        }
    }

    /**
     * Set the positions for the vertical dimension only
     *
     * @param top    top side position of the widget
     * @param bottom bottom side position of the widget
     */
    fun setVerticalDimension(top: Int, bottom: Int) {
        mY = top
        mHeight = bottom - top
        if (mHeight < mMinHeight) {
            mHeight = mMinHeight
        }
    }

    /**
     * Get the left/top position of the widget relative to
     * the outer side of the container (right/bottom).
     *
     * @param orientation Orientation by which to find the relative positioning of the widget.
     * @return The relative position of the widget.
     */
    fun getRelativePositioning(orientation: Int): Int {
        return if (orientation == HORIZONTAL) {
            mRelX
        } else if (orientation == VERTICAL) {
            mRelY
        } else {
            0
        }
    }

    /**
     * Set the left/top position of the widget relative to
     * the outer side of the container (right/bottom).
     *
     * @param offset      Offset of the relative position.
     * @param orientation Orientation of the offset being set.
     */
    fun setRelativePositioning(offset: Int, orientation: Int) {
        if (orientation == HORIZONTAL) {
            mRelX = offset
        } else if (orientation == VERTICAL) {
            mRelY = offset
        }
    }
    /**
     * Accessor for the skip value
     *
     * @return skip value
     */
    /**
     * Set the skip value for this widget. This can be used when a widget is in a container,
     * so that container can position the widget as if it was positioned further in the list
     * of widgets. For example, with Table, this is used to skip empty cells
     * (the widget after an empty cell will have a skip value of one)
     */
    var containerItemSkip: Int
        get() = mContainerItemSkip
        set(skip) {
            mContainerItemSkip = if (skip >= 0) {
                skip
            } else {
                0
            }
        }

    /**
     * Set the horizontal weight (only used in chains)
     *
     * @param horizontalWeight Floating point value weight
     */
    fun setHorizontalWeight(horizontalWeight: Float) {
        mWeight[DIMENSION_HORIZONTAL] = horizontalWeight
    }

    /**
     * Set the vertical weight (only used in chains)
     *
     * @param verticalWeight Floating point value weight
     */
    fun setVerticalWeight(verticalWeight: Float) {
        mWeight[DIMENSION_VERTICAL] = verticalWeight
    }

    /**
     * Returns true if this widget should be used in a barrier
     */
    open fun allowedInBarrier(): Boolean {
        return visibility != GONE
    }
    /*-----------------------------------------------------------------------*/ // Connections
    /*-----------------------------------------------------------------------*/
    /**
     * Immediate connection to an anchor without any checks.
     *
     * @param startType  The type of anchor on this widget
     * @param target     The target widget
     * @param endType    The type of anchor on the target widget
     * @param margin     How much margin we want to keep as
     * a minimum distance between the two anchors
     * @param goneMargin How much margin we want to keep if the target is set to `View.GONE`
     */
    fun immediateConnect(
        startType: ConstraintAnchor.Type?, target: ConstraintWidget,
        endType: ConstraintAnchor.Type?, margin: Int, goneMargin: Int
    ) {
        val startAnchor = getAnchor(startType)
        val endAnchor = target.getAnchor(endType)
        startAnchor!!.connect(endAnchor, margin, goneMargin, true)
    }

    /**
     * Connect the given anchors together (the from anchor should be owned by this widget)
     *
     * @param from   the anchor we are connecting from (of this widget)
     * @param to     the anchor we are connecting to
     * @param margin how much margin we want to have
     */
    fun connect(from: ConstraintAnchor?, to: ConstraintAnchor?, margin: Int) {
        if (from?.owner === this) {
            connect(from.type, to?.owner, to?.type, margin)
        }
    }

    /**
     * Connect a given anchor of this widget to another anchor of a target widget
     *
     * @param constraintFrom which anchor of this widget to connect from
     * @param target         the target widget
     * @param constraintTo   the target anchor on the target widget
     */
    fun connect(
        constraintFrom: ConstraintAnchor.Type,
        target: ConstraintWidget,
        constraintTo: ConstraintAnchor.Type
    ) {
        if (LinearSystem.DEBUG) {
            println(
                debugName + " connect "
                        + constraintFrom + " to " + target + " " + constraintTo
            )
        }
        connect(constraintFrom, target, constraintTo, 0)
    }

    /**
     * Connect a given anchor of this widget to another anchor of a target widget
     *
     * @param constraintFrom which anchor of this widget to connect from
     * @param target         the target widget
     * @param constraintTo   the target anchor on the target widget
     * @param margin         how much margin we want to keep as
     * a minimum distance between the two anchors
     */
    fun connect(
        constraintFrom: ConstraintAnchor.Type?,
        target: ConstraintWidget?,
        constraintTo: ConstraintAnchor.Type?, margin: Int
    ) {
        if (constraintFrom == ConstraintAnchor.Type.CENTER) {
            // If we have center, we connect instead to the corresponding
            // left/right or top/bottom pairs
            if (constraintTo == ConstraintAnchor.Type.CENTER) {
                val left = getAnchor(ConstraintAnchor.Type.LEFT)
                val right = getAnchor(ConstraintAnchor.Type.RIGHT)
                val top = getAnchor(ConstraintAnchor.Type.TOP)
                val bottom = getAnchor(ConstraintAnchor.Type.BOTTOM)
                var centerX = false
                var centerY = false
                if (left != null && left.isConnected || right != null && right.isConnected) {
                    // don't apply center here
                } else {
                    connect(
                        ConstraintAnchor.Type.LEFT, target,
                        ConstraintAnchor.Type.LEFT, 0
                    )
                    connect(
                        ConstraintAnchor.Type.RIGHT, target,
                        ConstraintAnchor.Type.RIGHT, 0
                    )
                    centerX = true
                }
                if (top != null && top.isConnected || bottom != null && bottom.isConnected) {
                    // don't apply center here
                } else {
                    connect(
                        ConstraintAnchor.Type.TOP, target,
                        ConstraintAnchor.Type.TOP, 0
                    )
                    connect(
                        ConstraintAnchor.Type.BOTTOM, target,
                        ConstraintAnchor.Type.BOTTOM, 0
                    )
                    centerY = true
                }
                if (centerX && centerY) {
                    val center = getAnchor(ConstraintAnchor.Type.CENTER)
                    center!!.connect(target!!.getAnchor(ConstraintAnchor.Type.CENTER), 0)
                } else if (centerX) {
                    val center = getAnchor(ConstraintAnchor.Type.CENTER_X)
                    center!!.connect(target!!.getAnchor(ConstraintAnchor.Type.CENTER_X), 0)
                } else if (centerY) {
                    val center = getAnchor(ConstraintAnchor.Type.CENTER_Y)
                    center!!.connect(target!!.getAnchor(ConstraintAnchor.Type.CENTER_Y), 0)
                }
            } else if (constraintTo == ConstraintAnchor.Type.LEFT || constraintTo == ConstraintAnchor.Type.RIGHT) {
                connect(
                    ConstraintAnchor.Type.LEFT, target,
                    constraintTo, 0
                )
                connect(
                    ConstraintAnchor.Type.RIGHT, target,
                    constraintTo, 0
                )
                val center = getAnchor(ConstraintAnchor.Type.CENTER)
                center!!.connect(target!!.getAnchor(constraintTo), 0)
            } else if (constraintTo == ConstraintAnchor.Type.TOP || constraintTo == ConstraintAnchor.Type.BOTTOM) {
                connect(
                    ConstraintAnchor.Type.TOP, target,
                    constraintTo, 0
                )
                connect(
                    ConstraintAnchor.Type.BOTTOM, target,
                    constraintTo, 0
                )
                val center = getAnchor(ConstraintAnchor.Type.CENTER)
                center!!.connect(target!!.getAnchor(constraintTo), 0)
            }
        } else if (constraintFrom == ConstraintAnchor.Type.CENTER_X
            && (constraintTo == ConstraintAnchor.Type.LEFT
                    || constraintTo == ConstraintAnchor.Type.RIGHT)
        ) {
            val left = getAnchor(ConstraintAnchor.Type.LEFT)
            val targetAnchor = target!!.getAnchor(constraintTo)
            val right = getAnchor(ConstraintAnchor.Type.RIGHT)
            left!!.connect(targetAnchor, 0)
            right!!.connect(targetAnchor, 0)
            val centerX = getAnchor(ConstraintAnchor.Type.CENTER_X)
            centerX!!.connect(targetAnchor, 0)
        } else if (constraintFrom == ConstraintAnchor.Type.CENTER_Y
            && (constraintTo == ConstraintAnchor.Type.TOP
                    || constraintTo == ConstraintAnchor.Type.BOTTOM)
        ) {
            val targetAnchor = target!!.getAnchor(constraintTo)
            val top = getAnchor(ConstraintAnchor.Type.TOP)
            top!!.connect(targetAnchor, 0)
            val bottom = getAnchor(ConstraintAnchor.Type.BOTTOM)
            bottom!!.connect(targetAnchor, 0)
            val centerY = getAnchor(ConstraintAnchor.Type.CENTER_Y)
            centerY!!.connect(targetAnchor, 0)
        } else if (constraintFrom == ConstraintAnchor.Type.CENTER_X
            && constraintTo == ConstraintAnchor.Type.CENTER_X
        ) {
            // Center X connection will connect left & right
            val left = getAnchor(ConstraintAnchor.Type.LEFT)
            val leftTarget = target!!.getAnchor(ConstraintAnchor.Type.LEFT)
            left!!.connect(leftTarget, 0)
            val right = getAnchor(ConstraintAnchor.Type.RIGHT)
            val rightTarget = target.getAnchor(ConstraintAnchor.Type.RIGHT)
            right!!.connect(rightTarget, 0)
            val centerX = getAnchor(ConstraintAnchor.Type.CENTER_X)
            centerX!!.connect(target.getAnchor(constraintTo), 0)
        } else if (constraintFrom == ConstraintAnchor.Type.CENTER_Y
            && constraintTo == ConstraintAnchor.Type.CENTER_Y
        ) {
            // Center Y connection will connect top & bottom.
            val top = getAnchor(ConstraintAnchor.Type.TOP)
            val topTarget = target!!.getAnchor(ConstraintAnchor.Type.TOP)
            top!!.connect(topTarget, 0)
            val bottom = getAnchor(ConstraintAnchor.Type.BOTTOM)
            val bottomTarget = target.getAnchor(ConstraintAnchor.Type.BOTTOM)
            bottom!!.connect(bottomTarget, 0)
            val centerY = getAnchor(ConstraintAnchor.Type.CENTER_Y)
            centerY!!.connect(target.getAnchor(constraintTo), 0)
        } else {
            val fromAnchor = getAnchor(constraintFrom)
            val toAnchor = target!!.getAnchor(constraintTo)
            if (fromAnchor!!.isValidConnection(toAnchor)) {
                // make sure that the baseline takes precedence over top/bottom
                // and reversely, reset the baseline if we are connecting top/bottom
                if (constraintFrom == ConstraintAnchor.Type.BASELINE) {
                    val top = getAnchor(ConstraintAnchor.Type.TOP)
                    val bottom = getAnchor(ConstraintAnchor.Type.BOTTOM)
                    top?.reset()
                    bottom?.reset()
                } else if (constraintFrom == ConstraintAnchor.Type.TOP || constraintFrom == ConstraintAnchor.Type.BOTTOM) {
                    val baseline = getAnchor(ConstraintAnchor.Type.BASELINE)
                    baseline?.reset()
                    val center = getAnchor(ConstraintAnchor.Type.CENTER)
                    if (center?.target !== toAnchor) {
                        center!!.reset()
                    }
                    val opposite = getAnchor(constraintFrom)!!.opposite
                    val centerY = getAnchor(ConstraintAnchor.Type.CENTER_Y)
                    if (centerY!!.isConnected) {
                        opposite!!.reset()
                        centerY.reset()
                    } else {
                        if (AUTOTAG_CENTER) {
                            // let's see if we need to mark center_y as connected
                            if (opposite!!.isConnected && opposite.target?.owner === toAnchor?.owner) {
                                val targetCenterY = toAnchor?.owner?.getAnchor(
                                    ConstraintAnchor.Type.CENTER_Y
                                )
                                centerY.connect(targetCenterY, 0)
                            }
                        }
                    }
                } else if (constraintFrom == ConstraintAnchor.Type.LEFT || constraintFrom == ConstraintAnchor.Type.RIGHT) {
                    val center = getAnchor(ConstraintAnchor.Type.CENTER)
                    if (center?.target !== toAnchor) {
                        center!!.reset()
                    }
                    val opposite = getAnchor(constraintFrom)!!.opposite
                    val centerX = getAnchor(ConstraintAnchor.Type.CENTER_X)
                    if (centerX!!.isConnected) {
                        opposite!!.reset()
                        centerX.reset()
                    } else {
                        if (AUTOTAG_CENTER) {
                            // let's see if we need to mark center_x as connected
                            if (opposite!!.isConnected && opposite.target?.owner === toAnchor?.owner) {
                                val targetCenterX = toAnchor?.owner?.getAnchor(
                                    ConstraintAnchor.Type.CENTER_X
                                )
                                centerX.connect(targetCenterX, 0)
                            }
                        }
                    }
                }
                fromAnchor.connect(toAnchor, margin)
            }
        }
    }

    /**
     * Reset all the constraints set on this widget
     */
    fun resetAllConstraints() {
        resetAnchors()
        verticalBiasPercent = DEFAULT_BIAS
        horizontalBiasPercent = DEFAULT_BIAS
    }

    /**
     * Reset the given anchor
     *
     * @param anchor the anchor we want to reset
     */
    fun resetAnchor(anchor: ConstraintAnchor) {
        if (parent != null) {
            if (parent is ConstraintWidgetContainer) {
                val parent = parent as ConstraintWidgetContainer?
                if (parent!!.handlesInternalConstraints()) {
                    return
                }
            }
        }
        val left = getAnchor(ConstraintAnchor.Type.LEFT)
        val right = getAnchor(ConstraintAnchor.Type.RIGHT)
        val top = getAnchor(ConstraintAnchor.Type.TOP)
        val bottom = getAnchor(ConstraintAnchor.Type.BOTTOM)
        val center = getAnchor(ConstraintAnchor.Type.CENTER)
        val centerX = getAnchor(ConstraintAnchor.Type.CENTER_X)
        val centerY = getAnchor(ConstraintAnchor.Type.CENTER_Y)
        if (anchor === center) {
            if (left!!.isConnected && right!!.isConnected && left.target === right.target) {
                left!!.reset()
                right!!.reset()
            }
            if (top!!.isConnected && bottom!!.isConnected && top.target === bottom.target) {
                top!!.reset()
                bottom!!.reset()
            }
            horizontalBiasPercent = 0.5f
            verticalBiasPercent = 0.5f
        } else if (anchor === centerX) {
            if (left!!.isConnected && right!!.isConnected && left.target!!.owner === right.target!!.owner) {
                left!!.reset()
                right!!.reset()
            }
            horizontalBiasPercent = 0.5f
        } else if (anchor === centerY) {
            if (top!!.isConnected && bottom!!.isConnected && top.target!!.owner === bottom.target!!.owner) {
                top!!.reset()
                bottom!!.reset()
            }
            verticalBiasPercent = 0.5f
        } else if (anchor === left || anchor === right) {
            if (left!!.isConnected && left.target === right!!.target) {
                center!!.reset()
            }
        } else if (anchor === top || anchor === bottom) {
            if (top!!.isConnected && top.target === bottom!!.target) {
                center!!.reset()
            }
        }
        anchor.reset()
    }

    /**
     * Reset all connections
     */
    fun resetAnchors() {
        val parent = parent
        if (parent != null && parent is ConstraintWidgetContainer) {
            val parentContainer = this.parent as ConstraintWidgetContainer?
            if (parentContainer!!.handlesInternalConstraints()) {
                return
            }
        }
        var i = 0
        val mAnchorsSize: Int = mAnchors.size
        while (i < mAnchorsSize) {
            val anchor: ConstraintAnchor = mAnchors.get(i)
            anchor.reset()
            i++
        }
    }

    /**
     * Given a type of anchor, returns the corresponding anchor.
     *
     * @param anchorType type of the anchor (LEFT, TOP, RIGHT, BOTTOM, BASELINE, CENTER_X, CENTER_Y)
     * @return the matching anchor
     */
    open fun getAnchor(anchorType: ConstraintAnchor.Type?): ConstraintAnchor? {
        when (anchorType) {
            ConstraintAnchor.Type.LEFT -> {
                return mLeft
            }

            ConstraintAnchor.Type.TOP -> {
                return mTop
            }

            ConstraintAnchor.Type.RIGHT -> {
                return mRight
            }

            ConstraintAnchor.Type.BOTTOM -> {
                return mBottom
            }

            ConstraintAnchor.Type.BASELINE -> {
                return mBaseline
            }

            ConstraintAnchor.Type.CENTER_X -> {
                return mCenterX
            }

            ConstraintAnchor.Type.CENTER_Y -> {
                return mCenterY
            }

            ConstraintAnchor.Type.CENTER -> {
                return mCenter
            }

            ConstraintAnchor.Type.NONE -> return null

            else -> throw AssertionError(anchorType!!.name)
        }
    }
    /**
     * Accessor for the horizontal dimension behaviour
     *
     * @return dimension behaviour
     */
    /**
     * Set the widget's behaviour for the horizontal dimension
     *
     * @param behaviour the horizontal dimension's behaviour
     */
    var horizontalDimensionBehaviour: DimensionBehaviour?
        get() = mListDimensionBehaviors[DIMENSION_HORIZONTAL]
        set(behaviour) {
            mListDimensionBehaviors[DIMENSION_HORIZONTAL] = behaviour
        }
    /**
     * Accessor for the vertical dimension behaviour
     *
     * @return dimension behaviour
     */
    /**
     * Set the widget's behaviour for the vertical dimension
     *
     * @param behaviour the vertical dimension's behaviour
     */
    var verticalDimensionBehaviour: DimensionBehaviour?
        get() = mListDimensionBehaviors[DIMENSION_VERTICAL]
        set(behaviour) {
            mListDimensionBehaviors[DIMENSION_VERTICAL] = behaviour
        }

    /**
     * Get the widget's [DimensionBehaviour] in an specific orientation.
     *
     * @return The [DimensionBehaviour] of the widget.
     */
    fun getDimensionBehaviour(orientation: Int): DimensionBehaviour? {
        return if (orientation == HORIZONTAL) {
            horizontalDimensionBehaviour
        } else if (orientation == VERTICAL) {
            verticalDimensionBehaviour
        } else {
            null
        }
    }

    /**
     * Test if you are in a Horizontal chain
     *
     * @return true if in a horizontal chain
     */
    val isInHorizontalChain: Boolean
        get() = mLeft?.target != null && mLeft?.target?.target === mLeft || mRight?.target != null && mRight?.target?.target === mRight

    /**
     * Return the previous chain member if one exists
     *
     * @param orientation HORIZONTAL or VERTICAL
     * @return the previous chain member or null if we are the first chain element
     */
    fun getPreviousChainMember(orientation: Int): ConstraintWidget? {
        if (orientation == HORIZONTAL) {
            if (mLeft?.target != null && mLeft?.target?.target === mLeft) {
                return mLeft?.target?.owner
            }
        } else if (orientation == VERTICAL) {
            if (mTop?.target != null && mTop?.target?.target === mTop) {
                return mTop?.target?.owner
            }
        }
        return null
    }

    /**
     * Return the next chain member if one exists
     *
     * @param orientation HORIZONTAL or VERTICAL
     * @return the next chain member or null if we are the last chain element
     */
    fun getNextChainMember(orientation: Int): ConstraintWidget? {
        if (orientation == HORIZONTAL) {
            if (mRight?.target != null && mRight?.target?.target === mRight) {
                return mRight?.target?.owner
            }
        } else if (orientation == VERTICAL) {
            if (mBottom?.target != null && mBottom?.target?.target === mBottom) {
                return mBottom?.target?.owner
            }
        }
        return null
    }

    /**
     * if in a horizontal chain return the left most widget in the chain.
     *
     * @return left most widget in chain or null
     */
    val horizontalChainControlWidget: ConstraintWidget?
        get() {
            var found: ConstraintWidget? = null
            if (isInHorizontalChain) {
                var tmp: ConstraintWidget? = this
                while (found == null && tmp != null) {
                    val anchor = tmp.getAnchor(ConstraintAnchor.Type.LEFT)
                    val targetOwner = anchor?.target
                    val target = targetOwner?.owner
                    if (target === parent) {
                        found = tmp
                        break
                    }
                    val targetAnchor = target?.getAnchor(ConstraintAnchor.Type.RIGHT)?.target
                    if (targetAnchor != null && targetAnchor.owner !== tmp) {
                        found = tmp
                    } else {
                        tmp = target
                    }
                }
            }
            return found
        }

    /**
     * Test if you are in a vertical chain
     *
     * @return true if in a vertical chain
     */
    val isInVerticalChain: Boolean
        get() = (mTop?.target != null && mTop?.target?.target === mTop || mBottom?.target != null) && mBottom?.target?.target === mBottom

    /**
     * if in a vertical chain return the top most widget in the chain.
     *
     * @return top most widget in chain or null
     */
    val verticalChainControlWidget: ConstraintWidget?
        get() {
            var found: ConstraintWidget? = null
            if (isInVerticalChain) {
                var tmp: ConstraintWidget? = this
                while (found == null && tmp != null) {
                    val anchor = tmp.getAnchor(ConstraintAnchor.Type.TOP)
                    val targetOwner = anchor?.target
                    val target = targetOwner?.owner
                    if (target === parent) {
                        found = tmp
                        break
                    }
                    val targetAnchor = target?.getAnchor(ConstraintAnchor.Type.BOTTOM)?.target
                    if (targetAnchor != null && targetAnchor.owner !== tmp) {
                        found = tmp
                    } else {
                        tmp = target
                    }
                }
            }
            return found
        }

    /**
     * Determine if the widget is the first element of a chain in a given orientation.
     *
     * @param orientation Either [.HORIZONTAL] or [.VERTICAL]
     * @return if the widget is the head of a chain
     */
    private fun isChainHead(orientation: Int): Boolean {
        val offset = orientation * 2
        return ((mListAnchors[offset]?.target != null
                && mListAnchors[offset]?.target?.target !== mListAnchors[offset])
                && (mListAnchors[offset + 1]?.target != null
                && mListAnchors[offset + 1]?.target?.target === mListAnchors[offset + 1]))
    }
    /*-----------------------------------------------------------------------*/ // Constraints
    /*-----------------------------------------------------------------------*/
    /**
     * Add this widget to the solver
     *
     * @param system   the solver we want to add the widget to
     * @param optimize true if [Optimizer.OPTIMIZATION_GRAPH] is on
     */
    open fun addToSolver(system: LinearSystem, optimize: Boolean) {
        if (LinearSystem.FULL_DEBUG) {
            println("\n----------------------------------------------")
            println("-- adding " + debugName + " to the solver")
            if (isInVirtualLayout) {
                println("-- note: is in virtual layout")
            }
            println("----------------------------------------------\n")
        }
        val left = system.createObjectVariable(mLeft)
        val right = system.createObjectVariable(mRight)
        val top = system.createObjectVariable(mTop)
        val bottom = system.createObjectVariable(mBottom)
        val baseline = system.createObjectVariable(mBaseline)
        var horizontalParentWrapContent = false
        var verticalParentWrapContent = false
        if (parent != null) {
            horizontalParentWrapContent =
                if (parent != null) parent!!.mListDimensionBehaviors[DIMENSION_HORIZONTAL] == DimensionBehaviour.WRAP_CONTENT else false
            verticalParentWrapContent =
                if (parent != null) parent!!.mListDimensionBehaviors[DIMENSION_VERTICAL] == DimensionBehaviour.WRAP_CONTENT else false
            when (mWrapBehaviorInParent) {
                WRAP_BEHAVIOR_SKIPPED -> {
                    horizontalParentWrapContent = false
                    verticalParentWrapContent = false
                }

                WRAP_BEHAVIOR_HORIZONTAL_ONLY -> {
                    verticalParentWrapContent = false
                }

                WRAP_BEHAVIOR_VERTICAL_ONLY -> {
                    horizontalParentWrapContent = false
                }
            }
        }
        if (!(visibility != GONE || isAnimated || hasDependencies()
                    || mIsInBarrier[HORIZONTAL] || mIsInBarrier[VERTICAL])
        ) {
            return
        }
        if (mResolvedHorizontal || mResolvedVertical) {
            if (LinearSystem.FULL_DEBUG) {
                println("\n----------------------------------------------")
                println(
                    "-- setting " + debugName
                            + " to " + mX + ", " + mY + " " + mWidth + " x " + mHeight
                )
                println("----------------------------------------------\n")
            }
            // For now apply all, but that won't work for wrap/wrap layouts.
            if (mResolvedHorizontal) {
                system.addEquality(left!!, mX)
                system.addEquality(right!!, mX + mWidth)
                if (horizontalParentWrapContent && parent != null) {
                    if (mOptimizeWrapOnResolved) {
                        val container = parent as ConstraintWidgetContainer
                        container.addHorizontalWrapMinVariable(mLeft!!)
                        container.addHorizontalWrapMaxVariable(mRight!!)
                    } else {
                        val wrapStrength: Int = SolverVariable.STRENGTH_EQUALITY
                        system.addGreaterThan(
                            system.createObjectVariable(parent!!.mRight)!!,
                            right!!, 0, wrapStrength
                        )
                    }
                }
            }
            if (mResolvedVertical) {
                system.addEquality(top!!, mY)
                system.addEquality(bottom!!, mY + mHeight)
                if (mBaseline!!.hasDependents()) {
                    system.addEquality(baseline!!, mY + mBaselineDistance)
                }
                if (verticalParentWrapContent && parent != null) {
                    if (mOptimizeWrapOnResolved) {
                        val container = parent as ConstraintWidgetContainer
                        container.addVerticalWrapMinVariable(mTop)
                        container.addVerticalWrapMaxVariable(mBottom)
                    } else {
                        val wrapStrength: Int = SolverVariable.STRENGTH_EQUALITY
                        system.addGreaterThan(
                            system.createObjectVariable(parent!!.mBottom)!!,
                            bottom!!, 0, wrapStrength
                        )
                    }
                }
            }
            if (mResolvedHorizontal && mResolvedVertical) {
                mResolvedHorizontal = false
                mResolvedVertical = false
                if (LinearSystem.FULL_DEBUG) {
                    println("\n----------------------------------------------")
                    println("-- setting COMPLETED for " + debugName)
                    println("----------------------------------------------\n")
                }
                return
            }
        }
        if (LinearSystem.sMetrics != null) {
            LinearSystem.sMetrics!!.widgets++
        }
        if (LinearSystem.FULL_DEBUG) {
            if (optimize && mHorizontalRun != null && mVerticalRun != null) {
                println(
                    "-- horizontal run : "
                            + mHorizontalRun!!.start + " : " + mHorizontalRun!!.end
                )
                println(
                    "-- vertical run : "
                            + mVerticalRun!!.start + " : " + mVerticalRun!!.end
                )
            }
        }
        if (optimize && mHorizontalRun != null && mVerticalRun != null && mHorizontalRun!!.start.resolved && mHorizontalRun!!.end.resolved
            && mVerticalRun!!.start.resolved && mVerticalRun!!.end.resolved
        ) {
            if (LinearSystem.sMetrics != null) {
                LinearSystem.sMetrics!!.graphSolved++
            }
            system.addEquality(left!!, mHorizontalRun!!.start.value)
            system.addEquality(right!!, mHorizontalRun!!.end.value)
            system.addEquality(top!!, mVerticalRun!!.start.value)
            system.addEquality(bottom!!, mVerticalRun!!.end.value)
            system.addEquality(baseline!!, mVerticalRun!!.baseline.value)
            if (parent != null) {
                if (horizontalParentWrapContent
                    && isTerminalWidget[HORIZONTAL] && !isInHorizontalChain
                ) {
                    val parentMax = system.createObjectVariable(parent!!.mRight)
                    system.addGreaterThan(parentMax!!, right!!, 0, SolverVariable.STRENGTH_FIXED)
                }
                if (verticalParentWrapContent
                    && isTerminalWidget[VERTICAL] && !isInVerticalChain
                ) {
                    val parentMax = system.createObjectVariable(parent!!.mBottom)
                    system.addGreaterThan(parentMax!!, bottom!!, 0, SolverVariable.STRENGTH_FIXED)
                }
            }
            mResolvedHorizontal = false
            mResolvedVertical = false
            return  // we are done here
        }
        if (LinearSystem.sMetrics != null) {
            LinearSystem.sMetrics!!.linearSolved++
        }
        var inHorizontalChain = false
        var inVerticalChain = false
        if (parent != null) {
            // Add this widget to a horizontal chain if it is the Head of it.
            inHorizontalChain = if (isChainHead(HORIZONTAL)) {
                (parent as ConstraintWidgetContainer).addChain(this, HORIZONTAL)
                true
            } else {
                isInHorizontalChain
            }

            // Add this widget to a vertical chain if it is the Head of it.
            inVerticalChain = if (isChainHead(VERTICAL)) {
                (parent as ConstraintWidgetContainer).addChain(this, VERTICAL)
                true
            } else {
                isInVerticalChain
            }
            if (!inHorizontalChain && horizontalParentWrapContent && visibility != GONE && mLeft?.target == null && mRight?.target == null) {
                if (LinearSystem.FULL_DEBUG) {
                    println("<>1 ADDING H WRAP GREATER FOR " + debugName)
                }
                val parentRight = system.createObjectVariable(parent!!.mRight)
                system.addGreaterThan(parentRight!!, right!!, 0, SolverVariable.STRENGTH_LOW)
            }
            if (!inVerticalChain && verticalParentWrapContent && visibility != GONE && mTop?.target == null && mBottom?.target == null && mBaseline == null) {
                if (LinearSystem.FULL_DEBUG) {
                    println("<>1 ADDING V WRAP GREATER FOR " + debugName)
                }
                val parentBottom = system.createObjectVariable(parent!!.mBottom)
                system.addGreaterThan(parentBottom!!, bottom!!, 0, SolverVariable.STRENGTH_LOW)
            }
        }
        var width = mWidth
        if (width < mMinWidth) {
            width = mMinWidth
        }
        var height = mHeight
        if (height < mMinHeight) {
            height = mMinHeight
        }

        // Dimensions can be either fixed (a given value)
        // or dependent on the solver if set to MATCH_CONSTRAINT
        val horizontalDimensionFixed =
            mListDimensionBehaviors[DIMENSION_HORIZONTAL] != DimensionBehaviour.MATCH_CONSTRAINT
        val verticalDimensionFixed = mListDimensionBehaviors[DIMENSION_VERTICAL] != DimensionBehaviour.MATCH_CONSTRAINT

        // We evaluate the dimension ratio here as the connections can change.
        // TODO: have a validation pass after connection instead
        var useRatio = false
        mResolvedDimensionRatioSide = dimensionRatioSide
        mResolvedDimensionRatio = dimensionRatio
        var matchConstraintDefaultWidth = mMatchConstraintDefaultWidth
        var matchConstraintDefaultHeight = mMatchConstraintDefaultHeight
        if (dimensionRatio > 0 && visibility != GONE) {
            useRatio = true
            if (mListDimensionBehaviors[DIMENSION_HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT
                && matchConstraintDefaultWidth == MATCH_CONSTRAINT_SPREAD
            ) {
                matchConstraintDefaultWidth = MATCH_CONSTRAINT_RATIO
            }
            if (mListDimensionBehaviors[DIMENSION_VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT
                && matchConstraintDefaultHeight == MATCH_CONSTRAINT_SPREAD
            ) {
                matchConstraintDefaultHeight = MATCH_CONSTRAINT_RATIO
            }
            if (mListDimensionBehaviors[DIMENSION_HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT && mListDimensionBehaviors[DIMENSION_VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT && matchConstraintDefaultWidth == MATCH_CONSTRAINT_RATIO && matchConstraintDefaultHeight == MATCH_CONSTRAINT_RATIO) {
                setupDimensionRatio(
                    horizontalParentWrapContent, verticalParentWrapContent,
                    horizontalDimensionFixed, verticalDimensionFixed
                )
            } else if (mListDimensionBehaviors[DIMENSION_HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT
                && matchConstraintDefaultWidth == MATCH_CONSTRAINT_RATIO
            ) {
                mResolvedDimensionRatioSide = HORIZONTAL
                width = (mResolvedDimensionRatio * mHeight).toInt()
                if (mListDimensionBehaviors[DIMENSION_VERTICAL] != DimensionBehaviour.MATCH_CONSTRAINT) {
                    matchConstraintDefaultWidth = MATCH_CONSTRAINT_RATIO_RESOLVED
                    useRatio = false
                }
            } else if (mListDimensionBehaviors[DIMENSION_VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT
                && matchConstraintDefaultHeight == MATCH_CONSTRAINT_RATIO
            ) {
                mResolvedDimensionRatioSide = VERTICAL
                if (dimensionRatioSide == UNKNOWN) {
                    // need to reverse the ratio as the parsing is done in horizontal mode
                    mResolvedDimensionRatio = 1 / mResolvedDimensionRatio
                }
                height = (mResolvedDimensionRatio * mWidth).toInt()
                if (mListDimensionBehaviors[DIMENSION_HORIZONTAL] != DimensionBehaviour.MATCH_CONSTRAINT) {
                    matchConstraintDefaultHeight = MATCH_CONSTRAINT_RATIO_RESOLVED
                    useRatio = false
                }
            }
        }
        mResolvedMatchConstraintDefault[HORIZONTAL] = matchConstraintDefaultWidth
        mResolvedMatchConstraintDefault[VERTICAL] = matchConstraintDefaultHeight
        mResolvedHasRatio = useRatio
        val useHorizontalRatio = useRatio && (mResolvedDimensionRatioSide == HORIZONTAL
                || mResolvedDimensionRatioSide == UNKNOWN)
        val useVerticalRatio = useRatio && (mResolvedDimensionRatioSide == VERTICAL
                || mResolvedDimensionRatioSide == UNKNOWN)

        // Horizontal resolution
        var wrapContent = (mListDimensionBehaviors[DIMENSION_HORIZONTAL] == DimensionBehaviour.WRAP_CONTENT
                && this is ConstraintWidgetContainer)
        if (wrapContent) {
            width = 0
        }
        var applyPosition = true
        if (mCenter.isConnected) {
            applyPosition = false
        }
        val isInHorizontalBarrier = mIsInBarrier[HORIZONTAL]
        val isInVerticalBarrier = mIsInBarrier[VERTICAL]
        if (mHorizontalResolution != DIRECT && !mResolvedHorizontal) {
            if (!optimize || !(mHorizontalRun != null && mHorizontalRun!!.start.resolved && mHorizontalRun!!.end.resolved)) {
                val parentMax = if (parent != null) system.createObjectVariable(parent!!.mRight) else null
                val parentMin = if (parent != null) system.createObjectVariable(parent!!.mLeft) else null
                applyConstraints(
                    system, true, horizontalParentWrapContent,
                    verticalParentWrapContent, isTerminalWidget[HORIZONTAL], parentMin,
                    parentMax, mListDimensionBehaviors[DIMENSION_HORIZONTAL], wrapContent,
                    mLeft, mRight, mX, width,
                    mMinWidth, mMaxDimension[HORIZONTAL],
                    horizontalBiasPercent, useHorizontalRatio,
                    mListDimensionBehaviors[VERTICAL] == DimensionBehaviour.MATCH_CONSTRAINT,
                    inHorizontalChain, inVerticalChain, isInHorizontalBarrier,
                    matchConstraintDefaultWidth, matchConstraintDefaultHeight,
                    mMatchConstraintMinWidth, mMatchConstraintMaxWidth,
                    mMatchConstraintPercentWidth, applyPosition
                )
            } else if (optimize) {
                system.addEquality(left!!, mHorizontalRun!!.start.value)
                system.addEquality(right!!, mHorizontalRun!!.end.value)
                if (parent != null) {
                    if (horizontalParentWrapContent
                        && isTerminalWidget[HORIZONTAL] && !isInHorizontalChain
                    ) {
                        if (LinearSystem.FULL_DEBUG) {
                            println("<>2 ADDING H WRAP GREATER FOR " + debugName)
                        }
                        val parentMax = system.createObjectVariable(parent!!.mRight)
                        system.addGreaterThan(parentMax!!, right!!, 0, SolverVariable.STRENGTH_FIXED)
                    }
                }
            }
        }
        var applyVerticalConstraints = true
        if (optimize && mVerticalRun != null && mVerticalRun!!.start.resolved && mVerticalRun!!.end.resolved) {
            system.addEquality(top!!, mVerticalRun!!.start.value)
            system.addEquality(bottom!!, mVerticalRun!!.end.value)
            system.addEquality(baseline!!, mVerticalRun!!.baseline.value)
            if (parent != null) {
                if (!inVerticalChain && verticalParentWrapContent && isTerminalWidget[VERTICAL]) {
                    if (LinearSystem.FULL_DEBUG) {
                        println("<>2 ADDING V WRAP GREATER FOR " + debugName)
                    }
                    val parentMax = system.createObjectVariable(parent!!.mBottom)
                    system.addGreaterThan(parentMax!!, bottom!!, 0, SolverVariable.STRENGTH_FIXED)
                }
            }
            applyVerticalConstraints = false
        }
        if (mVerticalResolution == DIRECT) {
            if (LinearSystem.FULL_DEBUG) {
                println("\n----------------------------------------------")
                println("-- DONE adding " + debugName + " to the solver")
                println("-- SKIP VERTICAL RESOLUTION")
                println("----------------------------------------------\n")
            }
            applyVerticalConstraints = false
        }
        if (applyVerticalConstraints && !mResolvedVertical) {
            // Vertical Resolution
            wrapContent = (mListDimensionBehaviors[DIMENSION_VERTICAL] == DimensionBehaviour.WRAP_CONTENT
                    && this is ConstraintWidgetContainer)
            if (wrapContent) {
                height = 0
            }
            val parentMax = if (parent != null) system.createObjectVariable(parent!!.mBottom) else null
            val parentMin = if (parent != null) system.createObjectVariable(parent!!.mTop) else null
            if (mBaselineDistance > 0 || visibility == GONE) {
                // if we are GONE we might still have to deal with baseline,
                // even if our baseline distance would be zero
                if (mBaseline?.target != null) {
                    system.addEquality(
                        baseline!!, top!!, baselineDistance,
                        SolverVariable.STRENGTH_FIXED
                    )
                    val baselineTarget = system.createObjectVariable(mBaseline?.target)
                    val baselineMargin = mBaseline!!.margin
                    system.addEquality(
                        baseline!!, baselineTarget!!, baselineMargin,
                        SolverVariable.STRENGTH_FIXED
                    )
                    applyPosition = false
                    if (verticalParentWrapContent) {
                        if (LinearSystem.FULL_DEBUG) {
                            println("<>3 ADDING V WRAP GREATER FOR " + debugName)
                        }
                        val end = system.createObjectVariable(mBottom)
                        val wrapStrength: Int = SolverVariable.STRENGTH_EQUALITY
                        system.addGreaterThan(parentMax!!, end!!, 0, wrapStrength)
                    }
                } else if (visibility == GONE) {
                    // TODO: use the constraints graph here to help
                    system.addEquality(
                        baseline!!, top!!, mBaseline!!.margin,
                        SolverVariable.STRENGTH_FIXED
                    )
                } else {
                    system.addEquality(
                        baseline!!, top!!, baselineDistance,
                        SolverVariable.STRENGTH_FIXED
                    )
                }
            }
            applyConstraints(
                system, false, verticalParentWrapContent,
                horizontalParentWrapContent, isTerminalWidget[VERTICAL], parentMin,
                parentMax, mListDimensionBehaviors[DIMENSION_VERTICAL],
                wrapContent, mTop, mBottom, mY, height,
                mMinHeight, mMaxDimension[VERTICAL], verticalBiasPercent, useVerticalRatio,
                mListDimensionBehaviors[HORIZONTAL] == DimensionBehaviour.MATCH_CONSTRAINT,
                inVerticalChain, inHorizontalChain, isInVerticalBarrier,
                matchConstraintDefaultHeight, matchConstraintDefaultWidth,
                mMatchConstraintMinHeight, mMatchConstraintMaxHeight,
                mMatchConstraintPercentHeight, applyPosition
            )
        }
        if (useRatio) {
            val strength: Int = SolverVariable.STRENGTH_FIXED
            if (mResolvedDimensionRatioSide == VERTICAL) {
                system.addRatio(bottom!!, top!!, right!!, left!!, mResolvedDimensionRatio, strength)
            } else {
                system.addRatio(right!!, left!!, bottom!!, top!!, mResolvedDimensionRatio, strength)
            }
        }
        if (mCenter.isConnected) {
            system.addCenterPoint(
                this,
                mCenter.target!!.owner,
                ((mCircleConstraintAngle + 90).toDouble() * PI / 180.0).toFloat(),
                mCenter.margin
            )
        }
        if (LinearSystem.FULL_DEBUG) {
            println("\n----------------------------------------------")
            println("-- DONE adding " + debugName + " to the solver")
            println("----------------------------------------------\n")
        }
        mResolvedHorizontal = false
        mResolvedVertical = false
    }

    /**
     * Used to select which widgets should be added to the solver first
     */
    fun addFirst(): Boolean {
        return this is VirtualLayout || this is Guideline
    }

    /**
     * Resolves the dimension ratio parameters
     * (mResolvedDimensionRatioSide & mDimensionRatio)
     *
     * @param hParentWrapContent       true if parent is in wrap content horizontally
     * @param vParentWrapContent       true if parent is in wrap content vertically
     * @param horizontalDimensionFixed true if this widget horizontal dimension is fixed
     * @param verticalDimensionFixed   true if this widget vertical dimension is fixed
     */
    fun setupDimensionRatio(
        hParentWrapContent: Boolean,
        vParentWrapContent: Boolean,
        horizontalDimensionFixed: Boolean,
        verticalDimensionFixed: Boolean
    ) {
        if (mResolvedDimensionRatioSide == UNKNOWN) {
            if (horizontalDimensionFixed && !verticalDimensionFixed) {
                mResolvedDimensionRatioSide = HORIZONTAL
            } else if (!horizontalDimensionFixed && verticalDimensionFixed) {
                mResolvedDimensionRatioSide = VERTICAL
                if (dimensionRatioSide == UNKNOWN) {
                    // need to reverse the ratio as the parsing is done in horizontal mode
                    mResolvedDimensionRatio = 1 / mResolvedDimensionRatio
                }
            }
        }
        if (mResolvedDimensionRatioSide == HORIZONTAL
            && !(mTop.isConnected && mBottom.isConnected)
        ) {
            mResolvedDimensionRatioSide = VERTICAL
        } else if (mResolvedDimensionRatioSide == VERTICAL
            && !(mLeft!!.isConnected && mRight!!.isConnected)
        ) {
            mResolvedDimensionRatioSide = HORIZONTAL
        }

        // if dimension is still unknown... check parentWrap
        if (mResolvedDimensionRatioSide == UNKNOWN) {
            if (!(mTop.isConnected && mBottom.isConnected
                        && mLeft!!.isConnected && mRight!!.isConnected)
            ) {
                // only do that if not all connections are set
                if (mTop.isConnected && mBottom.isConnected) {
                    mResolvedDimensionRatioSide = HORIZONTAL
                } else if (mLeft!!.isConnected && mRight!!.isConnected) {
                    mResolvedDimensionRatio = 1 / mResolvedDimensionRatio
                    mResolvedDimensionRatioSide = VERTICAL
                }
            }
        }
        if (DO_NOT_USE && mResolvedDimensionRatioSide == UNKNOWN) {
            if (hParentWrapContent && !vParentWrapContent) {
                mResolvedDimensionRatioSide = HORIZONTAL
            } else if (!hParentWrapContent && vParentWrapContent) {
                mResolvedDimensionRatio = 1 / mResolvedDimensionRatio
                mResolvedDimensionRatioSide = VERTICAL
            }
        }
        if (mResolvedDimensionRatioSide == UNKNOWN) {
            if (mMatchConstraintMinWidth > 0 && mMatchConstraintMinHeight == 0) {
                mResolvedDimensionRatioSide = HORIZONTAL
            } else if (mMatchConstraintMinWidth == 0 && mMatchConstraintMinHeight > 0) {
                mResolvedDimensionRatio = 1 / mResolvedDimensionRatio
                mResolvedDimensionRatioSide = VERTICAL
            }
        }
        if (DO_NOT_USE && mResolvedDimensionRatioSide == UNKNOWN && hParentWrapContent && vParentWrapContent) {
            mResolvedDimensionRatio = 1 / mResolvedDimensionRatio
            mResolvedDimensionRatioSide = VERTICAL
        }
    }

    /**
     * Apply the constraints in the system depending on the existing anchors, in one dimension
     *
     * @param system                the linear system we are adding constraints to
     * @param wrapContent           is the widget trying to wrap its content
     * (i.e. its size will depends on its content)
     * @param beginAnchor           the first anchor
     * @param endAnchor             the second anchor
     * @param beginPosition         the original position of the anchor
     * @param dimension             the dimension
     * @param matchPercentDimension the percentage relative to the parent,
     * applied if in match constraint and percent mode
     */
    private fun applyConstraints(
        system: LinearSystem, isHorizontal: Boolean,
        parentWrapContent: Boolean, oppositeParentWrapContent: Boolean,
        isTerminal: Boolean, parentMin: SolverVariable?,
        parentMax: SolverVariable?,
        dimensionBehaviour: DimensionBehaviour?, wrapContent: Boolean,
        beginAnchor: ConstraintAnchor?, endAnchor: ConstraintAnchor?,
        beginPosition: Int, dimension: Int, minDimension: Int,
        maxDimension: Int, bias: Float, useRatio: Boolean,
        oppositeVariable: Boolean, inChain: Boolean,
        oppositeInChain: Boolean, inBarrier: Boolean,
        matchConstraintDefault: Int,
        oppositeMatchConstraintDefault: Int,
        matchMinDimension: Int, matchMaxDimension: Int,
        matchPercentDimension: Float, applyPosition: Boolean
    ) {
        var parentWrapContent = parentWrapContent
        var isTerminal = isTerminal
        var dimension = dimension
        var matchConstraintDefault = matchConstraintDefault
        var matchMinDimension = matchMinDimension
        var matchMaxDimension = matchMaxDimension
        val begin = system.createObjectVariable(beginAnchor)
        val end = system.createObjectVariable(endAnchor)
        val beginTarget = system.createObjectVariable(beginAnchor!!.target)
        val endTarget = system.createObjectVariable(endAnchor!!.target)
        if (LinearSystem.sMetrics != null) {
            LinearSystem.sMetrics!!.nonresolvedWidgets++
        }
        val isBeginConnected = beginAnchor!!.isConnected
        val isEndConnected = endAnchor!!.isConnected
        val isCenterConnected = mCenter.isConnected
        var variableSize = false
        var numConnections = 0
        if (isBeginConnected) {
            numConnections++
        }
        if (isEndConnected) {
            numConnections++
        }
        if (isCenterConnected) {
            numConnections++
        }
        if (useRatio) {
            matchConstraintDefault = MATCH_CONSTRAINT_RATIO
        }
        when (dimensionBehaviour) {
            DimensionBehaviour.FIXED -> {
                variableSize = false
            }

            DimensionBehaviour.WRAP_CONTENT -> {
                variableSize = false
            }

            DimensionBehaviour.MATCH_PARENT -> {
                variableSize = false
            }

            DimensionBehaviour.MATCH_CONSTRAINT -> {
                variableSize = matchConstraintDefault != MATCH_CONSTRAINT_RATIO_RESOLVED
            }
            else -> {}
        }
        if (mWidthOverride != -1 && isHorizontal) {
            if (LinearSystem.FULL_DEBUG) {
                println("OVERRIDE WIDTH to $mWidthOverride")
            }
            variableSize = false
            dimension = mWidthOverride
            mWidthOverride = -1
        }
        if (mHeightOverride != -1 && !isHorizontal) {
            if (LinearSystem.FULL_DEBUG) {
                println("OVERRIDE HEIGHT to $mHeightOverride")
            }
            variableSize = false
            dimension = mHeightOverride
            mHeightOverride = -1
        }
        if (visibility == GONE) {
            dimension = 0
            variableSize = false
        }

        // First apply starting direct connections (more solver-friendly)
        if (applyPosition) {
            if (!isBeginConnected && !isEndConnected && !isCenterConnected) {
                system.addEquality(begin!!, beginPosition)
            } else if (isBeginConnected && !isEndConnected) {
                system.addEquality(
                    begin!!, beginTarget!!,
                    beginAnchor.margin, SolverVariable.STRENGTH_FIXED
                )
            }
        }

        // Then apply the dimension
        if (!variableSize) {
            if (wrapContent) {
                system.addEquality(end!!, begin!!, 0, SolverVariable.STRENGTH_HIGH)
                if (minDimension > 0) {
                    system.addGreaterThan(end!!, begin!!, minDimension, SolverVariable.STRENGTH_FIXED)
                }
                if (maxDimension < Int.MAX_VALUE) {
                    system.addLowerThan(end!!, begin!!, maxDimension, SolverVariable.STRENGTH_FIXED)
                }
            } else {
                system.addEquality(end!!, begin!!, dimension, SolverVariable.STRENGTH_FIXED)
            }
        } else {
            if (numConnections != 2 && !useRatio && (matchConstraintDefault == MATCH_CONSTRAINT_WRAP || matchConstraintDefault == MATCH_CONSTRAINT_SPREAD)) {
                variableSize = false
                var d: Int = max(matchMinDimension, dimension)
                if (matchMaxDimension > 0) {
                    d = min(matchMaxDimension, d)
                }
                system.addEquality(end!!, begin!!, d, SolverVariable.STRENGTH_FIXED)
            } else {
                if (matchMinDimension == WRAP) {
                    matchMinDimension = dimension
                }
                if (matchMaxDimension == WRAP) {
                    matchMaxDimension = dimension
                }
                if (dimension > 0
                    && matchConstraintDefault != MATCH_CONSTRAINT_WRAP
                ) {
                    if (USE_WRAP_DIMENSION_FOR_SPREAD && matchConstraintDefault == MATCH_CONSTRAINT_SPREAD) {
                        system.addGreaterThan(
                            end!!, begin!!, dimension,
                            SolverVariable.STRENGTH_HIGHEST
                        )
                    }
                    dimension = 0
                }
                if (matchMinDimension > 0) {
                    system.addGreaterThan(
                        end!!, begin!!, matchMinDimension,
                        SolverVariable.STRENGTH_FIXED
                    )
                    dimension = max(dimension, matchMinDimension)
                }
                if (matchMaxDimension > 0) {
                    var applyLimit = true
                    if (parentWrapContent && matchConstraintDefault == MATCH_CONSTRAINT_WRAP) {
                        applyLimit = false
                    }
                    if (applyLimit) {
                        system.addLowerThan(
                            end!!, begin!!,
                            matchMaxDimension, SolverVariable.STRENGTH_FIXED
                        )
                    }
                    dimension = min(dimension, matchMaxDimension)
                }
                if (matchConstraintDefault == MATCH_CONSTRAINT_WRAP) {
                    if (parentWrapContent) {
                        system.addEquality(end!!, begin!!, dimension, SolverVariable.STRENGTH_FIXED)
                    } else if (inChain) {
                        system.addEquality(end!!, begin!!, dimension, SolverVariable.STRENGTH_EQUALITY)
                        system.addLowerThan(end!!, begin!!, dimension, SolverVariable.STRENGTH_FIXED)
                    } else {
                        system.addEquality(end!!, begin!!, dimension, SolverVariable.STRENGTH_EQUALITY)
                        system.addLowerThan(end!!, begin!!, dimension, SolverVariable.STRENGTH_FIXED)
                    }
                } else if (matchConstraintDefault == MATCH_CONSTRAINT_PERCENT) {
                    var percentBegin: SolverVariable? = null
                    var percentEnd: SolverVariable? = null
                    if (beginAnchor.type == ConstraintAnchor.Type.TOP
                        || beginAnchor.type == ConstraintAnchor.Type.BOTTOM
                    ) {
                        // vertical
                        percentBegin = system.createObjectVariable(
                            parent!!.getAnchor(ConstraintAnchor.Type.TOP)
                        )
                        percentEnd = system.createObjectVariable(
                            parent!!.getAnchor(ConstraintAnchor.Type.BOTTOM)
                        )
                    } else {
                        percentBegin = system.createObjectVariable(
                            parent!!.getAnchor(ConstraintAnchor.Type.LEFT)
                        )
                        percentEnd = system.createObjectVariable(
                            parent!!.getAnchor(ConstraintAnchor.Type.RIGHT)
                        )
                    }
                    system.addConstraint(
                        system.createRow().createRowDimensionRatio(
                            end!!,
                            begin!!, percentEnd!!, percentBegin!!, matchPercentDimension
                        )
                    )
                    if (parentWrapContent) {
                        variableSize = false
                    }
                } else {
                    isTerminal = true
                }
            }
        }
        if (!applyPosition || inChain) {
            // If we don't need to apply the position, let's finish now.
            if (LinearSystem.FULL_DEBUG) {
                println(
                    "only deal with dimension for " + debugName
                            + ", not positioning (applyPosition: "
                            + applyPosition + " inChain: " + inChain + ")"
                )
            }
            if (numConnections < 2 && parentWrapContent && isTerminal) {
                system.addGreaterThan(begin!!, parentMin!!, 0, SolverVariable.STRENGTH_FIXED)
                var applyEnd = isHorizontal || mBaseline?.target == null
                if (!isHorizontal && mBaseline?.target != null) {
                    // generally we wouldn't take the current widget in the wrap content,
                    // but if the connected element is a ratio widget,
                    // then we can contribute (as the ratio widget may not be enough by itself)
                    // to it.
                    val target = mBaseline?.target!!.owner
                    applyEnd =
                        target.dimensionRatio != 0f && target.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && target.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT
                }
                if (applyEnd) {
                    if (LinearSystem.FULL_DEBUG) {
                        println("<>4 ADDING WRAP GREATER FOR " + debugName)
                    }
                    system.addGreaterThan(parentMax!!, end!!, 0, SolverVariable.STRENGTH_FIXED)
                }
            }
            return
        }

        // Ok, we are dealing with single or centered constraints, let's apply them
        var wrapStrength: Int = SolverVariable.STRENGTH_EQUALITY
        if (!isBeginConnected && !isEndConnected && !isCenterConnected) {
            // note we already applied the start position before, no need to redo it...
        } else if (isBeginConnected && !isEndConnected) {
            // note we already applied the start position before, no need to redo it...

            // If we are constrained to a barrier, make sure that we are not bypassed in the wrap
            val beginWidget = beginAnchor?.target?.owner
            if (parentWrapContent && beginWidget is Barrier) {
                wrapStrength = SolverVariable.STRENGTH_FIXED
            }
        } else if (!isBeginConnected && isEndConnected) {
            system.addEquality(
                end!!, endTarget!!,
                -endAnchor.margin, SolverVariable.STRENGTH_FIXED
            )
            if (parentWrapContent) {
                if (mOptimizeWrapO && begin!!.isFinalValue && parent != null) {
                    val container = parent as ConstraintWidgetContainer
                    if (isHorizontal) {
                        container.addHorizontalWrapMinVariable(beginAnchor)
                    } else {
                        container.addVerticalWrapMinVariable(beginAnchor)
                    }
                } else {
                    if (LinearSystem.FULL_DEBUG) {
                        println("<>5 ADDING WRAP GREATER FOR " + debugName)
                    }
                    system.addGreaterThan(begin!!, parentMin!!, 0, SolverVariable.STRENGTH_EQUALITY)
                }
            }
        } else if (isBeginConnected && isEndConnected) {
            var applyBoundsCheck = true
            var applyCentering = false
            var applyStrongChecks = false
            var applyRangeCheck = false
            var rangeCheckStrength: Int = SolverVariable.STRENGTH_EQUALITY

            // TODO: might not need it here (it's overridden)
            var boundsCheckStrength: Int = SolverVariable.STRENGTH_HIGHEST
            var centeringStrength: Int = SolverVariable.STRENGTH_BARRIER
            if (parentWrapContent) {
                rangeCheckStrength = SolverVariable.STRENGTH_EQUALITY
            }
            val beginWidget: ConstraintWidget = beginAnchor?.target!!.owner
            val endWidget: ConstraintWidget = endAnchor?.target!!.owner
            val parent = parent
            if (variableSize) {
                if (matchConstraintDefault == MATCH_CONSTRAINT_SPREAD) {
                    if (matchMaxDimension == 0 && matchMinDimension == 0) {
                        applyStrongChecks = true
                        rangeCheckStrength = SolverVariable.STRENGTH_FIXED
                        boundsCheckStrength = SolverVariable.STRENGTH_FIXED
                        // Optimization in case of centering in parent
                        if (beginTarget!!.isFinalValue && endTarget!!.isFinalValue) {
                            system.addEquality(
                                begin!!, beginTarget!!,
                                beginAnchor.margin, SolverVariable.STRENGTH_FIXED
                            )
                            system.addEquality(
                                end!!, endTarget!!,
                                -endAnchor.margin, SolverVariable.STRENGTH_FIXED
                            )
                            return
                        }
                    } else {
                        applyCentering = true
                        rangeCheckStrength = SolverVariable.STRENGTH_EQUALITY
                        boundsCheckStrength = SolverVariable.STRENGTH_EQUALITY
                        applyBoundsCheck = true
                        applyRangeCheck = true
                    }
                    if (beginWidget is Barrier || endWidget is Barrier) {
                        boundsCheckStrength = SolverVariable.STRENGTH_HIGHEST
                    }
                } else if (matchConstraintDefault == MATCH_CONSTRAINT_PERCENT) {
                    applyCentering = true
                    rangeCheckStrength = SolverVariable.STRENGTH_EQUALITY
                    boundsCheckStrength = SolverVariable.STRENGTH_EQUALITY
                    applyBoundsCheck = true
                    applyRangeCheck = true
                    if (beginWidget is Barrier || endWidget is Barrier) {
                        boundsCheckStrength = SolverVariable.STRENGTH_HIGHEST
                    }
                } else if (matchConstraintDefault == MATCH_CONSTRAINT_WRAP) {
                    applyCentering = true
                    applyRangeCheck = true
                    rangeCheckStrength = SolverVariable.STRENGTH_FIXED
                } else if (matchConstraintDefault == MATCH_CONSTRAINT_RATIO) {
                    if (mResolvedDimensionRatioSide == UNKNOWN) {
                        applyCentering = true
                        applyRangeCheck = true
                        applyStrongChecks = true
                        rangeCheckStrength = SolverVariable.STRENGTH_FIXED
                        boundsCheckStrength = SolverVariable.STRENGTH_EQUALITY
                        if (oppositeInChain) {
                            boundsCheckStrength = SolverVariable.STRENGTH_EQUALITY
                            centeringStrength = SolverVariable.STRENGTH_HIGHEST
                            if (parentWrapContent) {
                                centeringStrength = SolverVariable.STRENGTH_EQUALITY
                            }
                        } else {
                            centeringStrength = SolverVariable.STRENGTH_FIXED
                        }
                    } else {
                        applyCentering = true
                        applyRangeCheck = true
                        applyStrongChecks = true
                        if (useRatio) {
                            // useRatio is true
                            // if the side we base ourselves on for the ratio is this one
                            // if that's not the case, we need to have a stronger constraint.
                            val otherSideInvariable = (oppositeMatchConstraintDefault == MATCH_CONSTRAINT_PERCENT
                                    || oppositeMatchConstraintDefault
                                    == MATCH_CONSTRAINT_WRAP)
                            if (!otherSideInvariable) {
                                rangeCheckStrength = SolverVariable.STRENGTH_FIXED
                                boundsCheckStrength = SolverVariable.STRENGTH_EQUALITY
                            }
                        } else {
                            rangeCheckStrength = SolverVariable.STRENGTH_EQUALITY
                            if (matchMaxDimension > 0) {
                                boundsCheckStrength = SolverVariable.STRENGTH_EQUALITY
                            } else if (matchMaxDimension == 0 && matchMinDimension == 0) {
                                if (!oppositeInChain) {
                                    boundsCheckStrength = SolverVariable.STRENGTH_FIXED
                                } else {
                                    rangeCheckStrength = if (beginWidget !== parent && endWidget !== parent) {
                                        SolverVariable.STRENGTH_HIGHEST
                                    } else {
                                        SolverVariable.STRENGTH_EQUALITY
                                    }
                                    boundsCheckStrength = SolverVariable.STRENGTH_HIGHEST
                                }
                            }
                        }
                    }
                }
            } else {
                applyCentering = true
                applyRangeCheck = true

                // Let's optimize away if we can...
                if (beginTarget!!.isFinalValue && endTarget!!.isFinalValue) {
                    system.addCentering(
                        begin!!, beginTarget!!, beginAnchor.margin,
                        bias, endTarget!!, end!!, endAnchor.margin,
                        SolverVariable.STRENGTH_FIXED
                    )
                    if (parentWrapContent && isTerminal) {
                        var margin = 0
                        if (endAnchor?.target != null) {
                            margin = endAnchor.margin
                        }
                        if (endTarget !== parentMax) { // if not already applied
                            if (LinearSystem.FULL_DEBUG) {
                                println("<>6 ADDING WRAP GREATER FOR " + debugName)
                            }
                            system.addGreaterThan(parentMax!!, end, margin, wrapStrength)
                        }
                    }
                    return
                }
            }
            if (applyRangeCheck && beginTarget === endTarget && beginWidget !== parent) {
                // no need to apply range / bounds check if we are centered on the same anchor
                applyRangeCheck = false
                applyBoundsCheck = false
            }
            if (applyCentering) {
                if (!variableSize && !oppositeVariable && !oppositeInChain && beginTarget === parentMin && endTarget === parentMax) {
                    // for fixed size widgets, we can simplify the constraints
                    centeringStrength = SolverVariable.STRENGTH_FIXED
                    rangeCheckStrength = SolverVariable.STRENGTH_FIXED
                    applyBoundsCheck = false
                    parentWrapContent = false
                }
                system.addCentering(
                    begin!!, beginTarget!!, beginAnchor.margin,
                    bias, endTarget!!, end!!, endAnchor.margin, centeringStrength
                )
            }
            if (visibility == GONE && !endAnchor.hasDependents()) {
                return
            }
            if (applyRangeCheck) {
                if (parentWrapContent && beginTarget !== endTarget && !variableSize) {
                    if (beginWidget is Barrier || endWidget is Barrier) {
                        rangeCheckStrength = SolverVariable.STRENGTH_BARRIER
                    }
                }
                system.addGreaterThan(
                    begin!!, beginTarget!!,
                    beginAnchor.margin, rangeCheckStrength
                )
                system.addLowerThan(end!!, endTarget!!, -endAnchor.margin, rangeCheckStrength)
            }
            if (parentWrapContent && inBarrier // if we are referenced by a barrier
                && !(beginWidget is Barrier || endWidget is Barrier)
                && !(endWidget === parent)
            ) {
                // ... but not directly constrained by it
                // ... then make sure we can hold our own
                boundsCheckStrength = SolverVariable.STRENGTH_BARRIER
                rangeCheckStrength = SolverVariable.STRENGTH_BARRIER
                applyBoundsCheck = true
            }
            if (applyBoundsCheck) {
                if (applyStrongChecks && (!oppositeInChain || oppositeParentWrapContent)) {
                    var strength = boundsCheckStrength
                    if (beginWidget === parent || endWidget === parent) {
                        strength = SolverVariable.STRENGTH_BARRIER
                    }
                    if (beginWidget is Guideline || endWidget is Guideline) {
                        strength = SolverVariable.STRENGTH_EQUALITY
                    }
                    if (beginWidget is Barrier || endWidget is Barrier) {
                        strength = SolverVariable.STRENGTH_EQUALITY
                    }
                    if (oppositeInChain) {
                        strength = SolverVariable.STRENGTH_EQUALITY
                    }
                    boundsCheckStrength = max(strength, boundsCheckStrength)
                }
                if (parentWrapContent) {
                    boundsCheckStrength = min(rangeCheckStrength, boundsCheckStrength)
                    if (useRatio && !oppositeInChain
                        && (beginWidget === parent || endWidget === parent)
                    ) {
                        // When using ratio, relax some strength to allow other parts of the system
                        // to take precedence rather than driving it
                        boundsCheckStrength = SolverVariable.STRENGTH_HIGHEST
                    }
                }
                system.addEquality(
                    begin!!, beginTarget!!,
                    beginAnchor.margin, boundsCheckStrength
                )
                system.addEquality(end!!, endTarget!!, -endAnchor.margin, boundsCheckStrength)
            }
            if (parentWrapContent) {
                var margin = 0
                if (parentMin === beginTarget) {
                    margin = beginAnchor.margin
                }
                if (beginTarget !== parentMin) { // already done otherwise
                    if (LinearSystem.FULL_DEBUG) {
                        println("<>7 ADDING WRAP GREATER FOR " + debugName)
                    }
                    system.addGreaterThan(begin!!, parentMin!!, margin, wrapStrength)
                }
            }
            if (parentWrapContent && variableSize && minDimension == 0 && matchMinDimension == 0) {
                if (LinearSystem.FULL_DEBUG) {
                    println("<>8 ADDING WRAP GREATER FOR " + debugName)
                }
                if (variableSize && matchConstraintDefault == MATCH_CONSTRAINT_RATIO) {
                    system.addGreaterThan(end!!, begin!!, 0, SolverVariable.STRENGTH_FIXED)
                } else {
                    system.addGreaterThan(end!!, begin!!, 0, wrapStrength)
                }
            }
        }
        if (parentWrapContent && isTerminal) {
            var margin = 0
            if (endAnchor?.target != null) {
                margin = endAnchor.margin
            }
            if (endTarget !== parentMax) { // if not already applied
                if (mOptimizeWrapO && end!!.isFinalValue && parent != null) {
                    val container = parent as ConstraintWidgetContainer
                    if (isHorizontal) {
                        container.addHorizontalWrapMaxVariable(endAnchor)
                    } else {
                        container.addVerticalWrapMaxVariable(endAnchor)
                    }
                    return
                }
                if (LinearSystem.FULL_DEBUG) {
                    println("<>9 ADDING WRAP GREATER FOR " + debugName)
                }
                system.addGreaterThan(parentMax!!, end!!, margin, wrapStrength)
            }
        }
    }

    /**
     * Update the widget from the values generated by the solver
     *
     * @param system   the solver we get the values from.
     * @param optimize true if [Optimizer.OPTIMIZATION_GRAPH] is on
     */
    open fun updateFromSolver(system: LinearSystem, optimize: Boolean) {
        var left = system.getObjectVariableValue(mLeft!!)
        var top = system.getObjectVariableValue(mTop)
        var right = system.getObjectVariableValue(mRight!!)
        var bottom = system.getObjectVariableValue(mBottom)
        if (optimize && mHorizontalRun != null && mHorizontalRun!!.start.resolved && mHorizontalRun!!.end.resolved) {
            left = mHorizontalRun!!.start.value
            right = mHorizontalRun!!.end.value
        }
        if (optimize && mVerticalRun != null && mVerticalRun!!.start.resolved && mVerticalRun!!.end.resolved) {
            top = mVerticalRun!!.start.value
            bottom = mVerticalRun!!.end.value
        }
        val w = right - left
        val h = bottom - top
        if (w < 0 || h < 0 || left == Int.MIN_VALUE || left == Int.MAX_VALUE || top == Int.MIN_VALUE || top == Int.MAX_VALUE || right == Int.MIN_VALUE || right == Int.MAX_VALUE || bottom == Int.MIN_VALUE || bottom == Int.MAX_VALUE) {
            left = 0
            top = 0
            right = 0
            bottom = 0
        }
        setFrame(left, top, right, bottom)
        if (LinearSystem.DEBUG) {
            println(" *** UPDATE FROM SOLVER $this")
        }
    }

    /**
     * @TODO: add description
     */
    open fun copy(src: ConstraintWidget, map: HashMap<ConstraintWidget?, ConstraintWidget?>) {
        // Support for direct resolution
        mHorizontalResolution = src.mHorizontalResolution
        mVerticalResolution = src.mVerticalResolution
        mMatchConstraintDefaultWidth = src.mMatchConstraintDefaultWidth
        mMatchConstraintDefaultHeight = src.mMatchConstraintDefaultHeight
        mResolvedMatchConstraintDefault[0] = src.mResolvedMatchConstraintDefault[0]
        mResolvedMatchConstraintDefault[1] = src.mResolvedMatchConstraintDefault[1]
        mMatchConstraintMinWidth = src.mMatchConstraintMinWidth
        mMatchConstraintMaxWidth = src.mMatchConstraintMaxWidth
        mMatchConstraintMinHeight = src.mMatchConstraintMinHeight
        mMatchConstraintMaxHeight = src.mMatchConstraintMaxHeight
        mMatchConstraintPercentHeight = src.mMatchConstraintPercentHeight
        isWidthWrapContent = src.isWidthWrapContent
        isHeightWrapContent = src.isHeightWrapContent
        mResolvedDimensionRatioSide = src.mResolvedDimensionRatioSide
        mResolvedDimensionRatio = src.mResolvedDimensionRatio
        mMaxDimension = src.mMaxDimension.copyOf(src.mMaxDimension.size)
        mCircleConstraintAngle = src.mCircleConstraintAngle
        Utils.logStack(" copying angle = $mCircleConstraintAngle", 5)
        hasBaseline = src.hasBaseline
        isInPlaceholder = src.isInPlaceholder

        // The anchors available on the widget
        // note: all anchors should be added to the mAnchors array (see addAnchors())
        mLeft!!.reset()
        mTop.reset()
        mRight!!.reset()
        mBottom.reset()
        mBaseline!!.reset()
        mCenterX.reset()
        mCenterY.reset()
        mCenter.reset()
        mListDimensionBehaviors = mListDimensionBehaviors.copyOf(2)
        parent = if (parent == null) null else map.get(src.parent)
        mWidth = src.mWidth
        mHeight = src.mHeight
        dimensionRatio = src.dimensionRatio
        dimensionRatioSide = src.dimensionRatioSide
        mX = src.mX
        mY = src.mY
        mRelX = src.mRelX
        mRelY = src.mRelY
        mOffsetX = src.mOffsetX
        mOffsetY = src.mOffsetY
        mBaselineDistance = src.mBaselineDistance
        mMinWidth = src.mMinWidth
        mMinHeight = src.mMinHeight
        horizontalBiasPercent = src.horizontalBiasPercent
        verticalBiasPercent = src.verticalBiasPercent
        companionWidget = srcWidget
        mContainerItemSkip = src.mContainerItemSkip
        visibility = src.visibility
        isAnimated = src.isAnimated
        debugName = src.debugName
        type = src.type
        mDistToTop = src.mDistToTop
        mDistToLeft = src.mDistToLeft
        mDistToRight = src.mDistToRight
        mDistToBottom = src.mDistToBottom
        mLeftHasCentered = src.mLeftHasCentered
        mRightHasCentered = src.mRightHasCentered
        mTopHasCentered = src.mTopHasCentered
        mBottomHasCentered = src.mBottomHasCentered
        mHorizontalWrapVisited = src.mHorizontalWrapVisited
        mVerticalWrapVisited = src.mVerticalWrapVisited
        horizontalChainStyle = src.horizontalChainStyle
        verticalChainStyle = src.verticalChainStyle
        mHorizontalChainFixedPosition = src.mHorizontalChainFixedPosition
        mVerticalChainFixedPosition = src.mVerticalChainFixedPosition
        mWeight[0] = src.mWeight[0]
        mWeight[1] = src.mWeight[1]
        mListNextMatchConstraintsWidget[0] = src.mListNextMatchConstraintsWidget[0]
        mListNextMatchConstraintsWidget[1] = src.mListNextMatchConstraintsWidget[1]
        mNextChainWidget[0] = src.mNextChainWidget[0]
        mNextChainWidget[1] = src.mNextChainWidget[1]
        mHorizontalNextWidget = if (src.mHorizontalNextWidget == null) null else map.get(src.mHorizontalNextWidget)
        mVerticalNextWidget = if (src.mVerticalNextWidget == null) null else map.get(src.mVerticalNextWidget)
    }

    /**
     * @TODO: add description
     */
    open fun updateFromRuns(updateHorizontal: Boolean, updateVertical: Boolean) {
        var updateHorizontal = updateHorizontal
        var updateVertical = updateVertical
        updateHorizontal = updateHorizontal and mHorizontalRun!!.isResolved
        updateVertical = updateVertical and mVerticalRun!!.isResolved
        var left: Int = mHorizontalRun!!.start.value
        var top: Int = mVerticalRun!!.start.value
        var right: Int = mHorizontalRun!!.end.value
        var bottom: Int = mVerticalRun!!.end.value
        var w = right - left
        var h = bottom - top
        if (w < 0 || h < 0 || left == Int.MIN_VALUE || left == Int.MAX_VALUE || top == Int.MIN_VALUE || top == Int.MAX_VALUE || right == Int.MIN_VALUE || right == Int.MAX_VALUE || bottom == Int.MIN_VALUE || bottom == Int.MAX_VALUE) {
            left = 0
            top = 0
            right = 0
            bottom = 0
        }
        w = right - left
        h = bottom - top
        if (updateHorizontal) {
            mX = left
        }
        if (updateVertical) {
            mY = top
        }
        if (visibility == GONE) {
            mWidth = 0
            mHeight = 0
            return
        }

        // correct dimensional instability caused by rounding errors
        if (updateHorizontal) {
            if (mListDimensionBehaviors[DIMENSION_HORIZONTAL]
                == DimensionBehaviour.FIXED && w < mWidth
            ) {
                w = mWidth
            }
            mWidth = w
            if (mWidth < mMinWidth) {
                mWidth = mMinWidth
            }
        }
        if (updateVertical) {
            if (mListDimensionBehaviors[DIMENSION_VERTICAL]
                == DimensionBehaviour.FIXED && h < mHeight
            ) {
                h = mHeight
            }
            mHeight = h
            if (mHeight < mMinHeight) {
                mHeight = mMinHeight
            }
        }
    }

    /**
     * @TODO: add description
     */
    fun addChildrenToSolverByDependency(
        container: ConstraintWidgetContainer,
        system: LinearSystem,
        widgets: HashSet<ConstraintWidget?>,
        orientation: Int,
        addSelf: Boolean
    ) {
        if (addSelf) {
            if (!widgets.contains(this)) {
                return
            }
            Optimizer.checkMatchParent(container, system, this)
            widgets.remove(this)
            addToSolver(system, container.optimizeFor(Optimizer.OPTIMIZATION_GRAPH))
        }
        if (orientation == HORIZONTAL) {
            var dependents: HashSet<ConstraintAnchor>? = mLeft?.dependents
            if (dependents != null) {
                for (anchor in dependents) {
                    anchor.owner.addChildrenToSolverByDependency(
                        container,
                        system, widgets, orientation, true
                    )
                }
            }
            dependents = mRight!!.dependents
            if (dependents != null) {
                for (anchor in dependents) {
                    anchor.owner.addChildrenToSolverByDependency(
                        container,
                        system, widgets, orientation, true
                    )
                }
            }
        } else {
            var dependents: HashSet<ConstraintAnchor>? = mTop.dependents
            if (dependents != null) {
                for (anchor in dependents) {
                    anchor.owner.addChildrenToSolverByDependency(
                        container,
                        system, widgets, orientation, true
                    )
                }
            }
            dependents = mBottom.dependents
            if (dependents != null) {
                for (anchor in dependents) {
                    anchor.owner.addChildrenToSolverByDependency(
                        container,
                        system, widgets, orientation, true
                    )
                }
            }
            dependents = mBaseline?.dependents
            if (dependents != null) {
                for (anchor in dependents) {
                    anchor.owner.addChildrenToSolverByDependency(
                        container,
                        system, widgets, orientation, true
                    )
                }
            }
        }
        // horizontal
    }

    /**
     * @TODO: add description
     */
    open fun getSceneString(ret: StringBuilder) {
        ret.append("  $stringId:{\n")
        ret.append("    actualWidth:$mWidth")
        ret.append("\n")
        ret.append("    actualHeight:$mHeight")
        ret.append("\n")
        ret.append("    actualLeft:$mX")
        ret.append("\n")
        ret.append("    actualTop:$mY")
        ret.append("\n")
        getSceneString(ret, "left", mLeft)
        getSceneString(ret, "top", mTop)
        getSceneString(ret, "right", mRight)
        getSceneString(ret, "bottom", mBottom)
        getSceneString(ret, "baseline", mBaseline)
        getSceneString(ret, "centerX", mCenterX)
        getSceneString(ret, "centerY", mCenterY)
        getSceneString(
            ret, "    width",
            mWidth,
            mMinWidth,
            mMaxDimension[HORIZONTAL],
            mWidthOverride,
            mMatchConstraintMinWidth,
            mMatchConstraintDefaultWidth,
            mMatchConstraintPercentWidth,
            mWeight[DIMENSION_HORIZONTAL]
        )
        getSceneString(
            ret, "    height",
            mHeight,
            mMinHeight,
            mMaxDimension[VERTICAL],
            mHeightOverride,
            mMatchConstraintMinHeight,
            mMatchConstraintDefaultHeight,
            mMatchConstraintPercentHeight,
            mWeight[DIMENSION_VERTICAL]
        )
        serializeDimensionRatio(ret, "    dimensionRatio", dimensionRatio, dimensionRatioSide)
        serializeAttribute(ret, "    horizontalBias", horizontalBiasPercent, DEFAULT_BIAS)
        serializeAttribute(ret, "    verticalBias", verticalBiasPercent, DEFAULT_BIAS)
        serializeAttribute(ret, "    horizontalChainStyle", horizontalChainStyle, CHAIN_SPREAD)
        serializeAttribute(ret, "    verticalChainStyle", verticalChainStyle, CHAIN_SPREAD)
        ret.append("  }")
    }

    private fun getSceneString(
        ret: StringBuilder, type: String, size: Int,
        min: Int, max: Int, override: Int,
        matchConstraintMin: Int, matchConstraintDefault: Int,
        matchConstraintPercent: Float,
        weight: Float
    ) {
        ret.append(type)
        ret.append(" :  {\n")
        serializeAttribute(ret, "      size", size, 0)
        serializeAttribute(ret, "      min", min, 0)
        serializeAttribute(ret, "      max", max, Int.MAX_VALUE)
        serializeAttribute(ret, "      matchMin", matchConstraintMin, 0)
        serializeAttribute(ret, "      matchDef", matchConstraintDefault, MATCH_CONSTRAINT_SPREAD)
        serializeAttribute(ret, "      matchPercent", matchConstraintPercent, 1f)
        ret.append("    },\n")
    }

    private fun getSceneString(ret: StringBuilder, side: String, a: ConstraintAnchor?) {
        if (a?.target == null) {
            return
        }
        ret.append("    ")
        ret.append(side)
        ret.append(" : [ '")
        ret.append(a?.target)
        ret.append("'")
        if (a!!.mGoneMargin != Int.MIN_VALUE || a.mMargin != 0) {
            ret.append(",")
            ret.append(a.mMargin)
            if (a.mGoneMargin != Int.MIN_VALUE) {
                ret.append(",")
                ret.append(a.mGoneMargin)
                ret.append(",")
            }
        }
        ret.append(" ] ,\n")
    }

    companion object {
        private const val AUTOTAG_CENTER = false
        private const val DO_NOT_USE = false
        protected const val SOLVER = 1
        const val DIRECT = 2

        // apply an intrinsic size when wrap content for spread dimensions
        private const val USE_WRAP_DIMENSION_FOR_SPREAD = false

        ////////////////////////////////////////////////////////////////////////////////////////////////
        const val MATCH_CONSTRAINT_SPREAD = 0
        const val MATCH_CONSTRAINT_WRAP = 1
        const val MATCH_CONSTRAINT_PERCENT = 2
        const val MATCH_CONSTRAINT_RATIO = 3
        const val MATCH_CONSTRAINT_RATIO_RESOLVED = 4
        const val UNKNOWN = -1
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val BOTH = 2
        const val VISIBLE = 0
        const val INVISIBLE = 4
        const val GONE = 8

        // Values of the chain styles
        const val CHAIN_SPREAD = 0
        const val CHAIN_SPREAD_INSIDE = 1
        const val CHAIN_PACKED = 2

        // Values of the wrap behavior in parent
        const val WRAP_BEHAVIOR_INCLUDED = 0 // default
        const val WRAP_BEHAVIOR_HORIZONTAL_ONLY = 1
        const val WRAP_BEHAVIOR_VERTICAL_ONLY = 2
        const val WRAP_BEHAVIOR_SKIPPED = 3
        private const val WRAP = -2
        const val ANCHOR_LEFT = 0
        const val ANCHOR_RIGHT = 1
        const val ANCHOR_TOP = 2
        const val ANCHOR_BOTTOM = 3
        const val ANCHOR_BASELINE = 4

        // The horizontal and vertical behaviour for the widgets' dimensions
        const val DIMENSION_HORIZONTAL = 0
        const val DIMENSION_VERTICAL = 1

        // Percentages used for biasing one connection over another when dual connections
        // of the same strength exist
        var DEFAULT_BIAS = 0.5f
    }
}
