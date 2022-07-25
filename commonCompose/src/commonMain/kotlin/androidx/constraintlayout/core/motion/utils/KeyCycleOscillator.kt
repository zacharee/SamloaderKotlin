/*
 * Copyright (C) 2020 The Android Open Source Project
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
package androidx.constraintlayout.core.motion.utils

import androidx.constraintlayout.core.motion.MotionWidget
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.core.motion.utils.TypedValues.CycleType
import com.soywiz.korio.util.toStringDecimal
import kotlin.math.PI
import kotlin.math.atan2

/**
 * Provide the engine for executing cycles.
 * KeyCycleOscillator
 *
 * @DoNotShow
 */
abstract class KeyCycleOscillator {
    var curveFit: CurveFit? = null
        private set
    private var mCycleOscillator: CycleOscillator? = null
    private var mType: String? = null
    private var mWaveShape = 0
    private var mWaveString: String? = null
    var mVariesBy = 0 // 0 = position, 2=path
    var mWavePoints: ArrayList<WavePoint> = ArrayList()

    private class CoreSpline(var mType: String) : KeyCycleOscillator() {
        var mTypeId: Int

        init {
            mTypeId = CycleType.Companion.getId(mType)
        }

        override fun setProperty(widget: MotionWidget, t: Float) {
            widget.setValue(mTypeId, get(t))
        }
    }

    class PathRotateSet(var mType: String) : KeyCycleOscillator() {
        var mTypeId: Int

        init {
            mTypeId = CycleType.Companion.getId(mType)
        }

        override fun setProperty(widget: MotionWidget, t: Float) {
            widget.setValue(mTypeId, get(t))
        }

        /**
         * @TODO: add description
         */
        fun setPathRotate(view: MotionWidget, t: Float, dx: Double, dy: Double) {
            view.rotationZ = get(t) + (atan2(dy, dx) * 180.0 / PI).toFloat()
        }
    }

    /**
     * @TODO: add description
     */
    fun variesByPath(): Boolean {
        return mVariesBy == 1
    }

    class WavePoint(var mPosition: Int, var mPeriod: Float, var mOffset: Float, var mPhase: Float, var mValue: Float)

    override fun toString(): String {
        var str = mType
        for (wp in mWavePoints) {
            str += "[" + wp.mPosition + " , " + wp.mValue.toDouble().toStringDecimal(2) + "] "
        }
        return str!!
    }

    fun setType(type: String?) {
        mType = type
    }

    /**
     * @TODO: add description
     */
    operator fun get(t: Float): Float {
        return mCycleOscillator!!.getValues(t).toFloat()
    }

    /**
     * @TODO: add description
     */
    fun getSlope(position: Float): Float {
        return mCycleOscillator!!.getSlope(position).toFloat()
    }

    protected fun setCustom(custom: Any?) {}

    /**
     * sets a oscillator wave point
     *
     * @param framePosition the position
     * @param variesBy      only varies by path supported for now
     * @param period        the period of the wave
     * @param offset        the offset value
     * @param value         the adder
     * @param custom        The ConstraintAttribute used to set the value
     */
    fun setPoint(
        framePosition: Int,
        shape: Int,
        waveString: String?,
        variesBy: Int,
        period: Float,
        offset: Float,
        phase: Float,
        value: Float,
        custom: Any?
    ) {
        mWavePoints.add(WavePoint(framePosition, period, offset, phase, value))
        if (variesBy != -1) {
            mVariesBy = variesBy
        }
        mWaveShape = shape
        setCustom(custom)
        mWaveString = waveString
    }

    /**
     * sets a oscillator wave point
     *
     * @param framePosition the position
     * @param variesBy      only varies by path supported for now
     * @param period        the period of the wave
     * @param offset        the offset value
     * @param value         the adder
     */
    fun setPoint(
        framePosition: Int,
        shape: Int,
        waveString: String?,
        variesBy: Int,
        period: Float,
        offset: Float,
        phase: Float,
        value: Float
    ) {
        mWavePoints.add(WavePoint(framePosition, period, offset, phase, value))
        if (variesBy != -1) {
            mVariesBy = variesBy
        }
        mWaveShape = shape
        mWaveString = waveString
    }

    /**
     * @TODO: add description
     */
    fun setup(pathLength: Float) {
        val count: Int = mWavePoints.size
        if (count == 0) {
            return
        }
        mWavePoints.sortedWith { a, b ->
            a.mPosition.compareTo(b.mPosition)
        }
        val time = DoubleArray(count)
        val values = Array(count) { DoubleArray(3) }
        mCycleOscillator = CycleOscillator(mWaveShape, mWaveString, mVariesBy, count)
        var i = 0
        for (wp in mWavePoints) {
            time[i] = wp.mPeriod * 1E-2
            values[i][0] = wp.mValue.toDouble()
            values[i][1] = wp.mOffset.toDouble()
            values[i][2] = wp.mPhase.toDouble()
            mCycleOscillator!!.setPoint(
                i, wp.mPosition, wp.mPeriod,
                wp.mOffset, wp.mPhase, wp.mValue
            )
            i++
        }
        mCycleOscillator!!.setup(pathLength)
        curveFit = CurveFit.Companion.get(CurveFit.Companion.SPLINE, time, values)
    }

    internal class CycleOscillator(var mWaveShape: Int, customShape: String?, private val mVariesBy: Int, steps: Int) {
        var mOscillator = Oscillator()
        private val mOffst = 0
        private val mPhase = 1
        private val mValue = 2
        var mValues: FloatArray
        var mPosition: DoubleArray
        var mPeriod: FloatArray
        var mOffsetArr // offsets will be spline interpolated
                : FloatArray
        var mPhaseArr // phase will be spline interpolated
                : FloatArray
        var mScale // scales will be spline interpolated
                : FloatArray
        var mCurveFit: CurveFit? = null
        lateinit var mSplineValueCache // for the return value of the curve fit
                : DoubleArray
        lateinit var mSplineSlopeCache // for the return value of the curve fit
                : DoubleArray
        var mPathLength = 0f

        init {
            mOscillator.setType(mWaveShape, customShape)
            mValues = FloatArray(steps)
            mPosition = DoubleArray(steps)
            mPeriod = FloatArray(steps)
            mOffsetArr = FloatArray(steps)
            mPhaseArr = FloatArray(steps)
            mScale = FloatArray(steps)
        }

        fun getValues(time: Float): Double {
            if (mCurveFit != null) {
                mCurveFit!!.getPos(time.toDouble(), mSplineValueCache)
            } else { // only one value no need to interpolate
                mSplineValueCache[mOffst] = mOffsetArr[0].toDouble()
                mSplineValueCache[mPhase] = mPhaseArr[0].toDouble()
                mSplineValueCache[mValue] = mValues[0].toDouble()
            }
            val offset = mSplineValueCache[mOffst]
            val phase = mSplineValueCache[mPhase]
            val waveValue = mOscillator.getValue(time.toDouble(), phase)
            return offset + waveValue * mSplineValueCache[mValue]
        }

        val lastPhase: Double
            get() = mSplineValueCache[1]

        fun getSlope(time: Float): Double {
            if (mCurveFit != null) {
                mCurveFit!!.getSlope(time.toDouble(), mSplineSlopeCache)
                mCurveFit!!.getPos(time.toDouble(), mSplineValueCache)
            } else { // only one value no need to interpolate
                mSplineSlopeCache[mOffst] = 0.0
                mSplineSlopeCache[mPhase] = 0.0
                mSplineSlopeCache[mValue] = 0.0
            }
            val waveValue = mOscillator.getValue(time.toDouble(), mSplineValueCache[mPhase])
            val waveSlope = mOscillator.getSlope(
                time.toDouble(),
                mSplineValueCache[mPhase], mSplineSlopeCache[mPhase]
            )
            return mSplineSlopeCache[mOffst] + waveValue * mSplineSlopeCache[mValue] + waveSlope * mSplineValueCache[mValue]
        }

        /**
         *
         */
        fun setPoint(
            index: Int,
            framePosition: Int,
            wavePeriod: Float,
            offset: Float,
            phase: Float,
            values: Float
        ) {
            mPosition[index] = framePosition / 100.0
            mPeriod[index] = wavePeriod
            mOffsetArr[index] = offset
            mPhaseArr[index] = phase
            mValues[index] = values
        }

        fun setup(pathLength: Float) {
            mPathLength = pathLength
            val splineValues = Array(mPosition.size) { DoubleArray(3) }
            mSplineValueCache = DoubleArray(2 + mValues.size)
            mSplineSlopeCache = DoubleArray(2 + mValues.size)
            if (mPosition[0] > 0) {
                mOscillator.addPoint(0.0, mPeriod[0])
            }
            val last = mPosition.size - 1
            if (mPosition[last] < 1.0f) {
                mOscillator.addPoint(1.0, mPeriod[last])
            }
            for (i in splineValues.indices) {
                splineValues[i][mOffst] = mOffsetArr[i].toDouble()
                splineValues[i][mPhase] = mPhaseArr[i].toDouble()
                splineValues[i][mValue] = mValues[i].toDouble()
                mOscillator.addPoint(mPosition[i], mPeriod[i])
            }

            // TODO: add mVariesBy and get total time and path length
            mOscillator.normalize()
            mCurveFit = if (mPosition.size > 1) {
                CurveFit.Companion.get(CurveFit.Companion.SPLINE, mPosition, splineValues)
            } else {
                null
            }
        }

        companion object {
            const val UNSET = -1 // -1 is typically used through out android to the UNSET value
            private const val TAG = "CycleOscillator"
        }
    }

    /**
     * @TODO: add description
     */
    open fun setProperty(widget: MotionWidget, t: Float) {}

    companion object {
        private const val TAG = "KeyCycleOscillator"

        /**
         * @TODO: add description
         */
        fun makeWidgetCycle(attribute: String): KeyCycleOscillator {
            return if (attribute == AttributesType.Companion.S_PATH_ROTATE) {
                PathRotateSet(attribute)
            } else CoreSpline(attribute)
        }
    }
}
