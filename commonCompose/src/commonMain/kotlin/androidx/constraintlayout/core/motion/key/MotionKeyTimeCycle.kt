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
import androidx.constraintlayout.core.motion.utils.*
import androidx.constraintlayout.core.motion.utils.TimeCycleSplineSet.CustomVarSet
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.core.motion.utils.TypedValues.CycleType

class MotionKeyTimeCycle : MotionKey() {
    private var mTransitionEasing: String? = null
    private var mCurveFit = -1
    private var mAlpha = Float.NaN
    private var mElevation = Float.NaN
    private var mRotation = Float.NaN
    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mTransitionPathRotate = Float.NaN
    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN
    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN
    private var mProgress = Float.NaN
    private var mWaveShape = 0

    // TODO add support of custom wave shapes in KeyTimeCycle
    private var mCustomWaveShape: String? = null
    private var mWavePeriod = Float.NaN
    private var mWaveOffset = 0f

    init {
        mType = KEY_TYPE
        mCustom = HashMap<String, CustomVariable>()
    }

    /**
     * @TODO: add description
     */
    fun addTimeValues(splines: HashMap<String, TimeCycleSplineSet>) {
        for (s in splines.keys) {
            val splineSet: TimeCycleSplineSet = splines[s] ?: continue
            if (s.startsWith(CUSTOM)) {
                val cKey: String = s.substring(CUSTOM.length + 1)
                val cValue = mCustom!![cKey]
                if (cValue != null) {
                    (splineSet as CustomVarSet)
                        .setPoint(framePosition, cValue, mWavePeriod, mWaveShape, mWaveOffset)
                }
                continue
            }
            when (s) {
                AttributesType.S_ALPHA -> if (!mAlpha.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mAlpha, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_ROTATION_X -> if (!mRotationX.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mRotationX, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_ROTATION_Y -> if (!mRotationY.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mRotationY, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_ROTATION_Z -> if (!mRotation.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mRotation, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_PATH_ROTATE -> if (!mTransitionPathRotate.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mTransitionPathRotate, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_SCALE_X -> if (!mScaleX.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mScaleX, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_SCALE_Y -> if (!mScaleY.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mScaleY, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_TRANSLATION_X -> if (!mTranslationX.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mTranslationX, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_TRANSLATION_Y -> if (!mTranslationY.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mTranslationY, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_TRANSLATION_Z -> if (!mTranslationZ.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mTranslationZ, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_ELEVATION -> if (!mTranslationZ.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mTranslationZ, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                AttributesType.S_PROGRESS -> if (!mProgress.isNaN()) {
                    splineSet.setPoint(
                        framePosition,
                        mProgress, mWavePeriod, mWaveShape, mWaveOffset
                    )
                }

                else -> Utils.loge("KeyTimeCycles", "UNKNOWN addValues \"$s\"")
            }
        }
    }

    override fun addValues(splines: HashMap<String, SplineSet>) {}

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            TypedValues.TYPE_FRAME_POSITION -> framePosition = value
            CycleType.TYPE_WAVE_SHAPE -> mWaveShape = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            CycleType.TYPE_ALPHA -> mAlpha = value
            CycleType.TYPE_CURVE_FIT -> mCurveFit = toInt(value)
            CycleType.TYPE_ELEVATION -> mElevation = toFloat(value)
            CycleType.TYPE_PROGRESS -> mProgress = toFloat(value)
            CycleType.TYPE_ROTATION_Z -> mRotation = toFloat(value)
            CycleType.TYPE_ROTATION_X -> mRotationX = toFloat(value)
            CycleType.TYPE_ROTATION_Y -> mRotationY = toFloat(value)
            CycleType.TYPE_SCALE_X -> mScaleX = toFloat(value)
            CycleType.TYPE_SCALE_Y -> mScaleY = toFloat(value)
            CycleType.TYPE_PATH_ROTATE -> mTransitionPathRotate = toFloat(value)
            CycleType.TYPE_TRANSLATION_X -> mTranslationX = toFloat(value)
            CycleType.TYPE_TRANSLATION_Y -> mTranslationY = toFloat(value)
            CycleType.TYPE_TRANSLATION_Z -> mTranslationZ = toFloat(value)
            CycleType.TYPE_WAVE_PERIOD -> mWavePeriod = toFloat(value)
            CycleType.TYPE_WAVE_OFFSET -> mWaveOffset = toFloat(value)
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: String): Boolean {
        when (type) {
            CycleType.TYPE_WAVE_SHAPE -> {
                mWaveShape = Oscillator.CUSTOM
                mCustomWaveShape = value
            }

            CycleType.TYPE_EASING -> mTransitionEasing = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Boolean): Boolean {
        return super.setValue(type, value)
    }

    /**
     * @TODO: add description
     */
    override fun copy(src: MotionKey): MotionKey {
        super.copy(src)
        val k = src as MotionKeyTimeCycle
        mTransitionEasing = k.mTransitionEasing
        mCurveFit = k.mCurveFit
        mWaveShape = k.mWaveShape
        mWavePeriod = k.mWavePeriod
        mWaveOffset = k.mWaveOffset
        mProgress = k.mProgress
        mAlpha = k.mAlpha
        mElevation = k.mElevation
        mRotation = k.mRotation
        mTransitionPathRotate = k.mTransitionPathRotate
        mRotationX = k.mRotationX
        mRotationY = k.mRotationY
        mScaleX = k.mScaleX
        mScaleY = k.mScaleY
        mTranslationX = k.mTranslationX
        mTranslationY = k.mTranslationY
        mTranslationZ = k.mTranslationZ
        return this
    }

    override fun getAttributeNames(attributes: HashSet<String>) {
        if (!mAlpha.isNaN()) {
            attributes.add(CycleType.S_ALPHA)
        }
        if (!mElevation.isNaN()) {
            attributes.add(CycleType.S_ELEVATION)
        }
        if (!mRotation.isNaN()) {
            attributes.add(CycleType.S_ROTATION_Z)
        }
        if (!mRotationX.isNaN()) {
            attributes.add(CycleType.S_ROTATION_X)
        }
        if (!mRotationY.isNaN()) {
            attributes.add(CycleType.S_ROTATION_Y)
        }
        if (!mScaleX.isNaN()) {
            attributes.add(CycleType.S_SCALE_X)
        }
        if (!mScaleY.isNaN()) {
            attributes.add(CycleType.S_SCALE_Y)
        }
        if (!mTransitionPathRotate.isNaN()) {
            attributes.add(CycleType.S_PATH_ROTATE)
        }
        if (!mTranslationX.isNaN()) {
            attributes.add(CycleType.S_TRANSLATION_X)
        }
        if (!mTranslationY.isNaN()) {
            attributes.add(CycleType.S_TRANSLATION_Y)
        }
        if (!mTranslationZ.isNaN()) {
            attributes.add(CycleType.S_TRANSLATION_Z)
        }
        if (mCustom!!.size > 0) {
            for (s in mCustom!!.keys) {
                attributes.add(TypedValues.S_CUSTOM + "," + s)
            }
        }
    }

    /**
     * @TODO: add description
     */
    override fun clone(): MotionKey {
        return MotionKeyTimeCycle().copy(this)
    }

    override fun getId(name: String?): Int {
        return CycleType.getId(name)
    }

    companion object {
        private const val NAME = "KeyTimeCycle"
        private const val TAG = NAME
        const val KEY_TYPE = 3
    }
}
