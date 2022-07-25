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

import kotlin.math.sqrt

/**
 * Provides spline interpolation code.
 * Currently not used but it is anticipated that we will be using it in the
 * KeyMotion
 *
 * @DoNotShow
 */
class HyperSpline {
    var mPoints = 0
    lateinit var mCurve: Array<Array<Cubic?>?>
    var mDimensionality = 0
    lateinit var mCurveLength: DoubleArray
    var mTotalLength = 0.0
    lateinit var mCtl: Array<DoubleArray>

    /**
     * Spline in N dimensions
     *
     * @param points [mPoints][dimensionality]
     */
    constructor(points: Array<DoubleArray>) {
        setup(points)
    }

    constructor() {}

    /**
     * @TODO: add description
     */
    fun setup(points: Array<DoubleArray>) {
        mDimensionality = points[0].size
        mPoints = points.size
        mCtl = Array(mDimensionality) { DoubleArray(mPoints) }
        mCurve = arrayOfNulls(mDimensionality)
        for (d in 0 until mDimensionality) {
            for (p in 0 until mPoints) {
                mCtl[d][p] = points[p][d]
            }
        }
        for (d in 0 until mDimensionality) {
            mCurve[d] = calcNaturalCubic(mCtl[d].size, mCtl[d])
        }
        mCurveLength = DoubleArray(mPoints - 1)
        mTotalLength = 0.0
        val temp = arrayOfNulls<Cubic>(mDimensionality)
        for (p in mCurveLength.indices) {
            for (d in 0 until mDimensionality) {
                temp[d] = mCurve[d]!![p]
            }
            mCurveLength[p] = approxLength(temp)
            mTotalLength += mCurveLength[p]
        }
    }

    /**
     * @TODO: add description
     */
    fun getVelocity(p: Double, v: DoubleArray) {
        var pos = p * mTotalLength
        val sum = 0.0
        var k = 0
        while (k < mCurveLength.size - 1 && mCurveLength[k] < pos) {
            pos -= mCurveLength[k]
            k++
        }
        for (i in v.indices) {
            v[i] = mCurve[i]!![k]!!.vel(pos / mCurveLength[k])
        }
    }

    /**
     * @TODO: add description
     */
    fun getPos(p: Double, x: DoubleArray) {
        var pos = p * mTotalLength
        val sum = 0.0
        var k = 0
        while (k < mCurveLength.size - 1 && mCurveLength[k] < pos) {
            pos -= mCurveLength[k]
            k++
        }
        for (i in x.indices) {
            x[i] = mCurve[i]!![k]!!.eval(pos / mCurveLength[k])
        }
    }

    /**
     * @TODO: add description
     */
    fun getPos(p: Double, x: FloatArray) {
        var pos = p * mTotalLength
        val sum = 0.0
        var k = 0
        while (k < mCurveLength.size - 1 && mCurveLength[k] < pos) {
            pos -= mCurveLength[k]
            k++
        }
        for (i in x.indices) {
            x[i] = mCurve[i]!![k]!!.eval(pos / mCurveLength[k]).toFloat()
        }
    }

    /**
     * @TODO: add description
     */
    fun getPos(p: Double, splineNumber: Int): Double {
        var pos = p * mTotalLength
        val sum = 0.0
        var k = 0
        while (k < mCurveLength.size - 1 && mCurveLength[k] < pos) {
            pos -= mCurveLength[k]
            k++
        }
        return mCurve[splineNumber]!![k]!!.eval(pos / mCurveLength[k])
    }

    /**
     * @TODO: add description
     */
    fun approxLength(curve: Array<Cubic?>): Double {
        var sum = 0.0
        val n = curve.size
        val old = DoubleArray(curve.size)
        var i = 0.0
        while (i < 1) {
            var s = 0.0
            for (j in curve.indices) {
                var tmp = old[j]
                old[j] = curve[j]!!.eval(i)
                tmp -= old[j]
                s += tmp * tmp
            }
            if (i > 0) {
                sum += sqrt(s)
            }
            i += .1
        }
        var s = 0.0
        for (j in curve.indices) {
            var tmp = old[j]
            old[j] = curve[j]!!.eval(1.0)
            tmp -= old[j]
            s += tmp * tmp
        }
        sum += sqrt(s)
        return sum
    }

    class Cubic(var mA: Double, var mB: Double, var mC: Double, var mD: Double) {
        /**
         * @TODO: add description
         */
        fun eval(u: Double): Double {
            return ((mD * u + mC) * u + mB) * u + mA
        }

        /**
         * @TODO: add description
         */
        fun vel(v: Double): Double {
            //  (((mD * u) + mC) * u + mB) * u + mA
            //  =  "mA + u*mB + u*u*mC+u*u*u*mD" a cubic expression
            // diff with respect to u = mB + u*mC/2+ u*u*mD/3
            // made efficient (mD*u/3+mC/2)*u+mB;
            return (mD * 3 * v + mC * 2) * v + mB
        }
    }

    companion object {
        fun calcNaturalCubic(n: Int, x: DoubleArray): Array<Cubic?> {
            var n = n
            val gamma = DoubleArray(n)
            val delta = DoubleArray(n)
            val d = DoubleArray(n)
            n -= 1
            gamma[0] = (1.0f / 2.0f).toDouble()
            for (i in 1 until n) {
                gamma[i] = 1 / (4 - gamma[i - 1])
            }
            gamma[n] = 1 / (2 - gamma[n - 1])
            delta[0] = 3 * (x[1] - x[0]) * gamma[0]
            for (i in 1 until n) {
                delta[i] = (3 * (x[i + 1] - x[i - 1]) - delta[i - 1]) * gamma[i]
            }
            delta[n] = (3 * (x[n] - x[n - 1]) - delta[n - 1]) * gamma[n]
            d[n] = delta[n]
            for (i in n - 1 downTo 0) {
                d[i] = delta[i] - gamma[i] * d[i + 1]
            }
            val c = arrayOfNulls<Cubic>(n)
            for (i in 0 until n) {
                c[i] = Cubic(
                    x[i].toFloat().toDouble(), d[i], 3 * (x[i + 1] - x[i]) - (2
                            * d[i]) - d[i + 1], 2 * (x[i] - x[i + 1]) + d[i] + d[i + 1]
                )
            }
            return c
        }
    }
}
