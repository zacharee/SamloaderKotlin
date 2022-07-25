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

import kotlin.math.hypot

/**
 * This performs a spline interpolation in multiple dimensions
 *
 * @DoNotShow
 */
class MonotonicCurveFit(time: DoubleArray?, y: Array<DoubleArray>?) : CurveFit() {
    override val timePoints: DoubleArray?
    private val mY: Array<DoubleArray>?
    private val mTangent: Array<DoubleArray>
    private val mExtrapolate = true
    var mSlopeTemp: DoubleArray

    init {
        val n = time!!.size
        val dim = y!![0].size
        mSlopeTemp = DoubleArray(dim)
        val slope = Array(n - 1) { DoubleArray(dim) } // could optimize this out
        val tangent = Array(n) { DoubleArray(dim) }
        for (j in 0 until dim) {
            for (i in 0 until n - 1) {
                val dt = time[i + 1] - time[i]
                slope[i][j] = (y[i + 1][j] - y[i][j]) / dt
                if (i == 0) {
                    tangent[i][j] = slope[i][j]
                } else {
                    tangent[i][j] = (slope[i - 1][j] + slope[i][j]) * 0.5f
                }
            }
            tangent[n - 1][j] = slope[n - 2][j]
        }
        for (i in 0 until n - 1) {
            for (j in 0 until dim) {
                if (slope[i][j] == 0.0) {
                    tangent[i][j] = 0.0
                    tangent[i + 1][j] = 0.0
                } else {
                    val a = tangent[i][j] / slope[i][j]
                    val b = tangent[i + 1][j] / slope[i][j]
                    val h: Double = hypot(a, b)
                    if (h > 9.0) {
                        val t = 3.0 / h
                        tangent[i][j] = t * a * slope[i][j]
                        tangent[i + 1][j] = t * b * slope[i][j]
                    }
                }
            }
        }
        timePoints = time
        mY = y
        mTangent = tangent
    }

    override fun getPos(t: Double, v: DoubleArray) {
        val n = timePoints!!.size
        val dim = mY!![0].size
        if (mExtrapolate) {
            if (t <= timePoints[0]) {
                getSlope(timePoints[0], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = mY[0][j] + (t - timePoints[0]) * mSlopeTemp[j]
                }
                return
            }
            if (t >= timePoints[n - 1]) {
                getSlope(timePoints[n - 1], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j] + (t - timePoints[n - 1]) * mSlopeTemp[j]
                }
                return
            }
        } else {
            if (t <= timePoints[0]) {
                for (j in 0 until dim) {
                    v[j] = mY[0][j]
                }
                return
            }
            if (t >= timePoints[n - 1]) {
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j]
                }
                return
            }
        }
        for (i in 0 until n - 1) {
            if (t == timePoints[i]) {
                for (j in 0 until dim) {
                    v[j] = mY[i][j]
                }
            }
            if (t < timePoints[i + 1]) {
                val h = timePoints[i + 1] - timePoints[i]
                val x = (t - timePoints[i]) / h
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    val t1 = mTangent[i][j]
                    val t2 = mTangent[i + 1][j]
                    v[j] = interpolate(h, x, y1, y2, t1, t2)
                }
                return
            }
        }
    }

    override fun getPos(t: Double, v: FloatArray) {
        val n = timePoints!!.size
        val dim = mY!![0].size
        if (mExtrapolate) {
            if (t <= timePoints[0]) {
                getSlope(timePoints[0], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = (mY[0][j] + (t - timePoints[0]) * mSlopeTemp[j]).toFloat()
                }
                return
            }
            if (t >= timePoints[n - 1]) {
                getSlope(timePoints[n - 1], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = (mY[n - 1][j] + (t - timePoints[n - 1]) * mSlopeTemp[j]).toFloat()
                }
                return
            }
        } else {
            if (t <= timePoints[0]) {
                for (j in 0 until dim) {
                    v[j] = mY[0][j].toFloat()
                }
                return
            }
            if (t >= timePoints[n - 1]) {
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j].toFloat()
                }
                return
            }
        }
        for (i in 0 until n - 1) {
            if (t == timePoints[i]) {
                for (j in 0 until dim) {
                    v[j] = mY[i][j].toFloat()
                }
            }
            if (t < timePoints[i + 1]) {
                val h = timePoints[i + 1] - timePoints[i]
                val x = (t - timePoints[i]) / h
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    val t1 = mTangent[i][j]
                    val t2 = mTangent[i + 1][j]
                    v[j] = interpolate(h, x, y1, y2, t1, t2).toFloat()
                }
                return
            }
        }
    }

    override fun getPos(t: Double, j: Int): Double {
        val n = timePoints!!.size
        if (mExtrapolate) {
            if (t <= timePoints[0]) {
                return mY!![0][j] + (t - timePoints[0]) * getSlope(timePoints[0], j)
            }
            if (t >= timePoints[n - 1]) {
                return mY!![n - 1][j] + (t - timePoints[n - 1]) * getSlope(timePoints[n - 1], j)
            }
        } else {
            if (t <= timePoints[0]) {
                return mY!![0][j]
            }
            if (t >= timePoints[n - 1]) {
                return mY!![n - 1][j]
            }
        }
        for (i in 0 until n - 1) {
            if (t == timePoints[i]) {
                return mY!![i][j]
            }
            if (t < timePoints[i + 1]) {
                val h = timePoints[i + 1] - timePoints[i]
                val x = (t - timePoints[i]) / h
                val y1 = mY!![i][j]
                val y2 = mY[i + 1][j]
                val t1 = mTangent[i][j]
                val t2 = mTangent[i + 1][j]
                return interpolate(h, x, y1, y2, t1, t2)
            }
        }
        return 0.0 // should never reach here
    }

    override fun getSlope(t: Double, v: DoubleArray) {
        var t = t
        val n = timePoints!!.size
        val dim = mY!![0].size
        if (t <= timePoints[0]) {
            t = timePoints[0]
        } else if (t >= timePoints[n - 1]) {
            t = timePoints[n - 1]
        }
        for (i in 0 until n - 1) {
            if (t <= timePoints[i + 1]) {
                val h = timePoints[i + 1] - timePoints[i]
                val x = (t - timePoints[i]) / h
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    val t1 = mTangent[i][j]
                    val t2 = mTangent[i + 1][j]
                    v[j] = diff(h, x, y1, y2, t1, t2) / h
                }
                break
            }
        }
        return
    }

    override fun getSlope(t: Double, j: Int): Double {
        var t = t
        val n = timePoints!!.size
        if (t < timePoints[0]) {
            t = timePoints[0]
        } else if (t >= timePoints[n - 1]) {
            t = timePoints[n - 1]
        }
        for (i in 0 until n - 1) {
            if (t <= timePoints[i + 1]) {
                val h = timePoints[i + 1] - timePoints[i]
                val x = (t - timePoints[i]) / h
                val y1 = mY!![i][j]
                val y2 = mY[i + 1][j]
                val t1 = mTangent[i][j]
                val t2 = mTangent[i + 1][j]
                return diff(h, x, y1, y2, t1, t2) / h
            }
        }
        return 0.0 // should never reach here
    }

    companion object {
        private const val TAG = "MonotonicCurveFit"

        /**
         * Cubic Hermite spline
         */
        private fun interpolate(
            h: Double,
            x: Double,
            y1: Double,
            y2: Double,
            t1: Double,
            t2: Double
        ): Double {
            val x2 = x * x
            val x3 = x2 * x
            return (-2 * x3 * y2 + 3 * x2 * y2 + 2 * x3 * y1 - 3 * x2 * y1 + y1 + h * t2 * x3 + h * t1 * x3 - h * t2 * x2 - 2 * h * t1 * x2
                    + h * t1 * x)
        }

        /**
         * Cubic Hermite spline slope differentiated
         */
        private fun diff(h: Double, x: Double, y1: Double, y2: Double, t1: Double, t2: Double): Double {
            val x2 = x * x
            return -6 * x2 * y2 + 6 * x * y2 + 6 * x2 * y1 - 6 * x * y1 + 3 * h * t2 * x2 + 3 * h * t1 * x2 - 2 * h * t2 * x - 4 * h * t1 * x + h * t1
        }

        /**
         * This builds a monotonic spline to be used as a wave function
         */
        fun buildWave(configString: String?): MonotonicCurveFit {
            // done this way for efficiency
            val values = DoubleArray(configString!!.length / 2)
            var start = configString.indexOf('(') + 1
            var off1 = configString.indexOf(',', start)
            var count = 0
            while (off1 != -1) {
                val tmp = configString.substring(start, off1).trim { it <= ' ' }
                values[count++] = tmp.toDouble()
                off1 = configString.indexOf(',', off1 + 1.also { start = it })
            }
            off1 = configString.indexOf(')', start)
            val tmp = configString.substring(start, off1).trim { it <= ' ' }
            values[count++] = tmp.toDouble()
            return buildWave(values.copyOf(count))
        }

        private fun buildWave(values: DoubleArray): MonotonicCurveFit {
            val length = values.size * 3 - 2
            val len = values.size - 1
            val gap = 1.0 / len
            val points = Array(length) { DoubleArray(1) }
            val time = DoubleArray(length)
            for (i in values.indices) {
                val v = values[i]
                points[i + len][0] = v
                time[i + len] = i * gap
                if (i > 0) {
                    points[i + len * 2][0] = v + 1
                    time[i + len * 2] = i * gap + 1
                    points[i - 1][0] = v - 1 - gap
                    time[i - 1] = i * gap + -1 - gap
                }
            }
            return MonotonicCurveFit(time, points)
        }
    }
}
