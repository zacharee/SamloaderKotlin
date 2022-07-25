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
package androidx.constraintlayout.core.motion.key

import androidx.constraintlayout.core.motion.CustomVariable
import androidx.constraintlayout.core.motion.utils.SplineSet
import androidx.constraintlayout.core.motion.utils.TypedValues

/**
 * Base class in an element in a KeyFrame
 *
 * @DoNotShow
 */
abstract class MotionKey : TypedValues {
    /**
     * Gets the current frame position
     */
    /**
     * sets the frame position
     */
    var framePosition: Int = UNSET
    var mTargetId: Int = UNSET
    var mTargetString: String? = null
    var mType = 0
    var mCustom: HashMap<String, CustomVariable>? = null

    /**
     * @TODO: add description
     */
    abstract fun getAttributeNames(attributes: HashSet<String>)
    fun matches(constraintTag: String?): Boolean {
        return if (mTargetString == null || constraintTag == null) false else constraintTag.matches(Regex(mTargetString!!))
    }

    /**
     * Defines method to add a a view to splines derived form this key frame.
     * The values are written to the spline
     *
     * @param splines splines to write values to
     * @DoNotShow
     */
    abstract fun addValues(splines: HashMap<String, SplineSet>)

    /**
     * Return the float given a value. If the value is a "Float" object it is casted
     *
     * @DoNotShow
     */
    fun toFloat(value: Any): Float {
        return if (value is Float) value else value.toString().toFloat()
    }

    /**
     * Return the int version of an object if the value is an Integer object it is casted.
     *
     * @DoNotShow
     */
    fun toInt(value: Any): Int {
        return if (value is Int) value else value.toString().toInt()
    }

    /**
     * Return the boolean version this object if the object is a Boolean it is casted.
     *
     * @DoNotShow
     */
    fun toBoolean(value: Any): Boolean {
        return if (value is Boolean) value else value.toString().toBoolean()
    }

    /**
     * Key frame can specify the type of interpolation it wants on various attributes
     * For each string it set it to -1, CurveFit.LINEAR or  CurveFit.SPLINE
     */
    open fun setInterpolation(interpolation: HashMap<String, Int>) {}

    /**
     * @TODO: add description
     */
    open fun copy(src: MotionKey): MotionKey? {
        framePosition = src.framePosition
        mTargetId = src.mTargetId
        mTargetString = src.mTargetString
        mType = src.mType
        return this
    }

    /**
     * @TODO: add description
     */
    abstract fun clone(): MotionKey?

    /**
     * @TODO: add description
     */
    fun setViewId(id: Int): MotionKey {
        mTargetId = id
        return this
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            TypedValues.Companion.TYPE_FRAME_POSITION -> {
                framePosition = value
                return true
            }
        }
        return false
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Float): Boolean {
        return false
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: String?): Boolean {
        when (type) {
            TypedValues.Companion.TYPE_TARGET -> {
                mTargetString = value
                return true
            }
        }
        return false
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Boolean): Boolean {
        return false
    }

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: Float) {
        mCustom?.put(name, CustomVariable(name, type, value))
    }

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: Int) {
        mCustom?.put(name, CustomVariable(name, type, value))
    }

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: Boolean) {
        mCustom?.put(name, CustomVariable(name, type, value))
    }

    /**
     * @TODO: add description
     */
    fun setCustomAttribute(name: String, type: Int, value: String?) {
        mCustom?.put(name, CustomVariable(name, type, value))
    }

    companion object {
        var UNSET = -1
        const val ALPHA = "alpha"
        const val ELEVATION = "elevation"
        const val ROTATION = "rotationZ"
        const val ROTATION_X = "rotationX"
        const val TRANSITION_PATH_ROTATE = "transitionPathRotate"
        const val SCALE_X = "scaleX"
        const val SCALE_Y = "scaleY"
        const val TRANSLATION_X = "translationX"
        const val TRANSLATION_Y = "translationY"
        const val CUSTOM = "CUSTOM"
        const val VISIBILITY = "visibility"
    }
}
