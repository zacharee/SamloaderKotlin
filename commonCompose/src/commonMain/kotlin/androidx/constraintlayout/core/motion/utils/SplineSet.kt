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

import androidx.constraintlayout.core.motion.CustomAttribute
import androidx.constraintlayout.core.motion.CustomVariable
import androidx.constraintlayout.core.motion.MotionWidget
import androidx.constraintlayout.core.motion.utils.KeyFrameArray.CustomArray
import androidx.constraintlayout.core.motion.utils.KeyFrameArray.CustomVar
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.core.state.WidgetFrame
import com.soywiz.korio.util.toStringDecimal

/**
 * This engine allows manipulation of attributes by Curves
 *
 * @DoNotShow
 */
abstract class SplineSet {
    var curveFit: CurveFit? = null
        protected set
    protected var mTimePoints = IntArray(10)
    protected var mValues = FloatArray(10)
    private var mCount = 0
    private var mType: String? = null

    /**
     * @TODO: add description
     */
    open fun setProperty(widget: TypedValues, t: Float) {
        widget.setValue(AttributesType.Companion.getId(mType), get(t))
    }

    override fun toString(): String {
        var str = mType
        for (i in 0 until mCount) {
            str += "[" + mTimePoints[i] + " , " + mValues[i].toDouble().toStringDecimal(2) + "] "
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
        return curveFit!!.getPos(t.toDouble(), 0).toFloat()
    }

    /**
     * @TODO: add description
     */
    fun getSlope(t: Float): Float {
        return curveFit!!.getSlope(t.toDouble(), 0).toFloat()
    }

    /**
     * @TODO: add description
     */
    open fun setPoint(position: Int, value: Float) {
        if (mTimePoints.size < mCount + 1) {
            mTimePoints = mTimePoints.copyOf(mTimePoints.size * 2)
            mValues = mValues.copyOf(mValues.size * 2)
        }
        mTimePoints[mCount] = position
        mValues[mCount] = value
        mCount++
    }

    /**
     * @TODO: add description
     */
    open fun setup(curveType: Int) {
        if (mCount == 0) {
            return
        }
        Sort.doubleQuickSort(mTimePoints, mValues, 0, mCount - 1)
        var unique = 1
        for (i in 1 until mCount) {
            if (mTimePoints[i - 1] != mTimePoints[i]) {
                unique++
            }
        }
        val time = DoubleArray(unique)
        val values = Array(unique) { DoubleArray(1) }
        var k = 0
        for (i in 0 until mCount) {
            if (i > 0 && mTimePoints[i] == mTimePoints[i - 1]) {
                continue
            }
            time[k] = mTimePoints[i] * 1E-2
            values[k][0] = mValues[i].toDouble()
            k++
        }
        curveFit = CurveFit.Companion.get(curveType, time, values)
    }

    private object Sort {
        fun doubleQuickSort(key: IntArray, value: FloatArray, low: Int, hi: Int) {
            var low = low
            var hi = hi
            val stack = IntArray(key.size + 10)
            var count = 0
            stack[count++] = hi
            stack[count++] = low
            while (count > 0) {
                low = stack[--count]
                hi = stack[--count]
                if (low < hi) {
                    val p = partition(key, value, low, hi)
                    stack[count++] = p - 1
                    stack[count++] = low
                    stack[count++] = hi
                    stack[count++] = p + 1
                }
            }
        }

        private fun partition(array: IntArray, value: FloatArray, low: Int, hi: Int): Int {
            val pivot = array[hi]
            var i = low
            for (j in low until hi) {
                if (array[j] <= pivot) {
                    swap(array, value, i, j)
                    i++
                }
            }
            swap(array, value, i, hi)
            return i
        }

        private fun swap(array: IntArray, value: FloatArray, a: Int, b: Int) {
            val tmp = array[a]
            array[a] = array[b]
            array[b] = tmp
            val tmpv = value[a]
            value[a] = value[b]
            value[b] = tmpv
        }
    }

    class CustomSet(attribute: String, attrList: CustomArray) : SplineSet() {
        var mAttributeName: String
        var mConstraintAttributeList: CustomArray
        lateinit var mTempValues: FloatArray

        init {
            mAttributeName = attribute.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            mConstraintAttributeList = attrList
        }

        /**
         * @TODO: add description
         */
        override fun setup(curveType: Int) {
            val size = mConstraintAttributeList.size()
            val dimensionality: Int = mConstraintAttributeList.valueAt(0)!!.numberOfInterpolatedValues()
            val time = DoubleArray(size)
            mTempValues = FloatArray(dimensionality)
            val values = Array(size) { DoubleArray(dimensionality) }
            for (i in 0 until size) {
                val key = mConstraintAttributeList.keyAt(i)
                val ca = mConstraintAttributeList.valueAt(i)
                time[i] = key * 1E-2
                ca!!.getValuesToInterpolate(mTempValues)
                for (k in mTempValues.indices) {
                    values[i][k] = mTempValues[k].toDouble()
                }
            }
            curveFit = CurveFit.Companion.get(curveType, time, values)
        }

        /**
         * @TODO: add description
         */
        override fun setPoint(position: Int, value: Float) {
            throw RuntimeException(
                "don't call for custom "
                        + "attribute call setPoint(pos, ConstraintAttribute)"
            )
        }

        /**
         * @TODO: add description
         */
        fun setPoint(position: Int, value: CustomAttribute?) {
            mConstraintAttributeList.append(position, value)
        }

        /**
         * @TODO: add description
         */
        fun setProperty(view: WidgetFrame, t: Float) {
            curveFit!!.getPos(t.toDouble(), mTempValues)
            view.setCustomValue(mConstraintAttributeList.valueAt(0), mTempValues)
        }
    }

    private class CoreSpline internal constructor(var mType: String, var mStart: Long) : SplineSet() {
        override fun setProperty(widget: TypedValues, t: Float) {
            val id = widget.getId(mType)
            widget.setValue(id, get(t))
        }
    }

    class CustomSpline(attribute: String, attrList: CustomVar) : SplineSet() {
        var mAttributeName: String
        var mConstraintAttributeList: CustomVar
        lateinit var mTempValues: FloatArray

        init {
            mAttributeName = attribute.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            mConstraintAttributeList = attrList
        }

        /**
         * @TODO: add description
         */
        override fun setup(curveType: Int) {
            val size = mConstraintAttributeList.size()
            val dimensionality: Int = mConstraintAttributeList.valueAt(0)!!.numberOfInterpolatedValues()
            val time = DoubleArray(size)
            mTempValues = FloatArray(dimensionality)
            val values = Array(size) { DoubleArray(dimensionality) }
            for (i in 0 until size) {
                val key = mConstraintAttributeList.keyAt(i)
                val ca = mConstraintAttributeList.valueAt(i)
                time[i] = key * 1E-2
                ca!!.getValuesToInterpolate(mTempValues)
                for (k in mTempValues.indices) {
                    values[i][k] = mTempValues[k].toDouble()
                }
            }
            curveFit = CurveFit.Companion.get(curveType, time, values)
        }

        /**
         * @TODO: add description
         */
        override fun setPoint(position: Int, value: Float) {
            throw RuntimeException(
                "don't call for custom attribute"
                        + " call setPoint(pos, ConstraintAttribute)"
            )
        }

        /**
         * @TODO: add description
         */
        override fun setProperty(widget: TypedValues, t: Float) {
            setProperty(widget as MotionWidget, t)
        }

        /**
         * @TODO: add description
         */
        fun setPoint(position: Int, value: CustomVariable?) {
            mConstraintAttributeList.append(position, value)
        }

        /**
         * @TODO: add description
         */
        fun setProperty(view: MotionWidget, t: Float) {
            curveFit!!.getPos(t.toDouble(), mTempValues)
            mConstraintAttributeList.valueAt(0)!!.setInterpolatedValue(view, mTempValues)
        }
    }

    companion object {
        private const val TAG = "SplineSet"

        /**
         * @TODO: add description
         */
        fun makeCustomSpline(str: String, attrList: CustomArray): SplineSet {
            return CustomSet(str, attrList)
        }

        /**
         * @TODO: add description
         */
        fun makeCustomSplineSet(str: String, attrList: CustomVar): SplineSet {
            return CustomSpline(str, attrList)
        }

        /**
         * @TODO: add description
         */
        fun makeSpline(str: String, currentTime: Long): SplineSet {
            return CoreSpline(str, currentTime)
        }
    }
}
