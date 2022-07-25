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
 * This provides provides a curve fit system that stitches the x,y path together with
 * quarter ellipses
 */
class ArcCurveFit(arcModes: IntArray, override val timePoints: DoubleArray, y: Array<DoubleArray>) : CurveFit() {
    var mArcs: Array<Arc?> = arrayOfNulls(timePoints.size - 1)
    private val mExtrapolate = true
    override fun getPos(t: Double, v: DoubleArray) {
        var t = t
        if (mExtrapolate) {
            if (t < mArcs[0]!!.mTime1) {
                val t0 = mArcs[0]!!.mTime1
                val dt = t - mArcs[0]!!.mTime1
                val p = 0
                if (mArcs[p]!!.mLinear) {
                    v[0] = mArcs[p]!!.getLinearX(t0) + dt * mArcs[p]!!.getLinearDX(t0)
                    v[1] = mArcs[p]!!.getLinearY(t0) + dt * mArcs[p]!!.getLinearDY(t0)
                } else {
                    mArcs[p]!!.setPoint(t0)
                    v[0] = mArcs[p]!!.x + dt * mArcs[p]!!.dX
                    v[1] = mArcs[p]!!.y + dt * mArcs[p]!!.dY
                }
                return
            }
            if (t > mArcs[mArcs.size - 1]!!.mTime2) {
                val t0 = mArcs[mArcs.size - 1]!!.mTime2
                val dt = t - t0
                val p = mArcs.size - 1
                if (mArcs[p]!!.mLinear) {
                    v[0] = mArcs[p]!!.getLinearX(t0) + dt * mArcs[p]!!.getLinearDX(t0)
                    v[1] = mArcs[p]!!.getLinearY(t0) + dt * mArcs[p]!!.getLinearDY(t0)
                } else {
                    mArcs[p]!!.setPoint(t)
                    v[0] = mArcs[p]!!.x + dt * mArcs[p]!!.dX
                    v[1] = mArcs[p]!!.y + dt * mArcs[p]!!.dY
                }
                return
            }
        } else {
            if (t < mArcs[0]!!.mTime1) {
                t = mArcs[0]!!.mTime1
            }
            if (t > mArcs[mArcs.size - 1]!!.mTime2) {
                t = mArcs[mArcs.size - 1]!!.mTime2
            }
        }
        for (i in mArcs.indices) {
            if (t <= mArcs[i]!!.mTime2) {
                if (mArcs[i]!!.mLinear) {
                    v[0] = mArcs[i]!!.getLinearX(t)
                    v[1] = mArcs[i]!!.getLinearY(t)
                    return
                }
                mArcs[i]!!.setPoint(t)
                v[0] = mArcs[i]!!.x
                v[1] = mArcs[i]!!.y
                return
            }
        }
    }

    override fun getPos(t: Double, v: FloatArray) {
        var t = t
        if (mExtrapolate) {
            if (t < mArcs[0]!!.mTime1) {
                val t0 = mArcs[0]!!.mTime1
                val dt = t - mArcs[0]!!.mTime1
                val p = 0
                if (mArcs[p]!!.mLinear) {
                    v[0] = (mArcs[p]!!.getLinearX(t0) + dt * mArcs[p]!!.getLinearDX(t0)).toFloat()
                    v[1] = (mArcs[p]!!.getLinearY(t0) + dt * mArcs[p]!!.getLinearDY(t0)).toFloat()
                } else {
                    mArcs[p]!!.setPoint(t0)
                    v[0] = (mArcs[p]!!.x + dt * mArcs[p]!!.dX).toFloat()
                    v[1] = (mArcs[p]!!.y + dt * mArcs[p]!!.dY).toFloat()
                }
                return
            }
            if (t > mArcs[mArcs.size - 1]!!.mTime2) {
                val t0 = mArcs[mArcs.size - 1]!!.mTime2
                val dt = t - t0
                val p = mArcs.size - 1
                if (mArcs[p]!!.mLinear) {
                    v[0] = (mArcs[p]!!.getLinearX(t0) + dt * mArcs[p]!!.getLinearDX(t0)).toFloat()
                    v[1] = (mArcs[p]!!.getLinearY(t0) + dt * mArcs[p]!!.getLinearDY(t0)).toFloat()
                } else {
                    mArcs[p]!!.setPoint(t)
                    v[0] = mArcs[p]!!.x.toFloat()
                    v[1] = mArcs[p]!!.y.toFloat()
                }
                return
            }
        } else {
            if (t < mArcs[0]!!.mTime1) {
                t = mArcs[0]!!.mTime1
            } else if (t > mArcs[mArcs.size - 1]!!.mTime2) {
                t = mArcs[mArcs.size - 1]!!.mTime2
            }
        }
        for (i in mArcs.indices) {
            if (t <= mArcs[i]!!.mTime2) {
                if (mArcs[i]!!.mLinear) {
                    v[0] = mArcs[i]!!.getLinearX(t).toFloat()
                    v[1] = mArcs[i]!!.getLinearY(t).toFloat()
                    return
                }
                mArcs[i]!!.setPoint(t)
                v[0] = mArcs[i]!!.x.toFloat()
                v[1] = mArcs[i]!!.y.toFloat()
                return
            }
        }
    }

    override fun getSlope(t: Double, v: DoubleArray) {
        var t = t
        if (t < mArcs[0]!!.mTime1) {
            t = mArcs[0]!!.mTime1
        } else if (t > mArcs[mArcs.size - 1]!!.mTime2) {
            t = mArcs[mArcs.size - 1]!!.mTime2
        }
        for (i in mArcs.indices) {
            if (t <= mArcs[i]!!.mTime2) {
                if (mArcs[i]!!.mLinear) {
                    v[0] = mArcs[i]!!.getLinearDX(t)
                    v[1] = mArcs[i]!!.getLinearDY(t)
                    return
                }
                mArcs[i]!!.setPoint(t)
                v[0] = mArcs[i]!!.dX
                v[1] = mArcs[i]!!.dY
                return
            }
        }
    }

    override fun getPos(t: Double, j: Int): Double {
        var t = t
        if (mExtrapolate) {
            if (t < mArcs[0]!!.mTime1) {
                val t0 = mArcs[0]!!.mTime1
                val dt = t - mArcs[0]!!.mTime1
                val p = 0
                return if (mArcs[p]!!.mLinear) {
                    if (j == 0) {
                        mArcs[p]!!.getLinearX(t0) + dt * mArcs[p]!!.getLinearDX(t0)
                    } else mArcs[p]!!.getLinearY(t0) + dt * mArcs[p]!!.getLinearDY(t0)
                } else {
                    mArcs[p]!!.setPoint(t0)
                    if (j == 0) {
                        mArcs[p]!!.x + dt * mArcs[p]!!.dX
                    } else mArcs[p]!!.y + dt * mArcs[p]!!.dY
                }
            }
            if (t > mArcs[mArcs.size - 1]!!.mTime2) {
                val t0 = mArcs[mArcs.size - 1]!!.mTime2
                val dt = t - t0
                val p = mArcs.size - 1
                return if (j == 0) {
                    mArcs[p]!!.getLinearX(t0) + dt * mArcs[p]!!.getLinearDX(t0)
                } else mArcs[p]!!.getLinearY(t0) + dt * mArcs[p]!!.getLinearDY(t0)
            }
        } else {
            if (t < mArcs[0]!!.mTime1) {
                t = mArcs[0]!!.mTime1
            } else if (t > mArcs[mArcs.size - 1]!!.mTime2) {
                t = mArcs[mArcs.size - 1]!!.mTime2
            }
        }
        for (i in mArcs.indices) {
            if (t <= mArcs[i]!!.mTime2) {
                if (mArcs[i]!!.mLinear) {
                    return if (j == 0) {
                        mArcs[i]!!.getLinearX(t)
                    } else mArcs[i]!!.getLinearY(t)
                }
                mArcs[i]!!.setPoint(t)
                return if (j == 0) {
                    mArcs[i]!!.x
                } else mArcs[i]!!.y
            }
        }
        return Double.NaN
    }

    override fun getSlope(t: Double, j: Int): Double {
        var t = t
        if (t < mArcs[0]!!.mTime1) {
            t = mArcs[0]!!.mTime1
        }
        if (t > mArcs[mArcs.size - 1]!!.mTime2) {
            t = mArcs[mArcs.size - 1]!!.mTime2
        }
        for (i in mArcs.indices) {
            if (t <= mArcs[i]!!.mTime2) {
                if (mArcs[i]!!.mLinear) {
                    return if (j == 0) {
                        mArcs[i]!!.getLinearDX(t)
                    } else mArcs[i]!!.getLinearDY(t)
                }
                mArcs[i]!!.setPoint(t)
                return if (j == 0) {
                    mArcs[i]!!.dX
                } else mArcs[i]!!.dY
            }
        }
        return Double.NaN
    }

    init {
        var mode = START_VERTICAL
        var last = START_VERTICAL
        for (i in mArcs.indices) {
            when (arcModes[i]) {
                ARC_START_VERTICAL -> {
                    mode = START_VERTICAL
                    last = mode
                }

                ARC_START_HORIZONTAL -> {
                    mode = START_HORIZONTAL
                    last = mode
                }

                ARC_START_FLIP -> {
                    mode = if (last == START_VERTICAL) START_HORIZONTAL else START_VERTICAL
                    last = mode
                }

                ARC_START_LINEAR -> mode = START_LINEAR
            }
            mArcs[i] = Arc(mode, timePoints[i], timePoints[i + 1], y[i][0], y[i][1], y[i + 1][0], y[i + 1][1])
        }
    }

    class Arc internal constructor(mode: Int, t1: Double, t2: Double, x1: Double, y1: Double, x2: Double, y2: Double) {
        lateinit var mLut: DoubleArray
        var mArcDistance = 0.0
        var mTime1: Double
        var mTime2: Double
        var mX1 = 0.0
        var mX2 = 0.0
        var mY1 = 0.0
        var mY2 = 0.0
        var mOneOverDeltaTime: Double
        var mEllipseA: Double = Double.NaN
        var mEllipseB: Double = Double.NaN
        var mEllipseCenterX // also used to cache the slope in the unused center
                : Double
        var mEllipseCenterY // also used to cache the slope in the unused center
                : Double
        var mArcVelocity: Double
        var mTmpSinAngle = 0.0
        var mTmpCosAngle = 0.0
        var mVertical: Boolean
        var mLinear = false

        init {
            mVertical = mode == START_VERTICAL
            mTime1 = t1
            mTime2 = t2
            mOneOverDeltaTime = 1 / (mTime2 - mTime1)
            if (START_LINEAR == mode) {
                mLinear = true
            }
            val dx = x2 - x1
            val dy = y2 - y1
            if (mLinear || abs(dx) < EPSILON || abs(dy) < EPSILON) {
                mLinear = true
                mX1 = x1
                mX2 = x2
                mY1 = y1
                mY2 = y2
                mArcDistance = hypot(dy, dx)
                mArcVelocity = mArcDistance * mOneOverDeltaTime
                mEllipseCenterX = dx / (mTime2 - mTime1) // cache the slope in the unused center
                mEllipseCenterY = dy / (mTime2 - mTime1) // cache the slope in the unused center
            } else {
                mLut = DoubleArray(101)
                mEllipseA = dx * if (mVertical) -1 else 1
                mEllipseB = dy * if (mVertical) 1 else -1
                mEllipseCenterX = if (mVertical) x2 else x1
                mEllipseCenterY = if (mVertical) y1 else y2
                buildTable(x1, y1, x2, y2)
                mArcVelocity = mArcDistance * mOneOverDeltaTime
            }
        }

        fun setPoint(time: Double) {
            val percent = (if (mVertical) mTime2 - time else time - mTime1) * mOneOverDeltaTime
            val angle: Double = PI * 0.5 * lookup(percent)
            mTmpSinAngle = sin(angle)
            mTmpCosAngle = cos(angle)
        }

        val x: Double
            get() = mEllipseCenterX + mEllipseA * mTmpSinAngle
        val y: Double
            get() = mEllipseCenterY + mEllipseB * mTmpCosAngle
        val dX: Double
            get() {
                val vx = mEllipseA * mTmpCosAngle
                val vy = -mEllipseB * mTmpSinAngle
                val norm: Double = mArcVelocity / hypot(vx, vy)
                return if (mVertical) -vx * norm else vx * norm
            }
        val dY: Double
            get() {
                val vx = mEllipseA * mTmpCosAngle
                val vy = -mEllipseB * mTmpSinAngle
                val norm: Double = mArcVelocity / hypot(vx, vy)
                return if (mVertical) -vy * norm else vy * norm
            }

        fun getLinearX(t: Double): Double {
            var t = t
            t = (t - mTime1) * mOneOverDeltaTime
            return mX1 + t * (mX2 - mX1)
        }

        fun getLinearY(t: Double): Double {
            var t = t
            t = (t - mTime1) * mOneOverDeltaTime
            return mY1 + t * (mY2 - mY1)
        }

        fun getLinearDX(t: Double): Double {
            return mEllipseCenterX
        }

        fun getLinearDY(t: Double): Double {
            return mEllipseCenterY
        }

        fun lookup(v: Double): Double {
            if (v <= 0) {
                return 0.0
            }
            if (v >= 1) {
                return 0.0
            }
            val pos = v * (mLut.size - 1)
            val iv = pos.toInt()
            val off = pos - pos.toInt()
            return mLut[iv] + off * (mLut[iv + 1] - mLut[iv])
        }

        private fun buildTable(x1: Double, y1: Double, x2: Double, y2: Double) {
            val a = x2 - x1
            val b = y1 - y2
            var lx = 0.0
            var ly = 0.0
            var dist = 0.0
            for (i in sOurPercent.indices) {
                val angle: Double = (90.0 * i / (sOurPercent.size - 1)) * PI / 180.0
                val s: Double = sin(angle)
                val c: Double = cos(angle)
                val px = a * s
                val py = b * c
                if (i > 0) {
                    dist += hypot(px - lx, py - ly)
                    sOurPercent[i] = dist
                }
                lx = px
                ly = py
            }
            mArcDistance = dist
            for (i in sOurPercent.indices) {
                sOurPercent[i] /= dist
            }
            for (i in mLut.indices) {
                val pos = i / (mLut.size - 1).toDouble()
                val index: Int = sOurPercent.binarySearch(pos).index
                if (index >= 0) {
                    mLut[i] = index / (sOurPercent.size - 1).toDouble()
                } else if (index == -1) {
                    mLut[i] = 0.0
                } else {
                    val p1 = -index - 2
                    val p2 = -index - 1
                    val ans = (p1 + (pos - sOurPercent[p1])
                            / (sOurPercent[p2] - sOurPercent[p1])) / (sOurPercent.size - 1)
                    mLut[i] = ans
                }
            }
        }

        companion object {
            private const val TAG = "Arc"
            private val sOurPercent = DoubleArray(91)
            private const val EPSILON = 0.001
        }
    }

    companion object {
        const val ARC_START_VERTICAL = 1
        const val ARC_START_HORIZONTAL = 2
        const val ARC_START_FLIP = 3
        const val ARC_START_LINEAR = 0
        private const val START_VERTICAL = 1
        private const val START_HORIZONTAL = 2
        private const val START_LINEAR = 3
    }
}
