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
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.core.motion.utils.TypedValues.CycleType

class MotionKeyCycle : MotionKey() {
    private var mTransitionEasing: String? = null
    private var mCurveFit = 0
    private var mWaveShape = -1
    private var mCustomWaveShape: String? = null
    private var mWavePeriod = Float.NaN
    private var mWaveOffset = 0f
    private var mWavePhase = 0f
    private var mProgress = Float.NaN
    private var mAlpha = Float.NaN
    private var mElevation = Float.NaN
    private var mRotation = Float.NaN
    private var mTransitionPathRotate = Float.NaN
    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN
    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN

    init {
        mType = KEY_TYPE
        mCustom = HashMap<String, CustomVariable>()
    }

    override fun getAttributeNames(attributes: HashSet<String>) {
        if (!mAlpha.isNaN()) {
            attributes.add(CycleType.Companion.S_ALPHA)
        }
        if (!mElevation.isNaN()) {
            attributes.add(CycleType.Companion.S_ELEVATION)
        }
        if (!mRotation.isNaN()) {
            attributes.add(CycleType.Companion.S_ROTATION_Z)
        }
        if (!mRotationX.isNaN()) {
            attributes.add(CycleType.Companion.S_ROTATION_X)
        }
        if (!mRotationY.isNaN()) {
            attributes.add(CycleType.Companion.S_ROTATION_Y)
        }
        if (!mScaleX.isNaN()) {
            attributes.add(CycleType.Companion.S_SCALE_X)
        }
        if (!mScaleY.isNaN()) {
            attributes.add(CycleType.Companion.S_SCALE_Y)
        }
        if (!mTransitionPathRotate.isNaN()) {
            attributes.add(CycleType.Companion.S_PATH_ROTATE)
        }
        if (!mTranslationX.isNaN()) {
            attributes.add(CycleType.Companion.S_TRANSLATION_X)
        }
        if (!mTranslationY.isNaN()) {
            attributes.add(CycleType.Companion.S_TRANSLATION_Y)
        }
        if (!mTranslationZ.isNaN()) {
            attributes.add(CycleType.Companion.S_TRANSLATION_Z)
        }
        if (mCustom!!.size > 0) {
            for (s in mCustom!!.keys) {
                attributes.add(TypedValues.Companion.S_CUSTOM + "," + s)
            }
        }
    }

    override fun addValues(splines: HashMap<String, SplineSet>) {}

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Int): Boolean {
        return when (type) {
            CycleType.Companion.TYPE_CURVE_FIT -> {
                mCurveFit = value
                true
            }

            CycleType.Companion.TYPE_WAVE_SHAPE -> {
                mWaveShape = value
                true
            }

            else -> {
                val ret = setValue(type, value.toFloat())
                if (ret) {
                    true
                } else super.setValue(type, value)
            }
        }
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: String): Boolean {
        return when (type) {
            CycleType.Companion.TYPE_EASING -> {
                mTransitionEasing = value
                true
            }

            CycleType.Companion.TYPE_CUSTOM_WAVE_SHAPE -> {
                mCustomWaveShape = value
                true
            }

            else -> super.setValue(type, value)
        }
    }

    /**
     * @TODO: add description
     */
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            CycleType.Companion.TYPE_ALPHA -> mAlpha = value
            CycleType.Companion.TYPE_TRANSLATION_X -> mTranslationX = value
            CycleType.Companion.TYPE_TRANSLATION_Y -> mTranslationY = value
            CycleType.Companion.TYPE_TRANSLATION_Z -> mTranslationZ = value
            CycleType.Companion.TYPE_ELEVATION -> mElevation = value
            CycleType.Companion.TYPE_ROTATION_X -> mRotationX = value
            CycleType.Companion.TYPE_ROTATION_Y -> mRotationY = value
            CycleType.Companion.TYPE_ROTATION_Z -> mRotation = value
            CycleType.Companion.TYPE_SCALE_X -> mScaleX = value
            CycleType.Companion.TYPE_SCALE_Y -> mScaleY = value
            CycleType.Companion.TYPE_PROGRESS -> mProgress = value
            CycleType.Companion.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            CycleType.Companion.TYPE_WAVE_PERIOD -> mWavePeriod = value
            CycleType.Companion.TYPE_WAVE_OFFSET -> mWaveOffset = value
            CycleType.Companion.TYPE_WAVE_PHASE -> mWavePhase = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    /**
     * @TODO: add description
     */
    fun getValue(key: String?): Float {
        return when (key) {
            CycleType.Companion.S_ALPHA -> mAlpha
            CycleType.Companion.S_ELEVATION -> mElevation
            CycleType.Companion.S_ROTATION_Z -> mRotation
            CycleType.Companion.S_ROTATION_X -> mRotationX
            CycleType.Companion.S_ROTATION_Y -> mRotationY
            CycleType.Companion.S_PATH_ROTATE -> mTransitionPathRotate
            CycleType.Companion.S_SCALE_X -> mScaleX
            CycleType.Companion.S_SCALE_Y -> mScaleY
            CycleType.Companion.S_TRANSLATION_X -> mTranslationX
            CycleType.Companion.S_TRANSLATION_Y -> mTranslationY
            CycleType.Companion.S_TRANSLATION_Z -> mTranslationZ
            CycleType.Companion.S_WAVE_OFFSET -> mWaveOffset
            CycleType.Companion.S_WAVE_PHASE -> mWavePhase
            CycleType.Companion.S_PROGRESS -> mProgress
            else -> Float.NaN
        }
    }

    override fun clone(): MotionKey? {
        return null
    }

    override fun getId(name: String?): Int {
        when (name) {
            CycleType.Companion.S_CURVE_FIT -> return CycleType.Companion.TYPE_CURVE_FIT
            CycleType.Companion.S_VISIBILITY -> return CycleType.Companion.TYPE_VISIBILITY
            CycleType.Companion.S_ALPHA -> return CycleType.Companion.TYPE_ALPHA
            CycleType.Companion.S_TRANSLATION_X -> return CycleType.Companion.TYPE_TRANSLATION_X
            CycleType.Companion.S_TRANSLATION_Y -> return CycleType.Companion.TYPE_TRANSLATION_Y
            CycleType.Companion.S_TRANSLATION_Z -> return CycleType.Companion.TYPE_TRANSLATION_Z
            CycleType.Companion.S_ROTATION_X -> return CycleType.Companion.TYPE_ROTATION_X
            CycleType.Companion.S_ROTATION_Y -> return CycleType.Companion.TYPE_ROTATION_Y
            CycleType.Companion.S_ROTATION_Z -> return CycleType.Companion.TYPE_ROTATION_Z
            CycleType.Companion.S_SCALE_X -> return CycleType.Companion.TYPE_SCALE_X
            CycleType.Companion.S_SCALE_Y -> return CycleType.Companion.TYPE_SCALE_Y
            CycleType.Companion.S_PIVOT_X -> return CycleType.Companion.TYPE_PIVOT_X
            CycleType.Companion.S_PIVOT_Y -> return CycleType.Companion.TYPE_PIVOT_Y
            CycleType.Companion.S_PROGRESS -> return CycleType.Companion.TYPE_PROGRESS
            CycleType.Companion.S_PATH_ROTATE -> return CycleType.Companion.TYPE_PATH_ROTATE
            CycleType.Companion.S_EASING -> return CycleType.Companion.TYPE_EASING
            CycleType.Companion.S_WAVE_PERIOD -> return CycleType.Companion.TYPE_WAVE_PERIOD
            CycleType.Companion.S_WAVE_SHAPE -> return CycleType.Companion.TYPE_WAVE_SHAPE
            CycleType.Companion.S_WAVE_PHASE -> return CycleType.Companion.TYPE_WAVE_PHASE
            CycleType.Companion.S_WAVE_OFFSET -> return CycleType.Companion.TYPE_WAVE_OFFSET
            CycleType.Companion.S_CUSTOM_WAVE_SHAPE -> return CycleType.Companion.TYPE_CUSTOM_WAVE_SHAPE
        }
        return -1
    }

    /**
     * @TODO: add description
     */
    fun addCycleValues(oscSet: HashMap<String, KeyCycleOscillator>) {
        for (key in oscSet.keys) {
            if (key.startsWith(TypedValues.Companion.S_CUSTOM)) {
                val customKey: String = key.substring(TypedValues.Companion.S_CUSTOM.length + 1)
                val cValue = mCustom!![customKey]
                if (cValue == null || cValue.type != TypedValues.Custom.Companion.TYPE_FLOAT) {
                    continue
                }
                val osc: KeyCycleOscillator = oscSet[key] ?: continue
                osc.setPoint(
                    framePosition, mWaveShape, mCustomWaveShape, -1, mWavePeriod,
                    mWaveOffset, mWavePhase, cValue.valueToInterpolate, cValue
                )
                continue
            }
            val value = getValue(key)
            if (value.isNaN()) {
                continue
            }
            val osc: KeyCycleOscillator = oscSet.get(key) ?: continue
            osc.setPoint(
                framePosition, mWaveShape, mCustomWaveShape,
                -1, mWavePeriod, mWaveOffset, mWavePhase, value
            )
        }
    }

    /**
     * @TODO: add description
     */
    fun dump() {
        println(
            "MotionKeyCycle{"
                    + "mWaveShape=" + mWaveShape
                    + ", mWavePeriod=" + mWavePeriod
                    + ", mWaveOffset=" + mWaveOffset
                    + ", mWavePhase=" + mWavePhase
                    + ", mRotation=" + mRotation
                    + '}'
        )
    }

    /**
     * @TODO: add description
     */
    fun printAttributes() {
        val nameSet: HashSet<String> = HashSet()
        getAttributeNames(nameSet)
        Utils.Companion.log(" ------------- $framePosition -------------")
        Utils.Companion.log(
            "MotionKeyCycle{"
                    + "Shape=" + mWaveShape
                    + ", Period=" + mWavePeriod
                    + ", Offset=" + mWaveOffset
                    + ", Phase=" + mWavePhase
                    + '}'
        )
        val names: Array<String?> = nameSet.toTypedArray()
        for (i in names.indices) {
            val id: Int = AttributesType.Companion.getId(names[i])
            Utils.Companion.log(names[i] + ":" + getValue(names[i]))
        }
    }

    companion object {
        private const val TAG = "KeyCycle"
        const val NAME = "KeyCycle"
        const val WAVE_PERIOD = "wavePeriod"
        const val WAVE_OFFSET = "waveOffset"
        const val WAVE_PHASE = "wavePhase"
        const val WAVE_SHAPE = "waveShape"
        val SHAPE_SIN_WAVE: Int = Oscillator.Companion.SIN_WAVE
        val SHAPE_SQUARE_WAVE: Int = Oscillator.Companion.SQUARE_WAVE
        val SHAPE_TRIANGLE_WAVE: Int = Oscillator.Companion.TRIANGLE_WAVE
        val SHAPE_SAW_WAVE: Int = Oscillator.Companion.SAW_WAVE
        val SHAPE_REVERSE_SAW_WAVE: Int = Oscillator.Companion.REVERSE_SAW_WAVE
        val SHAPE_COS_WAVE: Int = Oscillator.Companion.COS_WAVE
        val SHAPE_BOUNCE: Int = Oscillator.Companion.BOUNCE
        const val KEY_TYPE = 4
    }
}
