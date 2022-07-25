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
package androidx.constraintlayout.core.motion.key

import androidx.constraintlayout.core.motion.CustomVariable
import androidx.constraintlayout.core.motion.utils.SplineSet
import androidx.constraintlayout.core.motion.utils.SplineSet.CustomSpline
import androidx.constraintlayout.core.motion.utils.TypedValues
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType

class MotionKeyAttributes : MotionKey() {
    private var mTransitionEasing: String? = null
    var curveFit = -1
        private set
    private var mVisibility = 0
    private var mAlpha = Float.NaN
    private var mElevation = Float.NaN
    private var mRotation = Float.NaN
    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mPivotX = Float.NaN
    private var mPivotY = Float.NaN
    private var mTransitionPathRotate = Float.NaN
    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN
    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN
    private var mProgress = Float.NaN

    init {
        mType = KEY_TYPE
        mCustom = HashMap<String, CustomVariable>()
    }

    override fun getAttributeNames(attributes: HashSet<String>) {
        if (!mAlpha.isNaN()) {
            attributes.add(AttributesType.Companion.S_ALPHA)
        }
        if (!mElevation.isNaN()) {
            attributes.add(AttributesType.Companion.S_ELEVATION)
        }
        if (!mRotation.isNaN()) {
            attributes.add(AttributesType.Companion.S_ROTATION_Z)
        }
        if (!mRotationX.isNaN()) {
            attributes.add(AttributesType.Companion.S_ROTATION_X)
        }
        if (!mRotationY.isNaN()) {
            attributes.add(AttributesType.Companion.S_ROTATION_Y)
        }
        if (!mPivotX.isNaN()) {
            attributes.add(AttributesType.Companion.S_PIVOT_X)
        }
        if (!mPivotY.isNaN()) {
            attributes.add(AttributesType.Companion.S_PIVOT_Y)
        }
        if (!mTranslationX.isNaN()) {
            attributes.add(AttributesType.Companion.S_TRANSLATION_X)
        }
        if (!mTranslationY.isNaN()) {
            attributes.add(AttributesType.Companion.S_TRANSLATION_Y)
        }
        if (!mTranslationZ.isNaN()) {
            attributes.add(AttributesType.Companion.S_TRANSLATION_Z)
        }
        if (!mTransitionPathRotate.isNaN()) {
            attributes.add(AttributesType.Companion.S_PATH_ROTATE)
        }
        if (!mScaleX.isNaN()) {
            attributes.add(AttributesType.Companion.S_SCALE_X)
        }
        if (!mScaleY.isNaN()) {
            attributes.add(AttributesType.Companion.S_SCALE_Y)
        }
        if (!mProgress.isNaN()) {
            attributes.add(AttributesType.Companion.S_PROGRESS)
        }
        if (mCustom!!.size > 0) {
            for (s in mCustom!!.keys) {
                attributes.add(TypedValues.Companion.S_CUSTOM + "," + s)
            }
        }
    }

    override fun addValues(splines: HashMap<String, SplineSet>) {
        for (s in splines.keys) {
            val splineSet: SplineSet = splines.get(s) ?: continue
            // TODO support custom
            if (s.startsWith(AttributesType.Companion.S_CUSTOM) == true) {
                val cKey: String = s.substring(AttributesType.Companion.S_CUSTOM.length + 1)
                val cValue = mCustom?.get(cKey)
                if (cValue != null) {
                    (splineSet as CustomSpline).setPoint(framePosition, cValue)
                }
                continue
            }
            when (s) {
                AttributesType.Companion.S_ALPHA -> if (!mAlpha.isNaN()) {
                    splineSet.setPoint(framePosition, mAlpha)
                }

                AttributesType.Companion.S_ELEVATION -> if (!mElevation.isNaN()) {
                    splineSet.setPoint(framePosition, mElevation)
                }

                AttributesType.Companion.S_ROTATION_Z -> if (!mRotation.isNaN()) {
                    splineSet.setPoint(framePosition, mRotation)
                }

                AttributesType.Companion.S_ROTATION_X -> if (!mRotationX.isNaN()) {
                    splineSet.setPoint(framePosition, mRotationX)
                }

                AttributesType.Companion.S_ROTATION_Y -> if (!mRotationY.isNaN()) {
                    splineSet.setPoint(framePosition, mRotationY)
                }

                AttributesType.Companion.S_PIVOT_X -> if (!mRotationX.isNaN()) {
                    splineSet.setPoint(framePosition, mPivotX)
                }

                AttributesType.Companion.S_PIVOT_Y -> if (!mRotationY.isNaN()) {
                    splineSet.setPoint(framePosition, mPivotY)
                }

                AttributesType.Companion.S_PATH_ROTATE -> if (!mTransitionPathRotate.isNaN()) {
                    splineSet.setPoint(framePosition, mTransitionPathRotate)
                }

                AttributesType.Companion.S_SCALE_X -> if (!mScaleX.isNaN()) {
                    splineSet.setPoint(framePosition, mScaleX)
                }

                AttributesType.Companion.S_SCALE_Y -> if (!mScaleY.isNaN()) {
                    splineSet.setPoint(framePosition, mScaleY)
                }

                AttributesType.Companion.S_TRANSLATION_X -> if (!mTranslationX.isNaN()) {
                    splineSet.setPoint(framePosition, mTranslationX)
                }

                AttributesType.Companion.S_TRANSLATION_Y -> if (!mTranslationY.isNaN()) {
                    splineSet.setPoint(framePosition, mTranslationY)
                }

                AttributesType.Companion.S_TRANSLATION_Z -> if (!mTranslationZ.isNaN()) {
                    splineSet.setPoint(framePosition, mTranslationZ)
                }

                AttributesType.Companion.S_PROGRESS -> if (!mProgress.isNaN()) {
                    splineSet.setPoint(framePosition, mProgress)
                }

                else -> println("not supported by KeyAttributes $s")
            }
        }
    }

    override fun clone(): MotionKey? {
        return null
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            AttributesType.Companion.TYPE_VISIBILITY -> mVisibility = value
            AttributesType.Companion.TYPE_CURVE_FIT -> curveFit = value
            TypedValues.Companion.TYPE_FRAME_POSITION -> framePosition = value
            else -> if (!setValue(type, value)) {
                return super.setValue(type, value)
            }
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            AttributesType.Companion.TYPE_ALPHA -> mAlpha = value
            AttributesType.Companion.TYPE_TRANSLATION_X -> mTranslationX = value
            AttributesType.Companion.TYPE_TRANSLATION_Y -> mTranslationY = value
            AttributesType.Companion.TYPE_TRANSLATION_Z -> mTranslationZ = value
            AttributesType.Companion.TYPE_ELEVATION -> mElevation = value
            AttributesType.Companion.TYPE_ROTATION_X -> mRotationX = value
            AttributesType.Companion.TYPE_ROTATION_Y -> mRotationY = value
            AttributesType.Companion.TYPE_ROTATION_Z -> mRotation = value
            AttributesType.Companion.TYPE_SCALE_X -> mScaleX = value
            AttributesType.Companion.TYPE_SCALE_Y -> mScaleY = value
            AttributesType.Companion.TYPE_PIVOT_X -> mPivotX = value
            AttributesType.Companion.TYPE_PIVOT_Y -> mPivotY = value
            AttributesType.Companion.TYPE_PROGRESS -> mProgress = value
            AttributesType.Companion.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            TypedValues.Companion.TYPE_FRAME_POSITION -> mTransitionPathRotate = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setInterpolation(interpolation: HashMap<String, Int>) {
        if (!mAlpha.isNaN()) {
            interpolation.put(AttributesType.Companion.S_ALPHA, curveFit)
        }
        if (!mElevation.isNaN()) {
            interpolation.put(AttributesType.Companion.S_ELEVATION, curveFit)
        }
        if (!mRotation.isNaN()) {
            interpolation.put(AttributesType.Companion.S_ROTATION_Z, curveFit)
        }
        if (!mRotationX.isNaN()) {
            interpolation.put(AttributesType.Companion.S_ROTATION_X, curveFit)
        }
        if (!mRotationY.isNaN()) {
            interpolation.put(AttributesType.Companion.S_ROTATION_Y, curveFit)
        }
        if (!mPivotX.isNaN()) {
            interpolation.put(AttributesType.Companion.S_PIVOT_X, curveFit)
        }
        if (!mPivotY.isNaN()) {
            interpolation.put(AttributesType.Companion.S_PIVOT_Y, curveFit)
        }
        if (!mTranslationX.isNaN()) {
            interpolation.put(AttributesType.Companion.S_TRANSLATION_X, curveFit)
        }
        if (!mTranslationY.isNaN()) {
            interpolation.put(AttributesType.Companion.S_TRANSLATION_Y, curveFit)
        }
        if (!mTranslationZ.isNaN()) {
            interpolation.put(AttributesType.Companion.S_TRANSLATION_Z, curveFit)
        }
        if (!mTransitionPathRotate.isNaN()) {
            interpolation.put(AttributesType.Companion.S_PATH_ROTATE, curveFit)
        }
        if (!mScaleX.isNaN()) {
            interpolation.put(AttributesType.Companion.S_SCALE_X, curveFit)
        }
        if (!mScaleY.isNaN()) {
            interpolation.put(AttributesType.Companion.S_SCALE_Y, curveFit)
        }
        if (!mProgress.isNaN()) {
            interpolation.put(AttributesType.Companion.S_PROGRESS, curveFit)
        }
        if (mCustom!!.size > 0) {
            for (s in mCustom!!.keys) {
                interpolation.put(AttributesType.Companion.S_CUSTOM + "," + s, curveFit)
            }
        }
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: String?): Boolean {
        when (type) {
            AttributesType.Companion.TYPE_EASING -> mTransitionEasing = value
            TypedValues.Companion.TYPE_TARGET -> mTargetString = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    override fun getId(name: String?): Int {
        return AttributesType.Companion.getId(name)
    }

    /**
     * @TODO: add description
     */
    fun printAttributes() {
        val nameSet: HashSet<String> = HashSet()
        getAttributeNames(nameSet)
        println(" ------------- $framePosition -------------")
        val names: Array<String?> = nameSet.toTypedArray()
        for (i in names.indices) {
            val id: Int = AttributesType.Companion.getId(names[i])
            println(names[i] + ":" + getFloatValue(id))
        }
    }

    private fun getFloatValue(id: Int): Float {
        return when (id) {
            AttributesType.Companion.TYPE_ALPHA -> mAlpha
            AttributesType.Companion.TYPE_TRANSLATION_X -> mTranslationX
            AttributesType.Companion.TYPE_TRANSLATION_Y -> mTranslationY
            AttributesType.Companion.TYPE_TRANSLATION_Z -> mTranslationZ
            AttributesType.Companion.TYPE_ELEVATION -> mElevation
            AttributesType.Companion.TYPE_ROTATION_X -> mRotationX
            AttributesType.Companion.TYPE_ROTATION_Y -> mRotationY
            AttributesType.Companion.TYPE_ROTATION_Z -> mRotation
            AttributesType.Companion.TYPE_SCALE_X -> mScaleX
            AttributesType.Companion.TYPE_SCALE_Y -> mScaleY
            AttributesType.Companion.TYPE_PIVOT_X -> mPivotX
            AttributesType.Companion.TYPE_PIVOT_Y -> mPivotY
            AttributesType.Companion.TYPE_PROGRESS -> mProgress
            AttributesType.Companion.TYPE_PATH_ROTATE -> mTransitionPathRotate
            TypedValues.Companion.TYPE_FRAME_POSITION -> framePosition.toFloat()
            else -> Float.NaN
        }
    }

    companion object {
        const val NAME = "KeyAttribute"
        private const val TAG = "KeyAttributes"
        private const val DEBUG = false
        const val KEY_TYPE = 1
    }
}
