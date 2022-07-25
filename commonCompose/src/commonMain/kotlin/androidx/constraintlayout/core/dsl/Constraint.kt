/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.constraintlayout.core.dsl

import androidx.compose.ui.text.intl.Locale

/**
 * Provides the API for creating a Constraint Object for use in the Core
 * ConstraintLayout & MotionLayout system
 */
class Constraint(val id: String) {

    inner class VAnchor internal constructor(override val mSide: Side.VSide) : Anchor(mSide)

    inner class HAnchor internal constructor(override val mSide: Side.HSide) : Anchor(mSide)

    open inner class Anchor internal constructor(open val mSide: Side) {
        val id = this@Constraint.id

        var mConnection: Anchor? = null
        var mMargin = 0
        var mGoneMargin = Int.MIN_VALUE
        val parent: Constraint
            get() = this@Constraint

        fun build(builder: StringBuilder) {
            if (mConnection != null) {
                builder.append(mSide.toString().lowercase())
                    .append(":").append(this).append(",\n")
            }
        }

        override fun toString(): String {
            val ret: StringBuilder = StringBuilder("[")
            if (mConnection != null) {
                ret.append("'").append(mConnection!!.id).append("',")
                    .append("'").append(mConnection!!.mSide.toString().lowercase())
                    .append("'")
            }
            if (mMargin != 0) {
                ret.append(",").append(mMargin)
            }
            if (mGoneMargin != Int.MIN_VALUE) {
                if (mMargin == 0) {
                    ret.append(",0,").append(mGoneMargin)
                } else {
                    ret.append(",").append(mGoneMargin)
                }
            }
            ret.append("]")
            return ret.toString()
        }
    }

    enum class Behaviour {
        SPREAD, WRAP, PERCENT, RATIO, RESOLVED
    }

    enum class ChainMode {
        SPREAD, SPREAD_INSIDE, PACKED
    }

    sealed interface Side {
        sealed interface HSide : Side
        sealed interface VSide : Side

        object Top : VSide
        object Bottom: VSide
        object Baseline: VSide

        object Left : HSide
        object Right: HSide
        object Start: HSide
        object End: HSide
    }

    var helperType: String? = null
    var helperJason: String? = null
    /**
     * get left anchor
     *
     * @return left anchor
     */
    /**
     * set left anchor
     *
     * @param left left anchor
     */
    var left = HAnchor(Side.Left)
    /**
     * get right anchor
     *
     * @return right anchor
     */
    /**
     * set right anchor
     *
     * @param right right anchor
     */
    var right = HAnchor(Side.Right)
    /**
     * get top anchor
     *
     * @return top anchor
     */
    /**
     * set top anchor
     *
     * @param top top anchor
     */
    var top = VAnchor(Side.Top)
    /**
     * get bottom anchor
     *
     * @return bottom anchor
     */
    /**
     * set bottom anchor
     *
     * @param bottom bottom anchor
     */
    var bottom = VAnchor(Side.Bottom)
    /**
     * get start anchor
     *
     * @return start anchor
     */
    /**
     * set start anchor
     *
     * @param start start anchor
     */
    var start = HAnchor(Side.Start)
    /**
     * get end anchor
     *
     * @return end anchor
     */
    /**
     * set end anchor
     *
     * @param end end anchor
     */
    var end = HAnchor(Side.End)
    /**
     * get baseline anchor
     *
     * @return baseline anchor
     */
    /**
     * set baseline anchor
     *
     * @param baseline baseline anchor
     */
    var baseline = VAnchor(Side.Baseline)
    /**
     * get width
     * @return width
     */
    /**
     * set width
     *
     * @param width
     */
    var width = UNSET
    /**
     * get height
     * @return height
     */
    /**
     * set height
     *
     * @param height
     */
    var height = UNSET
    /**
     * get horizontalBias
     *
     * @return horizontalBias
     */
    /**
     * set horizontalBias
     *
     * @param horizontalBias
     */
    var horizontalBias = Float.NaN
    /**
     * get verticalBias
     *
     * @return verticalBias
     */
    /**
     * set verticalBias
     *
     * @param verticalBias
     */
    var verticalBias = Float.NaN
    /**
     * get dimensionRatio
     *
     * @return dimensionRatio
     */
    /**
     * set dimensionRatio
     *
     * @param dimensionRatio
     */
    var dimensionRatio: String? = null
    /**
     * get circleConstraint
     *
     * @return circleConstraint
     */
    /**
     * set circleConstraint
     *
     * @param circleConstraint
     */
    var circleConstraint: String? = null
    /**
     * get circleRadius
     *
     * @return circleRadius
     */
    /**
     * set circleRadius
     *
     * @param circleRadius
     */
    var circleRadius = Int.MIN_VALUE
    /**
     * get circleAngle
     *
     * @return circleAngle
     */
    /**
     * set circleAngle
     *
     * @param circleAngle
     */
    var circleAngle = Float.NaN
    /**
     * get editorAbsoluteX
     * @return editorAbsoluteX
     */
    /**
     * set editorAbsoluteX
     * @param editorAbsoluteX
     */
    var editorAbsoluteX = Int.MIN_VALUE
    /**
     * get editorAbsoluteY
     * @return editorAbsoluteY
     */
    /**
     * set editorAbsoluteY
     * @param editorAbsoluteY
     */
    var editorAbsoluteY = Int.MIN_VALUE
    /**
     * get verticalWeight
     *
     * @return verticalWeight
     */
    /**
     * set verticalWeight
     *
     * @param verticalWeight
     */
    var verticalWeight = Float.NaN
    /**
     * get horizontalWeight
     *
     * @return horizontalWeight
     */
    /**
     * set horizontalWeight
     *
     * @param horizontalWeight
     */
    var horizontalWeight = Float.NaN
    /**
     * get horizontalChainStyle
     *
     * @return horizontalChainStyle
     */
    /**
     * set horizontalChainStyle
     *
     * @param horizontalChainStyle
     */
    var horizontalChainStyle: ChainMode? = null
    /**
     * get verticalChainStyle
     *
     * @return verticalChainStyle
     */
    /**
     * set verticalChainStyle
     *
     * @param verticalChainStyle
     */
    var verticalChainStyle: ChainMode? = null
    /**
     * get widthDefault
     *
     * @return widthDefault
     */
    /**
     * set widthDefault
     *
     * @param widthDefault
     */
    var widthDefault: Behaviour? = null
    /**
     * get heightDefault
     *
     * @return heightDefault
     */
    /**
     * set heightDefault
     *
     * @param heightDefault
     */
    var heightDefault: Behaviour? = null
    /**
     * get widthMax
     *
     * @return widthMax
     */
    /**
     * set widthMax
     *
     * @param widthMax
     */
    var widthMax = UNSET
    /**
     * get heightMax
     *
     * @return heightMax
     */
    /**
     * set heightMax
     *
     * @param heightMax
     */
    var heightMax = UNSET
    /**
     * get widthMin
     *
     * @return widthMin
     */
    /**
     * set widthMin
     *
     * @param widthMin
     */
    var widthMin = UNSET
    /**
     * get heightMin
     *
     * @return heightMin
     */
    /**
     * set heightMin
     *
     * @param heightMin
     */
    var heightMin = UNSET
    /**
     * get widthPercent
     *
     * @return
     */
    /**
     * set widthPercent
     *
     * @param widthPercent
     */
    var widthPercent = Float.NaN
    /**
     * get heightPercent
     *
     * @return heightPercent
     */
    /**
     * set heightPercent
     *
     * @param heightPercent
     */
    var heightPercent = Float.NaN
    /**
     * get referenceIds
     *
     * @return referenceIds
     */
    /**
     * set referenceIds
     *
     * @param referenceIds
     */
    var referenceIds: Array<String>? = null
    /**
     * is constrainedWidth
     *
     * @return true if width constrained
     */
    /**
     * set constrainedWidth
     *
     * @param constrainedWidth
     */
    var isConstrainedWidth = false
    /**
     * is constrainedHeight
     *
     * @return true if height constrained
     */
    /**
     * set constrainedHeight
     *
     * @param constrainedHeight
     */
    var isConstrainedHeight = false

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     */
    fun connectTop(anchor: VAnchor?, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        top.mConnection = anchor
        top.mMargin = margin
        top.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     */
    fun connectLeft(anchor: HAnchor?, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        left.mConnection = anchor
        left.mMargin = margin
        left.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     */
    fun connectRight(anchor: HAnchor?, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        right.mConnection = anchor
        right.mMargin = margin
        right.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     */
    fun connectStart(anchor: HAnchor?, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        start.mConnection = anchor
        start.mMargin = margin
        start.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     */
    fun connectEnd(anchor: HAnchor?, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        end.mConnection = anchor
        end.mMargin = margin
        end.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     */
    fun connectBottom(anchor: VAnchor?, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        bottom.mConnection = anchor
        bottom.mMargin = margin
        bottom.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     */
    fun connectBaseline(anchor: VAnchor?, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        baseline.mConnection = anchor
        end.mMargin = margin
        end.mGoneMargin = goneMargin
    }

    /**
     * convert a String array into a String representation
     *
     * @param str String array to be converted
     * @return a String representation of the input array.
     */
    fun convertStringArrayToString(str: Array<String>): String {
        val ret: StringBuilder = StringBuilder("[")
        for (i in str.indices) {
            ret.append(if (i == 0) "'" else ",'")
            ret.append(str[i])
            ret.append("'")
        }
        ret.append("]")
        return ret.toString()
    }

    protected fun append(builder: StringBuilder, name: String?, value: Float) {
        if (value.isNaN()) {
            return
        }
        builder.append(name)
        builder.append(":").append(value).append(",\n")
    }

    override fun toString(): String {
        val ret: StringBuilder = StringBuilder(
            """
    ${id}:{
    
    """.trimIndent()
        )
        left.build(ret)
        right.build(ret)
        top.build(ret)
        bottom.build(ret)
        start.build(ret)
        end.build(ret)
        baseline.build(ret)
        if (width != UNSET) {
            ret.append("width:").append(width).append(",\n")
        }
        if (height != UNSET) {
            ret.append("height:").append(height).append(",\n")
        }
        append(ret, "horizontalBias", horizontalBias)
        append(ret, "verticalBias", verticalBias)
        if (dimensionRatio != null) {
            ret.append("dimensionRatio:'").append(dimensionRatio).append("',\n")
        }
        if (circleConstraint != null) {
            if (!circleAngle.isNaN() || circleRadius != Int.MIN_VALUE) {
                ret.append("circular:['").append(circleConstraint).append("'")
                if (!circleAngle.isNaN()) {
                    ret.append(",").append(circleAngle)
                }
                if (circleRadius != Int.MIN_VALUE) {
                    if (circleAngle.isNaN()) {
                        ret.append(",0,").append(circleRadius)
                    } else {
                        ret.append(",").append(circleRadius)
                    }
                }
                ret.append("],\n")
            }
        }
        append(ret, "verticalWeight", verticalWeight)
        append(ret, "horizontalWeight", horizontalWeight)
        if (horizontalChainStyle != null) {
            ret.append("horizontalChainStyle:'").append(chainModeMap[horizontalChainStyle])
                .append("',\n")
        }
        if (verticalChainStyle != null) {
            ret.append("verticalChainStyle:'").append(chainModeMap[verticalChainStyle])
                .append("',\n")
        }
        if (widthDefault != null) {
            if (widthMax == UNSET && widthMin == UNSET) {
                ret.append("width:'").append(widthDefault.toString().lowercase())
                    .append("',\n")
            } else {
                ret.append("width:{value:'").append(widthDefault.toString().lowercase())
                    .append("'")
                if (widthMax != UNSET) {
                    ret.append(",max:").append(widthMax)
                }
                if (widthMin != UNSET) {
                    ret.append(",min:").append(widthMin)
                }
                ret.append("},\n")
            }
        }
        if (heightDefault != null) {
            if (heightMax == UNSET && heightMin == UNSET) {
                ret.append("height:'").append(heightDefault.toString().lowercase())
                    .append("',\n")
            } else {
                ret.append("height:{value:'").append(heightDefault.toString().lowercase())
                    .append("'")
                if (heightMax != UNSET) {
                    ret.append(",max:").append(heightMax)
                }
                if (heightMin != UNSET) {
                    ret.append(",min:").append(heightMin)
                }
                ret.append("},\n")
            }
        }
        if (!widthPercent.toDouble().isNaN()) {
            ret.append("width:'").append(widthPercent.toInt()).append("%',\n")
        }
        if (!heightPercent.toDouble().isNaN()) {
            ret.append("height:'").append(heightPercent.toInt()).append("%',\n")
        }
        if (referenceIds != null) {
            ret.append("mReferenceIds:")
                .append(convertStringArrayToString(referenceIds!!))
                .append(",\n")
        }
        if (isConstrainedWidth) {
            ret.append("constrainedWidth:").append(isConstrainedWidth).append(",\n")
        }
        if (isConstrainedHeight) {
            ret.append("constrainedHeight:").append(isConstrainedHeight).append(",\n")
        }
        ret.append("},\n")
        return ret.toString()
    }

    companion object {
        var UNSET = Int.MIN_VALUE
        var chainModeMap: MutableMap<ChainMode, String> = HashMap<ChainMode, String>()

        init {
            chainModeMap[ChainMode.SPREAD] = "spread"
            chainModeMap[ChainMode.SPREAD_INSIDE] = "spread_inside"
            chainModeMap[ChainMode.PACKED] = "packed"
        }
    }
}
