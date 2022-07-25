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

import androidx.constraintlayout.core.widgets.ConstraintWidget

/**
 * Represents a dimension (width or height) of a constrained widget
 */
class Dimension {
    private val mWrapContent = -2
    var mMin = 0
    var mMax = Int.MAX_VALUE
    var mPercent = 1f
    var mValue = 0
    var mRatioString: String? = null
    var mInitialValue: Any? = WRAP_DIMENSION
    var mIsSuggested = false

    /**
     * Returns true if the dimension is a fixed dimension of
     * the same given value
     */
    fun equalsFixedValue(value: Int): Boolean {
        return (mInitialValue == null
                && mValue == value)
    }

    enum class Type {
        FIXED, WRAP, MATCH_PARENT, MATCH_CONSTRAINT
    }

    private constructor() {}
    private constructor(type: Any) {
        mInitialValue = type
    }

    /**
     * @TODO: add description
     */
    fun percent(key: Any?, value: Float): Dimension {
        mPercent = value
        return this
    }

    /**
     * @TODO: add description
     */
    fun min(value: Int): Dimension {
        if (value >= 0) {
            mMin = value
        }
        return this
    }

    /**
     * @TODO: add description
     */
    fun min(value: Any): Dimension {
        if (value === WRAP_DIMENSION) {
            mMin = mWrapContent
        }
        return this
    }

    /**
     * @TODO: add description
     */
    fun max(value: Int): Dimension {
        if (mMax >= 0) {
            mMax = value
        }
        return this
    }

    /**
     * @TODO: add description
     */
    fun max(value: Any): Dimension {
        if (value === WRAP_DIMENSION && mIsSuggested) {
            mInitialValue = WRAP_DIMENSION
            mMax = Int.MAX_VALUE
        }
        return this
    }

    /**
     * @TODO: add description
     */
    fun suggested(value: Int): Dimension {
        mIsSuggested = true
        if (value >= 0) {
            mMax = value
        }
        return this
    }

    /**
     * @TODO: add description
     */
    fun suggested(value: Any?): Dimension {
        mInitialValue = value
        mIsSuggested = true
        return this
    }

    /**
     * @TODO: add description
     */
    fun fixed(value: Any?): Dimension {
        mInitialValue = value
        if (value is Int) {
            mValue = value
            mInitialValue = null
        }
        return this
    }

    /**
     * @TODO: add description
     */
    fun fixed(value: Int): Dimension {
        mInitialValue = null
        mValue = value
        return this
    }

    /**
     * @TODO: add description
     */
    fun ratio(ratio: String?): Dimension { // WxH ratio
        mRatioString = ratio
        return this
    }

    // fixed value
    var value: Int
        get() = mValue
        set(value) {
            mIsSuggested = false // fixed value
            mInitialValue = null
            mValue = value
        }

    /**
     * Apply the dimension to the given constraint widget
     */
    fun apply(state: State?, constraintWidget: ConstraintWidget, orientation: Int) {
        if (mRatioString != null) {
            constraintWidget.setDimensionRatio(mRatioString)
        }
        if (orientation == ConstraintWidget.HORIZONTAL) {
            if (mIsSuggested) {
                constraintWidget.horizontalDimensionBehaviour = (
                    ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
                )
                var type: Int = ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                if (mInitialValue === WRAP_DIMENSION) {
                    type = ConstraintWidget.MATCH_CONSTRAINT_WRAP
                } else if (mInitialValue === PERCENT_DIMENSION) {
                    type = ConstraintWidget.MATCH_CONSTRAINT_PERCENT
                }
                constraintWidget.setHorizontalMatchStyle(type, mMin, mMax, mPercent)
            } else { // fixed
                if (mMin > 0) {
                    constraintWidget.minWidth = mMin
                }
                if (mMax < Int.MAX_VALUE) {
                    constraintWidget.maxWidth = mMax
                }
                if (mInitialValue === WRAP_DIMENSION) {
                    constraintWidget.horizontalDimensionBehaviour = (
                        ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
                    )
                } else if (mInitialValue === PARENT_DIMENSION) {
                    constraintWidget.horizontalDimensionBehaviour = (
                        ConstraintWidget.DimensionBehaviour.MATCH_PARENT
                    )
                } else if (mInitialValue == null) {
                    constraintWidget.horizontalDimensionBehaviour = (
                        ConstraintWidget.DimensionBehaviour.FIXED
                    )
                    constraintWidget.width = mValue
                }
            }
        } else {
            if (mIsSuggested) {
                constraintWidget.verticalDimensionBehaviour = (
                    ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
                )
                var type: Int = ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                if (mInitialValue === WRAP_DIMENSION) {
                    type = ConstraintWidget.MATCH_CONSTRAINT_WRAP
                } else if (mInitialValue === PERCENT_DIMENSION) {
                    type = ConstraintWidget.MATCH_CONSTRAINT_PERCENT
                }
                constraintWidget.setVerticalMatchStyle(type, mMin, mMax, mPercent)
            } else { // fixed
                if (mMin > 0) {
                    constraintWidget.minHeight = mMin
                }
                if (mMax < Int.MAX_VALUE) {
                    constraintWidget.maxHeight = mMax
                }
                if (mInitialValue === WRAP_DIMENSION) {
                    constraintWidget.verticalDimensionBehaviour = (
                        ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
                    )
                } else if (mInitialValue === PARENT_DIMENSION) {
                    constraintWidget.verticalDimensionBehaviour = (
                        ConstraintWidget.DimensionBehaviour.MATCH_PARENT
                    )
                } else if (mInitialValue == null) {
                    constraintWidget.verticalDimensionBehaviour = (
                        ConstraintWidget.DimensionBehaviour.FIXED
                    )
                    constraintWidget.height = mValue
                }
            }
        }
    }

    companion object {
        val FIXED_DIMENSION = Any()
        val WRAP_DIMENSION = Any()
        val SPREAD_DIMENSION = Any()
        val PARENT_DIMENSION = Any()
        val PERCENT_DIMENSION = Any()
        val RATIO_DIMENSION = Any()

        /**
         * @TODO: add description
         */
        fun createSuggested(value: Int): Dimension {
            val dimension = Dimension()
            dimension.suggested(value)
            return dimension
        }

        /**
         * @TODO: add description
         */
        fun createSuggested(startValue: Any?): Dimension {
            val dimension = Dimension()
            dimension.suggested(startValue)
            return dimension
        }

        /**
         * @TODO: add description
         */
        fun createFixed(value: Int): Dimension {
            val dimension = Dimension(FIXED_DIMENSION)
            dimension.fixed(value)
            return dimension
        }

        /**
         * @TODO: add description
         */
        fun createFixed(value: Any?): Dimension {
            val dimension = Dimension(FIXED_DIMENSION)
            dimension.fixed(value)
            return dimension
        }

        /**
         * @TODO: add description
         */
        fun createPercent(key: Any?, value: Float): Dimension {
            val dimension = Dimension(PERCENT_DIMENSION)
            dimension.percent(key, value)
            return dimension
        }

        /**
         * @TODO: add description
         */
        fun createParent(): Dimension {
            return Dimension(PARENT_DIMENSION)
        }

        /**
         * @TODO: add description
         */
        fun createWrap(): Dimension {
            return Dimension(WRAP_DIMENSION)
        }

        /**
         * @TODO: add description
         */
        fun createSpread(): Dimension {
            return Dimension(SPREAD_DIMENSION)
        }

        /**
         * @TODO: add description
         */
        fun createRatio(ratio: String?): Dimension {
            val dimension = Dimension(RATIO_DIMENSION)
            dimension.ratio(ratio)
            return dimension
        }
    }
}
