/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.constraintlayout.core.motion

import kotlin.math.pow

/*
 * Copyright (C) 2017 The Android Open Source Project
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
 */ /**
 * Defines non standard Attributes
 *
 * @DoNotShow
 */
class CustomAttribute {
    private var mMethod = false
    var mName: String
    var type: AttributeType
        private set
    private var mIntegerValue = 0
    private var mFloatValue = 0f
    private var mStringValue: String? = null
    var mBooleanValue = false
    private var mColorValue = 0

    enum class AttributeType {
        INT_TYPE, FLOAT_TYPE, COLOR_TYPE, COLOR_DRAWABLE_TYPE, STRING_TYPE, BOOLEAN_TYPE, DIMENSION_TYPE, REFERENCE_TYPE
    }

    /**
     * Continuous types are interpolated they are fired only at
     */
    val isContinuous: Boolean
        get() = when (type) {
            AttributeType.REFERENCE_TYPE, AttributeType.BOOLEAN_TYPE, AttributeType.STRING_TYPE -> false
            else -> true
        }

    fun setFloatValue(value: Float) {
        mFloatValue = value
    }

    fun setColorValue(value: Int) {
        mColorValue = value
    }

    fun setIntValue(value: Int) {
        mIntegerValue = value
    }

    fun setStringValue(value: String?) {
        mStringValue = value
    }

    /**
     * The number of interpolation values that need to be interpolated
     * Typically 1 but 3 for colors.
     *
     * @return Typically 1 but 3 for colors.
     */
    fun numberOfInterpolatedValues(): Int {
        return when (type) {
            AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> 4
            else -> 1
        }
    }

    /**
     * Transforms value to a float for the purpose of interpolation
     *
     * @return interpolation value
     */
    val valueToInterpolate: Float
        get() = when (type) {
            AttributeType.INT_TYPE -> mIntegerValue.toFloat()
            AttributeType.FLOAT_TYPE -> mFloatValue
            AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> throw RuntimeException(
                "Color does not have a single color to interpolate"
            )

            AttributeType.STRING_TYPE -> throw RuntimeException(
                "Cannot interpolate String"
            )

            AttributeType.BOOLEAN_TYPE -> if (mBooleanValue) 1f else 0f
            AttributeType.DIMENSION_TYPE -> mFloatValue
            else -> Float.NaN
        }

    /**
     * @TODO: add description
     */
    fun getValuesToInterpolate(ret: FloatArray) {
        when (type) {
            AttributeType.INT_TYPE -> ret[0] = mIntegerValue.toFloat()
            AttributeType.FLOAT_TYPE -> ret[0] = mFloatValue
            AttributeType.COLOR_DRAWABLE_TYPE, AttributeType.COLOR_TYPE -> {
                val a = 0xFF and (mColorValue shr 24)
                val r = 0xFF and (mColorValue shr 16)
                val g = 0xFF and (mColorValue shr 8)
                val b = 0xFF and mColorValue
                val f_r: Float = (r / 255.0f).toDouble().pow(2.2).toFloat()
                val f_g: Float = (g / 255.0f).toDouble().pow(2.2).toFloat()
                val f_b: Float = (b / 255.0f).toDouble().pow(2.2).toFloat()
                ret[0] = f_r
                ret[1] = f_g
                ret[2] = f_b
                ret[3] = a / 255f
            }

            AttributeType.STRING_TYPE -> throw RuntimeException("Color does not have a single color to interpolate")
            AttributeType.BOOLEAN_TYPE -> ret[0] = (if (mBooleanValue) 1 else 0).toFloat()
            AttributeType.DIMENSION_TYPE -> ret[0] = mFloatValue
            else -> {}
        }
    }

    /**
     * @TODO: add description
     */
    fun setValue(value: FloatArray) {
        when (type) {
            AttributeType.REFERENCE_TYPE, AttributeType.INT_TYPE -> mIntegerValue = value[0].toInt()
            AttributeType.FLOAT_TYPE -> mFloatValue = value[0]
            AttributeType.COLOR_DRAWABLE_TYPE, AttributeType.COLOR_TYPE -> {
                mColorValue = hsvToRgb(value[0], value[1], value[2])
                mColorValue = mColorValue and 0xFFFFFF or (clamp((0xFF * value[3]).toInt()) shl 24)
            }

            AttributeType.STRING_TYPE -> throw RuntimeException("Color does not have a single color to interpolate")
            AttributeType.BOOLEAN_TYPE -> mBooleanValue = value[0] > 0.5
            AttributeType.DIMENSION_TYPE -> mFloatValue = value[0]
            else -> {}
        }
    }

    /**
     * test if the two attributes are different
     */
    fun diff(customAttribute: CustomAttribute?): Boolean {
        return if (customAttribute == null || type != customAttribute.type) {
            false
        } else when (type) {
            AttributeType.INT_TYPE, AttributeType.REFERENCE_TYPE -> mIntegerValue == customAttribute.mIntegerValue
            AttributeType.FLOAT_TYPE -> mFloatValue == customAttribute.mFloatValue
            AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> mColorValue == customAttribute.mColorValue
            AttributeType.STRING_TYPE -> mIntegerValue == customAttribute.mIntegerValue
            AttributeType.BOOLEAN_TYPE -> mBooleanValue == customAttribute.mBooleanValue
            AttributeType.DIMENSION_TYPE -> mFloatValue == customAttribute.mFloatValue
            else -> false
        }
    }

    constructor(name: String, attributeType: AttributeType) {
        mName = name
        type = attributeType
    }

    constructor(name: String, attributeType: AttributeType, value: Any, method: Boolean) {
        mName = name
        type = attributeType
        mMethod = method
        setValue(value)
    }

    constructor(source: CustomAttribute, value: Any) {
        mName = source.mName
        type = source.type
        setValue(value)
    }

    /**
     * @TODO: add description
     */
    fun setValue(value: Any) {
        when (type) {
            AttributeType.REFERENCE_TYPE, AttributeType.INT_TYPE -> mIntegerValue = value as Int
            AttributeType.FLOAT_TYPE -> mFloatValue = value as Float
            AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> mColorValue = value as Int
            AttributeType.STRING_TYPE -> mStringValue = value as String
            AttributeType.BOOLEAN_TYPE -> mBooleanValue = value as Boolean
            AttributeType.DIMENSION_TYPE -> mFloatValue = value as Float
            else -> {}
        }
    }

    companion object {
        private const val TAG = "TransitionLayout"

        /**
         * @TODO: add description
         */
        fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
            val h = (hue * 6).toInt()
            val f = hue * 6 - h
            val p = (0.5f + 255 * value * (1 - saturation)).toInt()
            val q = (0.5f + 255 * value * (1 - f * saturation)).toInt()
            val t = (0.5f + 255 * value * (1 - (1 - f) * saturation)).toInt()
            val v = (0.5f + 255 * value).toInt()
            return when (h) {
                0 -> -0x1000000 or (v shl 16) + (t shl 8) + p
                1 -> -0x1000000 or (q shl 16) + (v shl 8) + p
                2 -> -0x1000000 or (p shl 16) + (v shl 8) + t
                3 -> -0x1000000 or (p shl 16) + (q shl 8) + v
                4 -> -0x1000000 or (t shl 16) + (p shl 8) + v
                5 -> -0x1000000 or (v shl 16) + (p shl 8) + q
                else -> 0
            }
        }

        private fun clamp(c: Int): Int {
            var c = c
            val n = 255
            c = c and (c shr 31).inv()
            c -= n
            c = c and (c shr 31)
            c += n
            return c
        }
    }
}
