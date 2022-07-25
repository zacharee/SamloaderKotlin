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
import androidx.constraintlayout.core.widgets.*
import androidx.constraintlayout.core.widgets.analyzer.*

import androidx.constraintlayout.core.LinearSystem
import androidx.constraintlayout.coreimport.Metrics
import androidx.constraintlayout.coreimport.SolverVariable
import kotlin.math.max

/**
 * A container of ConstraintWidget that can layout its children
 */
class ConstraintWidgetContainer : WidgetContainer {
    var mBasicMeasureSolver = BasicMeasure(this)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Graph measures
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var mDependencyGraph = DependencyGraph(this)
    private var mPass // number of layout passes
            = 0

    /**
     * Invalidate the graph of constraints
     */
    fun invalidateGraph() {
        mDependencyGraph.invalidateGraph()
    }

    /**
     * Invalidate the widgets measures
     */
    fun invalidateMeasures() {
        mDependencyGraph.invalidateMeasures()
    }

    /**
     * @TODO: add description
     */
    fun directMeasure(optimizeWrap: Boolean): Boolean {
        return mDependencyGraph.directMeasure(optimizeWrap)
        //        int paddingLeft = getX();
//        int paddingTop = getY();
//        if (mDependencyGraph.directMeasureSetup(optimizeWrap)) {
//            mDependencyGraph.measureWidgets();
//            boolean allResolved =
//                      mDependencyGraph.directMeasureWithOrientation(optimizeWrap, HORIZONTAL);
//            allResolved &= mDependencyGraph.directMeasureWithOrientation(optimizeWrap, VERTICAL);
//            for (ConstraintWidget child : mChildren) {
//                child.setDrawX(child.getDrawX() + paddingLeft);
//                child.setDrawY(child.getDrawY() + paddingTop);
//            }
//            setX(paddingLeft);
//            setY(paddingTop);
//            return allResolved;
//        }
//        return false;
    }

    /**
     * @TODO: add description
     */
    fun directMeasureSetup(optimizeWrap: Boolean): Boolean {
        return mDependencyGraph.directMeasureSetup(optimizeWrap)
    }

    /**
     * @TODO: add description
     */
    fun directMeasureWithOrientation(optimizeWrap: Boolean, orientation: Int): Boolean {
        return mDependencyGraph.directMeasureWithOrientation(optimizeWrap, orientation)
    }

    /**
     * @TODO: add description
     */
    fun defineTerminalWidgets() {
        mDependencyGraph.defineTerminalWidgets(
            horizontalDimensionBehaviour!!,
            verticalDimensionBehaviour!!
        )
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Measure the layout
     */
    fun measure(
        optimizationLevel: Int, widthMode: Int, widthSize: Int,
        heightMode: Int, heightSize: Int, lastMeasureWidth: Int,
        lastMeasureHeight: Int, paddingX: Int, paddingY: Int
    ): Long {
        mPaddingLeft = paddingX
        mPaddingTop = paddingY
        return mBasicMeasureSolver.solverMeasure(
            this, optimizationLevel, paddingX, paddingY,
            widthMode, widthSize, heightMode, heightSize,
            lastMeasureWidth, lastMeasureHeight
        )
    }

    /**
     * @TODO: add description
     */
    fun updateHierarchy() {
        mBasicMeasureSolver.updateHierarchy(this)
    }

    protected var mMeasurer: BasicMeasure.Measurer? = null

    /**
     * @TODO: add description
     */
    var measurer: BasicMeasure.Measurer?
        get() = mMeasurer
        set(measurer) {
            mMeasurer = measurer
            mDependencyGraph.setMeasurer(measurer)
        }
    /**
     * Returns the rtl status.
     *
     * @return true if in RTL, false otherwise.
     */
    /**
     * Set the rtl status. This has implications for Chains.
     *
     * @param isRtl true if we are in RTL.
     */
    var isRtl = false
    var mMetrics: Metrics? = null

    /**
     * @TODO: add description
     */
    fun fillMetrics(metrics: Metrics) {
        mMetrics = metrics
        system.fillMetrics(metrics)
    }

    var system = LinearSystem()
        protected set
    var mPaddingLeft = 0
    var mPaddingTop = 0
    var mPaddingRight = 0
    var mPaddingBottom = 0
    var mHorizontalChainsSize = 0
    var mVerticalChainsSize = 0
    var mVerticalChainsArray = arrayOfNulls<ChainHead>(4)
    var mHorizontalChainsArray = arrayOfNulls<ChainHead>(4)
    var mGroupsWrapOptimized = false
    var mHorizontalWrapOptimized = false
    var mVerticalWrapOptimized = false
    var mWrapFixedWidth = 0
    var mWrapFixedHeight = 0
    private var mOptimizationLevel = Optimizer.OPTIMIZATION_STANDARD
    var mSkipSolver = false

    /**
     * Return true if the width given is too small for the content laid out
     */
    var isWidthMeasuredTooSmall = false
        private set

    /**
     * Return true if the height given is too small for the content laid out
     */
    var isHeightMeasuredTooSmall = false
        private set
    /*-----------------------------------------------------------------------*/ // Construction
    /*-----------------------------------------------------------------------*/
    /**
     * Default constructor
     */
    constructor() {}

    /**
     * Constructor
     *
     * @param x      x position
     * @param y      y position
     * @param width  width of the layout
     * @param height height of the layout
     */
    constructor(x: Int, y: Int, width: Int, height: Int) : super(x, y, width, height) {}

    /**
     * Constructor
     *
     * @param width  width of the layout
     * @param height height of the layout
     */
    constructor(width: Int, height: Int) : super(width, height) {}
    constructor(debugName: String?, width: Int, height: Int) : super(width, height) {
        this.debugName = (debugName)
    }
    /**
     * Returns the current optimization level
     */
    /**
     * Resolves the system directly when possible
     *
     * @param value optimization level
     */
    var optimizationLevel: Int
        get() = mOptimizationLevel
        set(value) {
            mOptimizationLevel = value
            LinearSystem.USE_DEPENDENCY_ORDERING = optimizeFor(Optimizer.OPTIMIZATION_DEPENDENCY_ORDERING)
        }

    /**
     * Returns true if the given feature should be optimized
     */
    fun optimizeFor(feature: Int): Boolean {
        return mOptimizationLevel and feature == feature
    }

    /**
     * Specify the xml type for the container
     */
    override var type: String?
        get() = "ConstraintLayout"
        set(type) {
            super.type = type
        }

    override fun reset() {
        system.reset()
        mPaddingLeft = 0
        mPaddingRight = 0
        mPaddingTop = 0
        mPaddingBottom = 0
        mSkipSolver = false
        super.reset()
    }

    var mDebugSolverPassCount = 0
    private var mVerticalWrapMin: ConstraintAnchor? = null
    private var mHorizontalWrapMin: ConstraintAnchor? = null
    private var mVerticalWrapMax: ConstraintAnchor? = null
    private var mHorizontalWrapMax: ConstraintAnchor? = null
    fun addVerticalWrapMinVariable(top: ConstraintAnchor) {
        if (mVerticalWrapMin == null || mVerticalWrapMin == null || top.finalValue > mVerticalWrapMin!!.finalValue
        ) {
            mVerticalWrapMin = top
        }
    }

    /**
     * @TODO: add description
     */
    fun addHorizontalWrapMinVariable(left: ConstraintAnchor) {
        if (mHorizontalWrapMin == null || mHorizontalWrapMin == null || left.finalValue > mHorizontalWrapMin!!.finalValue
        ) {
            mHorizontalWrapMin = left
        }
    }

    fun addVerticalWrapMaxVariable(bottom: ConstraintAnchor) {
        if (mVerticalWrapMax == null || mVerticalWrapMax == null || bottom.finalValue > mVerticalWrapMax!!.finalValue
        ) {
            mVerticalWrapMax = bottom
        }
    }

    /**
     * @TODO: add description
     */
    fun addHorizontalWrapMaxVariable(right: ConstraintAnchor) {
        if (mHorizontalWrapMax == null || mHorizontalWrapMax == null || right.finalValue > mHorizontalWrapMax!!.finalValue
        ) {
            mHorizontalWrapMax = right
        }
    }

    private fun addMinWrap(constraintAnchor: ConstraintAnchor, parentMin: SolverVariable) {
        val variable = system.createObjectVariable(constraintAnchor)
        val wrapStrength: Int = SolverVariable.Companion.STRENGTH_EQUALITY
        system.addGreaterThan(variable!!, parentMin, 0, wrapStrength)
    }

    private fun addMaxWrap(constraintAnchor: ConstraintAnchor, parentMax: SolverVariable) {
        val variable = system.createObjectVariable(constraintAnchor)
        val wrapStrength: Int = SolverVariable.Companion.STRENGTH_EQUALITY
        system.addGreaterThan(parentMax, variable!!, 0, wrapStrength)
    }

    var mWidgetsToAdd: HashSet<ConstraintWidget?> = HashSet()

    /**
     * Add this widget to the solver
     *
     * @param system the solver we want to add the widget to
     */
    fun addChildrenToSolver(system: LinearSystem): Boolean {
        if (DEBUG) {
            println("\n#######################################")
            println("##    ADD CHILDREN TO SOLVER  ($mDebugSolverPassCount) ##")
            println("#######################################\n")
            mDebugSolverPassCount++
        }
        val optimize = optimizeFor(Optimizer.OPTIMIZATION_GRAPH)
        addToSolver(system, optimize)
        val count = mChildren.size
        var hasBarriers = false
        for (i in 0 until count) {
            val widget = mChildren[i]
            widget.setInBarrier(ConstraintWidget.HORIZONTAL, false)
            widget.setInBarrier(ConstraintWidget.VERTICAL, false)
            if (widget is Barrier) {
                hasBarriers = true
            }
        }
        if (hasBarriers) {
            for (i in 0 until count) {
                val widget = mChildren[i]
                if (widget is Barrier) {
                    (widget as Barrier).markWidgets()
                }
            }
        }
        mWidgetsToAdd.clear()
        for (i in 0 until count) {
            val widget = mChildren[i]
            if (widget.addFirst()) {
                if (widget is VirtualLayout) {
                    mWidgetsToAdd.add(widget)
                } else {
                    widget.addToSolver(system, optimize)
                }
            }
        }

        // If we have virtual layouts, we need to add them to the solver in the correct
        // order (in case they reference one another)
        while (mWidgetsToAdd.size > 0) {
            val numLayouts: Int = mWidgetsToAdd.size
            var layout: VirtualLayout? = null
            for (widget in mWidgetsToAdd) {
                layout = widget as VirtualLayout

                // we'll go through the virtual layouts that references others first, to give
                // them a shot at setting their constraints.
                if (layout!!.contains(mWidgetsToAdd)) {
                    layout.addToSolver(system, optimize)
                    mWidgetsToAdd.remove(layout)
                    break
                }
            }
            if (numLayouts == mWidgetsToAdd.size) {
                // looks we didn't find anymore dependency, let's add everything.
                for (widget in mWidgetsToAdd) {
                    widget?.addToSolver(system, optimize)
                }
                mWidgetsToAdd.clear()
            }
        }
        if (LinearSystem.USE_DEPENDENCY_ORDERING) {
            val widgetsToAdd: HashSet<ConstraintWidget?> = HashSet()
            for (i in 0 until count) {
                val widget = mChildren[i]
                if (!widget.addFirst()) {
                    widgetsToAdd.add(widget)
                }
            }
            var orientation: Int = ConstraintWidget.VERTICAL
            if (horizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                orientation = ConstraintWidget.HORIZONTAL
            }
            addChildrenToSolverByDependency(this, system, widgetsToAdd, orientation, false)
            for (widget in widgetsToAdd) {
                Optimizer.checkMatchParent(this, system, widget!!)
                widget.addToSolver(system, optimize)
            }
        } else {
            for (i in 0 until count) {
                val widget = mChildren[i]
                if (widget is ConstraintWidgetContainer) {
                    val horizontalBehaviour =
                        widget.mListDimensionBehaviors.get(ConstraintWidget.DIMENSION_HORIZONTAL)
                    val verticalBehaviour =
                        widget.mListDimensionBehaviors.get(ConstraintWidget.DIMENSION_VERTICAL)
                    if (horizontalBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                        widget.horizontalDimensionBehaviour = (DimensionBehaviour.FIXED)
                    }
                    if (verticalBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                        widget.verticalDimensionBehaviour = (DimensionBehaviour.FIXED)
                    }
                    widget.addToSolver(system, optimize)
                    if (horizontalBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                        widget.horizontalDimensionBehaviour = (horizontalBehaviour)
                    }
                    if (verticalBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                        widget.verticalDimensionBehaviour = (verticalBehaviour)
                    }
                } else {
                    Optimizer.checkMatchParent(this, system, widget)
                    if (!widget.addFirst()) {
                        widget.addToSolver(system, optimize)
                    }
                }
            }
        }
        if (mHorizontalChainsSize > 0) {
            Chain.applyChainConstraints(this, system, null, ConstraintWidget.HORIZONTAL)
        }
        if (mVerticalChainsSize > 0) {
            Chain.applyChainConstraints(this, system, null, ConstraintWidget.VERTICAL)
        }
        return true
    }

    /**
     * Update the frame of the layout and its children from the solver
     *
     * @param system the solver we get the values from.
     */
    fun updateChildrenFromSolver(system: LinearSystem, flags: BooleanArray?): Boolean {
        flags!![Optimizer.FLAG_RECOMPUTE_BOUNDS] = false
        val optimize = optimizeFor(Optimizer.OPTIMIZATION_GRAPH)
        updateFromSolver(system, optimize)
        val count = mChildren.size
        var hasOverride = false
        for (i in 0 until count) {
            val widget = mChildren[i]
            widget.updateFromSolver(system, optimize)
            if (widget.hasDimensionOverride()) {
                hasOverride = true
            }
        }
        return hasOverride
    }

    override fun updateFromRuns(updateHorizontal: Boolean, updateVertical: Boolean) {
        super.updateFromRuns(updateHorizontal, updateVertical)
        val count = mChildren.size
        for (i in 0 until count) {
            val widget = mChildren[i]
            widget.updateFromRuns(updateHorizontal, updateVertical)
        }
    }

    /**
     * Set the padding on this container. It will apply to the position of the children.
     *
     * @param left   left padding
     * @param top    top padding
     * @param right  right padding
     * @param bottom bottom padding
     */
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mPaddingLeft = left
        mPaddingTop = top
        mPaddingRight = right
        mPaddingBottom = bottom
    }

    /*-----------------------------------------------------------------------*/ // Overloaded methods from ConstraintWidget
    /*-----------------------------------------------------------------------*/
    var mMeasure = BasicMeasure.Measure()

    /**
     * Layout the tree of widgets
     */
    override fun layout() {
        if (DEBUG) {
            println("\n#####################################")
            println("##          CL LAYOUT PASS           ##")
            println("#####################################\n")
            mDebugSolverPassCount = 0
        }
        mX = 0
        mY = 0
        isWidthMeasuredTooSmall = false
        isHeightMeasuredTooSmall = false
        val count = mChildren.size
        var preW: Int = max(0, width)
        var preH: Int = max(0, height)
        val originalVerticalDimensionBehaviour: DimensionBehaviour =
            mListDimensionBehaviors[ConstraintWidget.DIMENSION_VERTICAL]!!
        val originalHorizontalDimensionBehaviour: DimensionBehaviour =
            mListDimensionBehaviors[ConstraintWidget.DIMENSION_HORIZONTAL]!!
        if (DEBUG_LAYOUT) {
            println(
                "layout with preW: " + preW + " ("
                        + mListDimensionBehaviors.get(ConstraintWidget.DIMENSION_HORIZONTAL) + ") preH: " + preH
                        + " (" + mListDimensionBehaviors.get(ConstraintWidget.DIMENSION_VERTICAL) + ")"
            )
        }
        if (mMetrics != null) {
            mMetrics!!.layouts++
        }
        var wrap_override = false
        if (LinearSystem.FULL_DEBUG) {
            println("OPTIMIZATION LEVEL $mOptimizationLevel")
        }

        // Only try the direct optimization in the first layout pass
        if (mPass == 0 && Optimizer.enabled(mOptimizationLevel, Optimizer.OPTIMIZATION_DIRECT)) {
            if (LinearSystem.Companion.FULL_DEBUG) {
                println("Direct pass " + sMyCounter++)
            }
            Direct.solvingPass(this, measurer)
            if (LinearSystem.Companion.FULL_DEBUG) {
                println("Direct pass done.")
            }
            for (i in 0 until count) {
                val child = mChildren[i]
                if (LinearSystem.Companion.FULL_DEBUG) {
                    if (child.isInHorizontalChain) {
                        print("H")
                    } else {
                        print(" ")
                    }
                    if (child.isInVerticalChain) {
                        print("V")
                    } else {
                        print(" ")
                    }
                    if (child.isResolvedHorizontally && child.isResolvedVertically) {
                        print("*")
                    } else {
                        print(" ")
                    }
                    println(
                        "[" + i + "] child " + child.debugName
                                + " H: " + child.isResolvedHorizontally
                                + " V: " + child.isResolvedVertically
                    )
                }
                if (child.isMeasureRequested
                    && child !is Guideline
                    && child !is Barrier
                    && child !is VirtualLayout
                    && !child.isInVirtualLayout
                ) {
                    val widthBehavior =
                        child.getDimensionBehaviour(ConstraintWidget.Companion.HORIZONTAL)
                    val heightBehavior =
                        child.getDimensionBehaviour(ConstraintWidget.Companion.VERTICAL)
                    val skip =
                        widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT && child.mMatchConstraintDefaultWidth != ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP && heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT && child.mMatchConstraintDefaultHeight != ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
                    if (!skip) {
                        val measure = BasicMeasure.Measure()
                        measure(
                            0, child, mMeasurer,
                            measure, BasicMeasure.Measure.SELF_DIMENSIONS
                        )
                    }
                }
            }
            // let's measure children
            if (LinearSystem.Companion.FULL_DEBUG) {
                println("Direct pass all done.")
            }
        } else {
            if (LinearSystem.Companion.FULL_DEBUG) {
                println("No DIRECT PASS")
            }
        }
        if (count > 2 && (originalHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT
                    || originalVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT)
            && Optimizer.enabled(mOptimizationLevel, Optimizer.OPTIMIZATION_GROUPING)
        ) {
            if (Grouping.simpleSolvingPass(this, measurer)) {
                if (originalHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    if (preW < width && preW > 0) {
                        if (DEBUG_LAYOUT) {
                            println("Override width " + width + " to " + preH)
                        }
                        width = (preW)
                        isWidthMeasuredTooSmall = true
                    } else {
                        preW = width
                    }
                }
                if (originalVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    if (preH < height && preH > 0) {
                        if (DEBUG_LAYOUT) {
                            println("Override height " + height + " to " + preH)
                        }
                        height = (preH)
                        isHeightMeasuredTooSmall = true
                    } else {
                        preH = height
                    }
                }
                wrap_override = true
                if (DEBUG_LAYOUT) {
                    println(
                        "layout post opt, preW: " + preW
                                + " (" + mListDimensionBehaviors.get(ConstraintWidget.Companion.DIMENSION_HORIZONTAL)
                                + ") preH: " + preH + " (" + mListDimensionBehaviors.get(ConstraintWidget.Companion.DIMENSION_VERTICAL)
                                + "), new size " + width + " x " + height
                    )
                }
            }
        }
        val useGraphOptimizer = (optimizeFor(Optimizer.OPTIMIZATION_GRAPH)
                || optimizeFor(Optimizer.OPTIMIZATION_GRAPH_WRAP))
        system.graphOptimizer = false
        system.newgraphOptimizer = false
        if (mOptimizationLevel != Optimizer.OPTIMIZATION_NONE
            && useGraphOptimizer
        ) {
            system.newgraphOptimizer = true
        }
        var countSolve = 0
        val allChildren: List<ConstraintWidget?> = mChildren
        val hasWrapContent = (horizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT
                || verticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT)

        // Reset the chains before iterating on our children
        resetChains()
        countSolve = 0

        // Before we solve our system, we should call layout() on any
        // of our children that is a container.
        for (i in 0 until count) {
            val widget = mChildren[i]
            if (widget is WidgetContainer) {
                (widget as WidgetContainer).layout()
            }
        }
        val optimize = optimizeFor(Optimizer.OPTIMIZATION_GRAPH)

        // Now let's solve our system as usual
        var needsSolving = true
        while (needsSolving) {
            countSolve++
            try {
                system.reset()
                resetChains()
                if (DEBUG) {
                    var debugName = this.debugName
                    if (debugName == null) {
                        debugName = "root"
                    }
                    setDebugSolverName(system, debugName)
                    for (i in 0 until count) {
                        val widget = mChildren[i]
                        if (widget.debugName != null) {
                            widget.setDebugSolverName(system, widget.debugName)
                        }
                    }
                } else {
                    createObjectVariables(system)
                    for (i in 0 until count) {
                        val widget = mChildren[i]
                        widget.createObjectVariables(system)
                    }
                }
                needsSolving = addChildrenToSolver(system)
                if (mVerticalWrapMin != null && mVerticalWrapMin != null) {
                    addMinWrap(mVerticalWrapMin!!, system.createObjectVariable(mTop)!!)
                    mVerticalWrapMin = null
                }
                if (mVerticalWrapMax != null && mVerticalWrapMax != null) {
                    addMaxWrap(mVerticalWrapMax!!, system.createObjectVariable(mBottom)!!)
                    mVerticalWrapMax = null
                }
                if (mHorizontalWrapMin != null && mHorizontalWrapMin != null) {
                    addMinWrap(mHorizontalWrapMin!!, system.createObjectVariable(mLeft)!!)
                    mHorizontalWrapMin = null
                }
                if (mHorizontalWrapMax != null && mHorizontalWrapMax != null) {
                    addMaxWrap(mHorizontalWrapMax!!, system.createObjectVariable(mRight)!!)
                    mHorizontalWrapMax = null
                }
                if (needsSolving) {
                    system.minimize()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("EXCEPTION : $e")
            }
            needsSolving = if (needsSolving) {
                updateChildrenFromSolver(system, Optimizer.sFlags)
            } else {
                updateFromSolver(system, optimize)
                for (i in 0 until count) {
                    val widget = mChildren[i]
                    widget.updateFromSolver(system, optimize)
                }
                false
            }
            if (hasWrapContent && countSolve < MAX_ITERATIONS && Optimizer.sFlags[Optimizer.FLAG_RECOMPUTE_BOUNDS]) {
                // let's get the new bounds
                var maxX = 0
                var maxY = 0
                for (i in 0 until count) {
                    val widget = mChildren[i]
                    maxX = max(maxX, widget.mX + widget.width)
                    maxY = max(maxY, widget.mY + widget.height)
                }
                maxX = max(mMinWidth, maxX)
                maxY = max(mMinHeight, maxY)
                if (originalHorizontalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    if (width < maxX) {
                        if (DEBUG_LAYOUT) {
                            println(
                                countSolve.toString() +
                                        "layout override width from " + width + " vs " + maxX
                            )
                        }
                        width = (maxX)
                        // force using the solver
                        mListDimensionBehaviors[(ConstraintWidget.DIMENSION_HORIZONTAL)] =
                            DimensionBehaviour.WRAP_CONTENT
                        wrap_override = true
                        needsSolving = true
                    }
                }
                if (originalVerticalDimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    if (height < maxY) {
                        if (DEBUG_LAYOUT) {
                            println(
                                "layout override height from " + height + " vs " + maxY
                            )
                        }
                        height = (maxY)
                        // force using the solver
                        mListDimensionBehaviors[(ConstraintWidget.Companion.DIMENSION_VERTICAL)] =
                            DimensionBehaviour.WRAP_CONTENT
                        wrap_override = true
                        needsSolving = true
                    }
                }
            }
            if (true) {
                var width: Int = max(mMinWidth, width)
                if (width > this.width) {
                    if (DEBUG_LAYOUT) {
                        println(
                            "layout override 2, width from " + this.width + " vs " + width
                        )
                    }
                    this.width = (width)
                    mListDimensionBehaviors[(ConstraintWidget.Companion.DIMENSION_HORIZONTAL)] =
                        DimensionBehaviour.FIXED
                    wrap_override = true
                    needsSolving = true
                }
                var height: Int = max(mMinHeight, this.height)
                if (height > this.height) {
                    if (DEBUG_LAYOUT) {
                        println(
                            "layout override 2, height from " + this.height + " vs " + height
                        )
                    }
                    this.height = (height)
                    mListDimensionBehaviors[(ConstraintWidget.Companion.DIMENSION_VERTICAL)] =
                        DimensionBehaviour.FIXED
                    wrap_override = true
                    needsSolving = true
                }
                if (!wrap_override) {
                    if (mListDimensionBehaviors.get(ConstraintWidget.Companion.DIMENSION_HORIZONTAL) == DimensionBehaviour.WRAP_CONTENT
                        && preW > 0
                    ) {
                        if (width > preW) {
                            if (DEBUG_LAYOUT) {
                                println(
                                    "layout override 3, width from " + width + " vs "
                                            + preW
                                )
                            }
                            isWidthMeasuredTooSmall = true
                            wrap_override = true
                            mListDimensionBehaviors[(ConstraintWidget.Companion.DIMENSION_HORIZONTAL)] =
                                DimensionBehaviour.FIXED
                            width = (preW)
                            needsSolving = true
                        }
                    }
                    if (mListDimensionBehaviors.get(ConstraintWidget.Companion.DIMENSION_VERTICAL) == DimensionBehaviour.WRAP_CONTENT
                        && preH > 0
                    ) {
                        if (height > preH) {
                            if (DEBUG_LAYOUT) {
                                println(
                                    "layout override 3, height from " + height + " vs "
                                            + preH
                                )
                            }
                            isHeightMeasuredTooSmall = true
                            wrap_override = true
                            mListDimensionBehaviors[(ConstraintWidget.Companion.DIMENSION_VERTICAL)] =
                                DimensionBehaviour.FIXED
                            height = (preH)
                            needsSolving = true
                        }
                    }
                }
                if (countSolve > MAX_ITERATIONS) {
                    needsSolving = false
                }
            }
        }
        if (DEBUG_LAYOUT) {
            println(
                "Solved system in " + countSolve + " iterations (" + width + " x "
                        + height + ")"
            )
        }
        mChildren = allChildren as ArrayList<ConstraintWidget>
        if (wrap_override) {
            mListDimensionBehaviors[(ConstraintWidget.Companion.DIMENSION_HORIZONTAL)] =
                originalHorizontalDimensionBehaviour
            mListDimensionBehaviors[(ConstraintWidget.Companion.DIMENSION_VERTICAL)] =
                originalVerticalDimensionBehaviour
        }
        resetSolverVariables(system.cache)
    }

    /**
     * Indicates if the container knows how to layout its content on its own
     *
     * @return true if the container does the layout, false otherwise
     */
    fun handlesInternalConstraints(): Boolean {
        return false
    }
    /*-----------------------------------------------------------------------*/ // Guidelines
    /*-----------------------------------------------------------------------*/
    /**
     * Accessor to the vertical guidelines contained in the table.
     *
     * @return array of guidelines
     */
    val verticalGuidelines: ArrayList<Guideline>
        get() {
            val guidelines: ArrayList<Guideline> = ArrayList<Guideline>()
            var i = 0
            val mChildrenSize = mChildren.size
            while (i < mChildrenSize) {
                val widget = mChildren[i]
                if (widget is Guideline) {
                    val guideline = widget as Guideline
                    if (guideline.orientation == Guideline.Companion.VERTICAL) {
                        guidelines.add(guideline)
                    }
                }
                i++
            }
            return guidelines
        }

    /**
     * Accessor to the horizontal guidelines contained in the table.
     *
     * @return array of guidelines
     */
    val horizontalGuidelines: ArrayList<Guideline>
        get() {
            val guidelines: ArrayList<Guideline> = ArrayList<Guideline>()
            var i = 0
            val mChildrenSize = mChildren.size
            while (i < mChildrenSize) {
                val widget = mChildren[i]
                if (widget is Guideline) {
                    val guideline = widget as Guideline
                    if (guideline.orientation == Guideline.Companion.HORIZONTAL) {
                        guidelines.add(guideline)
                    }
                }
                i++
            }
            return guidelines
        }
    /*-----------------------------------------------------------------------*/ // Chains
    /*-----------------------------------------------------------------------*/
    /**
     * Reset the chains array. Need to be called before layout.
     */
    private fun resetChains() {
        mHorizontalChainsSize = 0
        mVerticalChainsSize = 0
    }

    /**
     * Add the chain which constraintWidget is part of. Called by ConstraintWidget::addToSolver()
     *
     * @param type HORIZONTAL or VERTICAL chain
     */
    fun addChain(constraintWidget: ConstraintWidget, type: Int) {
        if (type == ConstraintWidget.Companion.HORIZONTAL) {
            addHorizontalChain(constraintWidget)
        } else if (type == ConstraintWidget.Companion.VERTICAL) {
            addVerticalChain(constraintWidget)
        }
    }

    /**
     * Add a widget to the list of horizontal chains. The widget is the left-most widget
     * of the chain which doesn't have a left dual connection.
     *
     * @param widget widget starting the chain
     */
    private fun addHorizontalChain(widget: ConstraintWidget) {
        if (mHorizontalChainsSize + 1 >= mHorizontalChainsArray.size) {
            mHorizontalChainsArray = mHorizontalChainsArray.copyOf(mHorizontalChainsArray.size * 2)
        }
        mHorizontalChainsArray[mHorizontalChainsSize] = ChainHead(widget, ConstraintWidget.Companion.HORIZONTAL, isRtl)
        mHorizontalChainsSize++
    }

    /**
     * Add a widget to the list of vertical chains. The widget is the top-most widget
     * of the chain which doesn't have a top dual connection.
     *
     * @param widget widget starting the chain
     */
    private fun addVerticalChain(widget: ConstraintWidget) {
        if (mVerticalChainsSize + 1 >= mVerticalChainsArray.size) {
            mVerticalChainsArray = mVerticalChainsArray.copyOf(mVerticalChainsArray.size * 2)
        }
        mVerticalChainsArray[mVerticalChainsSize] = ChainHead(widget, ConstraintWidget.Companion.VERTICAL, isRtl)
        mVerticalChainsSize++
    }

    /**
     * Keep track of the # of passes
     */
    fun setPass(pass: Int) {
        mPass = pass
    }

    /**
     * @TODO: add description
     */
    override fun getSceneString(ret: StringBuilder) {
        ret.append(stringId + ":{\n")
        ret.append("  actualWidth:$mWidth")
        ret.append("\n")
        ret.append("  actualHeight:$mHeight")
        ret.append("\n")
        val children: ArrayList<ConstraintWidget> = children
        for (child in children) {
            child.getSceneString(ret)
            ret.append(",\n")
        }
        ret.append("}")
    }

    companion object {
        private const val MAX_ITERATIONS = 8
        private val DEBUG: Boolean = LinearSystem.Companion.FULL_DEBUG
        private const val DEBUG_LAYOUT = false
        const val DEBUG_GRAPH = false

        /**
         * @TODO: add description
         */
        fun measure(
            level: Int,
            widget: ConstraintWidget,
            measurer: BasicMeasure.Measurer?,
            measure: BasicMeasure.Measure,
            measureStrategy: Int
        ): Boolean {
            if (DEBUG) {
                println(Direct.ls(level) + "(M) call to measure " + widget.debugName)
            }
            if (measurer == null) {
                return false
            }
            if (widget.visibility == ConstraintWidget.Companion.GONE || widget is Guideline
                || widget is Barrier
            ) {
                if (DEBUG) {
                    println(
                        Direct.ls(level)
                                + "(M) no measure needed for " + widget.debugName
                    )
                }
                measure.measuredWidth = 0
                measure.measuredHeight = 0
                return false
            }
            measure.horizontalBehavior = widget.horizontalDimensionBehaviour
            measure.verticalBehavior = widget.verticalDimensionBehaviour
            measure.horizontalDimension = widget.width
            measure.verticalDimension = widget.height
            measure.measuredNeedsSolverPass = false
            measure.measureStrategy = measureStrategy
            var horizontalMatchConstraints = measure.horizontalBehavior == DimensionBehaviour.MATCH_CONSTRAINT
            var verticalMatchConstraints = measure.verticalBehavior == DimensionBehaviour.MATCH_CONSTRAINT
            val horizontalUseRatio = horizontalMatchConstraints && widget.dimensionRatio > 0
            val verticalUseRatio = verticalMatchConstraints && widget.dimensionRatio > 0
            if (horizontalMatchConstraints && widget.hasDanglingDimension(ConstraintWidget.Companion.HORIZONTAL) && widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD && !horizontalUseRatio) {
                horizontalMatchConstraints = false
                measure.horizontalBehavior = DimensionBehaviour.WRAP_CONTENT
                if (verticalMatchConstraints
                    && widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                ) {
                    // if match x match, size would be zero.
                    measure.horizontalBehavior = DimensionBehaviour.FIXED
                }
            }
            if (verticalMatchConstraints && widget.hasDanglingDimension(ConstraintWidget.Companion.VERTICAL) && widget.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD && !verticalUseRatio) {
                verticalMatchConstraints = false
                measure.verticalBehavior = DimensionBehaviour.WRAP_CONTENT
                if (horizontalMatchConstraints
                    && widget.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
                ) {
                    // if match x match, size would be zero.
                    measure.verticalBehavior = DimensionBehaviour.FIXED
                }
            }
            if (widget.isResolvedHorizontally) {
                horizontalMatchConstraints = false
                measure.horizontalBehavior = DimensionBehaviour.FIXED
            }
            if (widget.isResolvedVertically) {
                verticalMatchConstraints = false
                measure.verticalBehavior = DimensionBehaviour.FIXED
            }
            if (horizontalUseRatio) {
                if (widget.mResolvedMatchConstraintDefault[ConstraintWidget.Companion.HORIZONTAL]
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO_RESOLVED
                ) {
                    measure.horizontalBehavior = DimensionBehaviour.FIXED
                } else if (!verticalMatchConstraints) {
                    // let's measure here
                    val measuredHeight: Int
                    if (measure.verticalBehavior == DimensionBehaviour.FIXED) {
                        measuredHeight = measure.verticalDimension
                    } else {
                        measure.horizontalBehavior = DimensionBehaviour.WRAP_CONTENT
                        measurer.measure(widget, measure)
                        measuredHeight = measure.measuredHeight
                    }
                    measure.horizontalBehavior = DimensionBehaviour.FIXED
                    // regardless of which side we are using for the ratio, getDimensionRatio() already
                    // made sure that it's expressed in WxH format, so we can simply go and multiply
                    measure.horizontalDimension = (widget.dimensionRatio * measuredHeight).toInt()
                    if (DEBUG) {
                        println("(M) Measured once for ratio on horizontal side...")
                    }
                }
            }
            if (verticalUseRatio) {
                if (widget.mResolvedMatchConstraintDefault[ConstraintWidget.Companion.VERTICAL]
                    == ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO_RESOLVED
                ) {
                    measure.verticalBehavior = DimensionBehaviour.FIXED
                } else if (!horizontalMatchConstraints) {
                    // let's measure here
                    val measuredWidth: Int
                    if (measure.horizontalBehavior == DimensionBehaviour.FIXED) {
                        measuredWidth = measure.horizontalDimension
                    } else {
                        measure.verticalBehavior = DimensionBehaviour.WRAP_CONTENT
                        measurer.measure(widget, measure)
                        measuredWidth = measure.measuredWidth
                    }
                    measure.verticalBehavior = DimensionBehaviour.FIXED
                    if (widget.dimensionRatioSide == -1) {
                        // regardless of which side we are using for the ratio,
                        //  getDimensionRatio() already
                        // made sure that it's expressed in WxH format,
                        //  so we can simply go and divide
                        measure.verticalDimension = (measuredWidth / widget.dimensionRatio).toInt()
                    } else {
                        // getDimensionRatio() already got reverted, so we can simply multiply
                        measure.verticalDimension = (widget.dimensionRatio * measuredWidth).toInt()
                    }
                    if (DEBUG) {
                        println("(M) Measured once for ratio on vertical side...")
                    }
                }
            }
            measurer.measure(widget, measure)
            widget.width = measure.measuredWidth
            widget.height = measure.measuredHeight
            widget.hasBaseline = measure.measuredHasBaseline
            widget.baselineDistance = measure.measuredBaseline
            measure.measureStrategy = BasicMeasure.Measure.Companion.SELF_DIMENSIONS
            if (DEBUG) {
                println(
                    "(M) Measured " + widget.debugName + " with : "
                            + widget.horizontalDimensionBehaviour + " x "
                            + widget.verticalDimensionBehaviour + " => "
                            + widget.width + " x " + widget.height
                )
            }
            return measure.measuredNeedsSolverPass
        }

        var sMyCounter = 0
    }
}
