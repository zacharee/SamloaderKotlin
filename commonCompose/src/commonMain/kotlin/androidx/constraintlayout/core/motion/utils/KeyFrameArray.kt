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
package androidx.constraintlayout.core.motion.utils

import androidx.constraintlayout.core.motion.CustomAttribute
import androidx.constraintlayout.core.motion.CustomVariable

class KeyFrameArray {
    // =================================== CustomAttribute =================================
    class CustomArray {
        var mKeys = IntArray(101)
        var mValues = arrayOfNulls<CustomAttribute>(101)
        var mCount = 0

        init {
            clear()
        }

        /**
         * @TODO: add description
         */
        fun clear() {
            mKeys.fill(EMPTY)
            mValues.fill(null)
            mCount = 0
        }

        /**
         * @TODO: add description
         */
        fun dump() {
            println("V: " + mKeys.copyOf(mCount).contentToString())
            print("K: [")
            for (i in 0 until mCount) {
                print((if (i == 0) "" else ", ") + valueAt(i))
            }
            println("]")
        }

        /**
         * @TODO: add description
         */
        fun size(): Int {
            return mCount
        }

        /**
         * @TODO: add description
         */
        fun valueAt(i: Int): CustomAttribute? {
            return mValues[mKeys[i]]
        }

        /**
         * @TODO: add description
         */
        fun keyAt(i: Int): Int {
            return mKeys[i]
        }

        /**
         * @TODO: add description
         */
        fun append(position: Int, value: CustomAttribute?) {
            if (mValues[position] != null) {
                remove(position)
            }
            mValues[position] = value
            mKeys[mCount++] = position
            mKeys.sort()
        }

        /**
         * @TODO: add description
         */
        fun remove(position: Int) {
            mValues[position] = null
            var j = 0
            var i = 0
            while (i < mCount) {
                if (position == mKeys[i]) {
                    mKeys[i] = EMPTY
                    j++
                }
                if (i != j) {
                    mKeys[i] = mKeys[j]
                }
                j++
                i++
            }
            mCount--
        }

        companion object {
            private const val EMPTY = 999
        }
    }

    // =================================== CustomVar =================================
    class CustomVar {
        var mKeys = IntArray(101)
        var mValues = arrayOfNulls<CustomVariable>(101)
        var mCount = 0

        init {
            clear()
        }

        /**
         * @TODO: add description
         */
        fun clear() {
            mKeys.fill(EMPTY)
            mValues.fill(null)
            mCount = 0
        }

        /**
         * @TODO: add description
         */
        fun dump() {
            println("V: ${mKeys.copyOf(mCount).contentToString()}")
            print("K: [")
            for (i in 0 until mCount) {
                print((if (i == 0) "" else ", ") + valueAt(i))
            }
            println("]")
        }

        /**
         * @TODO: add description
         */
        fun size(): Int {
            return mCount
        }

        /**
         * @TODO: add description
         */
        fun valueAt(i: Int): CustomVariable? {
            return mValues[mKeys[i]]
        }

        /**
         * @TODO: add description
         */
        fun keyAt(i: Int): Int {
            return mKeys[i]
        }

        /**
         * @TODO: add description
         */
        fun append(position: Int, value: CustomVariable?) {
            if (mValues[position] != null) {
                remove(position)
            }
            mValues[position] = value
            mKeys[mCount++] = position
            mKeys.sort()
        }

        /**
         * @TODO: add description
         */
        fun remove(position: Int) {
            mValues[position] = null
            var j = 0
            var i = 0
            while (i < mCount) {
                if (position == mKeys[i]) {
                    mKeys[i] = EMPTY
                    j++
                }
                if (i != j) {
                    mKeys[i] = mKeys[j]
                }
                j++
                i++
            }
            mCount--
        }

        companion object {
            private const val EMPTY = 999
        }
    }

    // =================================== FloatArray ======================================
    class FloatArray {
        var mKeys = IntArray(101)
        var mValues = arrayOfNulls<kotlin.FloatArray>(101)
        var mCount = 0

        init {
            clear()
        }

        fun clear() {
            mKeys.fill(EMPTY)
            mValues.fill(null)
            mCount = 0
        }

        fun dump() {
            println("V: ${mKeys.copyOf(mCount).contentToString()}")
            print("K: [")
            for (i in 0 until mCount) {
                print((if (i == 0) "" else ", ") + valueAt(i).contentToString())
            }
            println("]")
        }

        fun size(): Int {
            return mCount
        }

        fun valueAt(i: Int): kotlin.FloatArray? {
            return mValues[mKeys[i]]
        }

        fun keyAt(i: Int): Int {
            return mKeys[i]
        }

        fun append(position: Int, value: kotlin.FloatArray?) {
            if (mValues[position] != null) {
                remove(position)
            }
            mValues[position] = value
            mKeys[mCount++] = position
            mKeys.sort()
        }

        fun remove(position: Int) {
            mValues[position] = null
            var j = 0
            var i = 0
            while (i < mCount) {
                if (position == mKeys[i]) {
                    mKeys[i] = EMPTY
                    j++
                }
                if (i != j) {
                    mKeys[i] = mKeys[j]
                }
                j++
                i++
            }
            mCount--
        }

        companion object {
            private const val EMPTY = 999
        }
    }
}
