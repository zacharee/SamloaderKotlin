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

import com.soywiz.kds.binarySearch
import kotlin.math.*

/**
 * This generates variable frequency oscillation curves
 *
 * @DoNotShow
 */
class Oscillator {
    var mPeriod = floatArrayOf()
    var mPosition = doubleArrayOf()
    lateinit var mArea: DoubleArray
    var mCustomType: String? = null
    var mCustomCurve: MonotonicCurveFit? = null
    var mType = 0
    var mPI2: Double = PI * 2
    private var mNormalized = false
    override fun toString(): String {
        return "pos =" + mPosition.contentToString() + " period=" + mPeriod.contentToString()
    }

    /**
     * @TODO: add description
     */
    fun setType(type: Int, customType: String?) {
        mType = type
        mCustomType = customType
        if (mCustomType != null) {
            mCustomCurve = MonotonicCurveFit.buildWave(customType)
        }
    }

    /**
     * @TODO: add description
     */
    fun addPoint(position: Double, period: Float) {
        val len = mPeriod.size + 1
        var j: Int = mPosition.binarySearch(position).index
        if (j < 0) {
            j = -j - 1
        }
        mPosition = mPosition.copyOf(len)
        mPeriod = mPeriod.copyOf(len)
        mArea = DoubleArray(len)
        mPosition.copyInto(mPosition, j, j + 1, len - j - 1)
        mPosition[j] = position
        mPeriod[j] = period
        mNormalized = false
    }

    /**
     * After adding point every thing must be normalized
     */
    fun normalize() {
        var totalArea = 0.0
        var totalCount = 0.0
        for (i in mPeriod.indices) {
            totalCount += mPeriod[i].toDouble()
        }
        for (i in 1 until mPeriod.size) {
            val h = (mPeriod[i - 1] + mPeriod[i]) / 2
            val w = mPosition[i] - mPosition[i - 1]
            totalArea = totalArea + w * h
        }
        // scale periods to normalize it
        for (i in mPeriod.indices) {
            mPeriod[i] *= (totalCount / totalArea).toFloat()
        }
        mArea[0] = 0.0
        for (i in 1 until mPeriod.size) {
            val h = (mPeriod[i - 1] + mPeriod[i]) / 2
            val w = mPosition[i] - mPosition[i - 1]
            mArea[i] = mArea[i - 1] + w * h
        }
        mNormalized = true
    }

    fun getP(time: Double): Double {
        var time = time
        if (time < 0) {
            time = 0.0
        } else if (time > 1) {
            time = 1.0
        }
        var index: Int = mPosition.binarySearch(time).index
        var p = 0.0
        if (index > 0) {
            p = 1.0
        } else if (index != 0) {
            index = -index - 1
            val t = time
            val m = ((mPeriod[index] - mPeriod[index - 1])
                    / (mPosition[index] - mPosition[index - 1]))
            p = mArea[index - 1] + (mPeriod[index - 1] - m * mPosition[index - 1]) * (t - mPosition[index - 1]) + m * (t * t - mPosition[index - 1] * mPosition[index - 1]) / 2
        }
        return p
    }

    /**
     * @TODO: add description
     */
    fun getValue(time: Double, phase: Double): Double {
        val angle = phase + getP(time) // angle is / by 360
        return when (mType) {
            SIN_WAVE -> sin(mPI2 * angle)
            SQUARE_WAVE -> sign(0.5 - angle % 1)
            TRIANGLE_WAVE -> 1 - abs((angle * 4 + 1) % 4 - 2)
            SAW_WAVE -> (angle * 2 + 1) % 2 - 1
            REVERSE_SAW_WAVE -> 1 - (angle * 2 + 1) % 2
            COS_WAVE -> cos(mPI2 * (phase + angle))
            BOUNCE -> {
                val x: Double = 1 - abs(angle * 4 % 4 - 2)
                1 - x * x
            }

            CUSTOM -> mCustomCurve!!.getPos(angle % 1, 0)
            else -> sin(mPI2 * angle)
        }
    }

    fun getDP(time: Double): Double {
        var time = time
        if (time <= 0) {
            time = 0.00001
        } else if (time >= 1) {
            time = .999999
        }
        var index: Int = mPosition.binarySearch(time).index
        var p = 0.0
        if (index > 0) {
            return 0.0
        }
        if (index != 0) {
            index = -index - 1
            val t = time
            val m = ((mPeriod[index] - mPeriod[index - 1])
                    / (mPosition[index] - mPosition[index - 1]))
            p = m * t + (mPeriod[index - 1] - m * mPosition[index - 1])
        }
        return p
    }

    /**
     * @TODO: add description
     */
    fun getSlope(time: Double, phase: Double, dphase: Double): Double {
        val angle = phase + getP(time)
        val dangle_dtime = getDP(time) + dphase
        return when (mType) {
            SIN_WAVE -> mPI2 * dangle_dtime * cos(mPI2 * angle)
            SQUARE_WAVE -> 0.0
            TRIANGLE_WAVE -> 4 * dangle_dtime * sign((angle * 4 + 3) % 4 - 2)
            SAW_WAVE -> dangle_dtime * 2
            REVERSE_SAW_WAVE -> -dangle_dtime * 2
            COS_WAVE -> -mPI2 * dangle_dtime * sin(mPI2 * angle)
            BOUNCE -> 4 * dangle_dtime * ((angle * 4 + 2) % 4 - 2)
            CUSTOM -> mCustomCurve!!.getSlope(angle % 1, 0)
            else -> mPI2 * dangle_dtime * cos(mPI2 * angle)
        }
    }

    companion object {
        const val TAG = "Oscillator"
        const val SIN_WAVE = 0 // theses must line up with attributes
        const val SQUARE_WAVE = 1
        const val TRIANGLE_WAVE = 2
        const val SAW_WAVE = 3
        const val REVERSE_SAW_WAVE = 4
        const val COS_WAVE = 5
        const val BOUNCE = 6
        const val CUSTOM = 7
    }
}
