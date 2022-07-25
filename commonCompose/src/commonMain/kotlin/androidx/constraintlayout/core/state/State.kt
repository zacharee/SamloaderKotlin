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
package androidx.constraintlayout.core.state

import androidx.constraintlayout.core.state.helpers.*
import androidx.constraintlayout.core.state.helpers.BarrierReference
import androidx.constraintlayout.core.widgets.ConstraintWidget
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer

/**
 * Represents a full state of a ConstraintLayout
 */
open class State {
    /**
     * Set the function that converts dp to Pixels
     */
    var dpToPixel: CorePixelDp? = null
    protected var mReferences: HashMap<Any, Reference> = HashMap()
    protected var mHelperReferences: HashMap<Any, HelperReference> = HashMap()
    var mTags: HashMap<String, ArrayList<String>> = HashMap()
    val mParent = ConstraintReference(this)

    enum class Constraint {
        LEFT_TO_LEFT, LEFT_TO_RIGHT, RIGHT_TO_LEFT, RIGHT_TO_RIGHT, START_TO_START, START_TO_END, END_TO_START, END_TO_END, TOP_TO_TOP, TOP_TO_BOTTOM, BOTTOM_TO_TOP, BOTTOM_TO_BOTTOM, BASELINE_TO_BASELINE, BASELINE_TO_TOP, BASELINE_TO_BOTTOM, CENTER_HORIZONTALLY, CENTER_VERTICALLY, CIRCULAR_CONSTRAINT
    }

    enum class Direction {
        LEFT, RIGHT, START, END, TOP, BOTTOM
    }

    enum class Helper {
        HORIZONTAL_CHAIN, VERTICAL_CHAIN, ALIGN_HORIZONTALLY, ALIGN_VERTICALLY, BARRIER, LAYER, FLOW
    }

    enum class Chain {
        SPREAD, SPREAD_INSIDE, PACKED
    }

    /**
     * Clear the state
     */
    open fun reset() {
        for (ref in mReferences.keys) {
            mReferences[ref]?.constraintWidget?.reset()
        }
        mReferences.clear()
        mReferences.put(PARENT, mParent)
        mHelperReferences.clear()
        mTags.clear()
        mBaselineNeeded.clear()
        mDirtyBaselineNeededWidgets = true
    }

    /**
     * Implements a conversion function for values, returning int.
     * This can be used in case values (e.g. margins) are represented
     * via an object, not directly an int.
     *
     * @param value the object to convert from
     */
    open fun convertDimension(value: Any?): Int {
        if (value is Float) {
            return value.toInt()
        }
        return if (value is Int) {
            value
        } else 0
    }

    /**
     * Create a new reference given a key.
     */
    fun createConstraintReference(key: Any?): ConstraintReference {
        return ConstraintReference(this)
    }

    /**
     * @TODO: add description
     */
    fun sameFixedWidth(width: Int): Boolean {
        return mParent.width.equalsFixedValue(width) == true
    }

    /**
     * @TODO: add description
     */
    fun sameFixedHeight(height: Int): Boolean {
        return mParent.height.equalsFixedValue(height) == true
    }

    /**
     * @TODO: add description
     */
    fun width(dimension: Dimension): State {
        return setWidth(dimension)
    }

    /**
     * @TODO: add description
     */
    fun height(dimension: Dimension): State {
        return setHeight(dimension)
    }

    /**
     * @TODO: add description
     */
    fun setWidth(dimension: Dimension): State {
        mParent.width = dimension
        return this
    }

    /**
     * @TODO: add description
     */
    fun setHeight(dimension: Dimension): State {
        mParent.height = dimension
        return this
    }

    fun reference(key: Any?): Reference {
        return mReferences[key]!!
    }

    /**
     * @TODO: add description
     */
    fun constraints(key: Any): ConstraintReference? {
        var reference: Reference? = mReferences[key]
        if (reference == null) {
            reference = createConstraintReference(key)
            mReferences[key] = reference
            reference.key = (key)
        }
        return if (reference is ConstraintReference) {
            reference
        } else null
    }

    private var mNumHelpers = 0
    private fun createHelperKey(): String {
        return "__HELPER_KEY_" + mNumHelpers++ + "__"
    }

    /**
     * @TODO: add description
     */
    fun helper(key: Any?, type: Helper): HelperReference {
        var key = key
        if (key == null) {
            key = createHelperKey()
        }
        var reference: HelperReference? = mHelperReferences[key]
        if (reference == null) {
            when (type) {
                Helper.HORIZONTAL_CHAIN -> {
                    reference = HorizontalChainReference(this)
                }

                Helper.VERTICAL_CHAIN -> {
                    reference = VerticalChainReference(this)
                }

                Helper.ALIGN_HORIZONTALLY -> {
                    reference = AlignHorizontallyReference(this)
                }

                Helper.ALIGN_VERTICALLY -> {
                    reference = AlignVerticallyReference(this)
                }

                Helper.BARRIER -> {
                    reference = BarrierReference(this)
                }

                else -> {
                    reference = HelperReference(this, type)
                }
            }
            reference.key = (key)
            mHelperReferences[key] = reference
        }
        return reference
    }

    /**
     * @TODO: add description
     */
    fun horizontalGuideline(key: Any): GuidelineReference? {
        return guideline(key, ConstraintWidget.HORIZONTAL)
    }

    /**
     * @TODO: add description
     */
    fun verticalGuideline(key: Any): GuidelineReference? {
        return guideline(key, ConstraintWidget.VERTICAL)
    }

    /**
     * @TODO: add description
     */
    fun guideline(key: Any, orientation: Int): GuidelineReference {
        val reference = constraints(key)
        if (reference!!.facade == null
            || reference.facade !is GuidelineReference
        ) {
            val guidelineReference = GuidelineReference(this)
            guidelineReference.orientation = orientation
            guidelineReference.key = key
            reference.setFacade(guidelineReference)
        }
        return reference.facade as GuidelineReference
    }

    /**
     * @TODO: add description
     */
    fun barrier(key: Any, direction: Direction?): BarrierReference {
        val reference = constraints(key)
        if (reference!!.facade == null || reference.facade !is BarrierReference) {
            val barrierReference = BarrierReference(this)
            barrierReference.setBarrierDirection(direction)
            reference.setFacade(barrierReference)
        }
        return reference.facade as BarrierReference
    }

    /**
     * @TODO: add description
     */
    fun verticalChain(): VerticalChainReference {
        return helper(null, Helper.VERTICAL_CHAIN) as VerticalChainReference
    }

    /**
     * @TODO: add description
     */
    fun verticalChain(vararg references: Any): VerticalChainReference {
        val reference = helper(null, Helper.VERTICAL_CHAIN) as VerticalChainReference
        reference.add(*references)
        return reference
    }

    /**
     * @TODO: add description
     */
    fun horizontalChain(): HorizontalChainReference {
        return helper(null, Helper.HORIZONTAL_CHAIN) as HorizontalChainReference
    }

    /**
     * @TODO: add description
     */
    fun horizontalChain(vararg references: Any): HorizontalChainReference {
        val reference = helper(null, Helper.HORIZONTAL_CHAIN) as HorizontalChainReference
        reference.add(*references)
        return reference
    }

    /**
     * @TODO: add description
     */
    fun centerHorizontally(vararg references: Any): AlignHorizontallyReference {
        val reference = helper(null, Helper.ALIGN_HORIZONTALLY) as AlignHorizontallyReference
        reference.add(*references)
        return reference
    }

    /**
     * @TODO: add description
     */
    fun centerVertically(vararg references: Any): AlignVerticallyReference {
        val reference = helper(null, Helper.ALIGN_VERTICALLY) as AlignVerticallyReference
        reference.add(*references)
        return reference
    }

    /**
     * @TODO: add description
     */
    fun directMapping() {
        for (key in mReferences.keys) {
            val ref = constraints(key) ?: continue
            ref.view = key
        }
    }

    /**
     * @TODO: add description
     */
    fun map(key: Any, view: Any?) {
        val ref: Reference? = constraints(key)
        if (ref is ConstraintReference) {
            ref.view = view
        }
    }

    /**
     * @TODO: add description
     */
    fun setTag(key: String, tag: String) {
        val ref: Reference? = constraints(key)
        if (ref is ConstraintReference) {
            ref.tag = tag
            val list: ArrayList<String>?
            if (!mTags.containsKey(tag)) {
                list = ArrayList()
                mTags.put(tag, list)
            } else {
                list = mTags.get(tag)
            }
            list?.add(key)
        }
    }

    /**
     * @TODO: add description
     */
    fun getIdsForTag(tag: String?): ArrayList<String>? {
        return if (mTags.containsKey(tag)) {
            mTags.get(tag)
        } else null
    }

    /**
     * @TODO: add description
     */
    fun apply(container: ConstraintWidgetContainer) {
        container.removeAllChildren()
        mParent.width.apply(this, container, ConstraintWidget.HORIZONTAL)
        mParent.height.apply(this, container, ConstraintWidget.VERTICAL)
        // add helper references
        for (key in mHelperReferences.keys) {
            val reference = mHelperReferences[key]
            val helperWidget = reference?.helperWidget
            if (helperWidget != null) {
                var constraintReference: Reference? = mReferences[key]
                if (constraintReference == null) {
                    constraintReference = constraints(key)
                }
                constraintReference!!.constraintWidget = (helperWidget)
            }
        }
        for (key in mReferences.keys) {
            val reference = mReferences.get(key)
            if (reference !== mParent && reference?.facade is HelperReference) {
                val helperWidget = (reference.facade as HelperReference).helperWidget
                if (helperWidget != null) {
                    var constraintReference: Reference? = mReferences[key]
                    if (constraintReference == null) {
                        constraintReference = constraints(key)
                    }
                    constraintReference!!.constraintWidget = (helperWidget)
                }
            }
        }
        for (key in mReferences.keys) {
            val reference = mReferences.get(key)
            if (reference !== mParent) {
                val widget = reference?.constraintWidget
                widget?.debugName = reference?.key?.toString()
                widget?.parent = null
                if (reference?.facade is GuidelineReference) {
                    // we apply Guidelines first to correctly setup their ConstraintWidget.
                    reference.apply()
                }
                widget?.let {
                    container.add(widget)
                }
            } else {
                reference.constraintWidget = (container)
            }
        }
        for (key in mHelperReferences.keys) {
            val reference = mHelperReferences[key]
            val helperWidget = reference?.helperWidget
            if (helperWidget != null) {
                for (keyRef in reference.mReferences) {
                    val constraintReference = mReferences[keyRef]
                    reference.helperWidget?.add(constraintReference?.constraintWidget)
                }
                reference.apply()
            } else {
                reference?.apply()
            }
        }
        for (key in mReferences.keys) {
            val reference = mReferences[key]
            if (reference !== mParent && reference?.facade is HelperReference) {
                val helperReference = reference.facade as HelperReference
                val helperWidget = helperReference.helperWidget
                if (helperWidget != null) {
                    for (keyRef in helperReference.mReferences) {
                        val constraintReference = mReferences[keyRef]
                        if (constraintReference != null) {
                            helperWidget.add(constraintReference.constraintWidget)
                        } else if (keyRef is Reference) {
                            helperWidget.add(keyRef.constraintWidget)
                        } else {
                            println("couldn't find reference for $keyRef")
                        }
                    }
                    reference.apply()
                }
            }
        }
        for (key in mReferences.keys) {
            val reference = mReferences[key]
            reference?.apply()
            val widget = reference?.constraintWidget
            if (widget != null) {
                widget.stringId = key.toString()
            }
        }
    }

    // ================= add baseline code================================
    var mBaselineNeeded: ArrayList<Any> = ArrayList()
    var mBaselineNeededWidgets: ArrayList<ConstraintWidget> = ArrayList()
    var mDirtyBaselineNeededWidgets = true

    init {
        mReferences[PARENT] = mParent
    }

    /**
     * Baseline is needed for this object
     */
    fun baselineNeededFor(id: Any) {
        mBaselineNeeded.add(id)
        mDirtyBaselineNeededWidgets = true
    }

    /**
     * Does this constraintWidget need a baseline
     *
     * @return true if the constraintWidget needs a baseline
     */
    fun isBaselineNeeded(constraintWidget: ConstraintWidget?): Boolean {
        if (mDirtyBaselineNeededWidgets) {
            mBaselineNeededWidgets.clear()
            for (id in mBaselineNeeded) {
                val widget = mReferences[id]?.constraintWidget
                if (widget != null) mBaselineNeededWidgets.add(widget)
            }
            mDirtyBaselineNeededWidgets = false
        }
        return mBaselineNeededWidgets.contains(constraintWidget)
    }

    companion object {
        const val UNKNOWN = -1
        const val CONSTRAINT_SPREAD = 0
        const val CONSTRAINT_WRAP = 1
        const val CONSTRAINT_RATIO = 2
        const val PARENT = 0
    }
}
