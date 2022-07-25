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

class TypedBundle {
    var mTypeInt = IntArray(INITIAL_INT)
    var mValueInt = IntArray(INITIAL_INT)
    var mCountInt = 0
    var mTypeFloat = IntArray(INITIAL_FLOAT)
    var mValueFloat = FloatArray(INITIAL_FLOAT)
    var mCountFloat = 0
    var mTypeString = IntArray(INITIAL_STRING)
    var mValueString = arrayOfNulls<String>(INITIAL_STRING)
    var mCountString = 0
    var mTypeBoolean = IntArray(INITIAL_BOOLEAN)
    var mValueBoolean = BooleanArray(INITIAL_BOOLEAN)
    var mCountBoolean = 0

    /**
     * @TODO: add description
     */
    fun getInteger(type: Int): Int {
        for (i in 0 until mCountInt) {
            if (mTypeInt[i] == type) {
                return mValueInt[i]
            }
        }
        return -1
    }

    /**
     * @TODO: add description
     */
    fun add(type: Int, value: Int) {
        if (mCountInt >= mTypeInt.size) {
            mTypeInt = mTypeInt.copyOf(mTypeInt.size * 2)
            mValueInt = mValueInt.copyOf(mValueInt.size * 2)
        }
        mTypeInt[mCountInt] = type
        mValueInt[mCountInt++] = value
    }

    /**
     * @TODO: add description
     */
    fun add(type: Int, value: Float) {
        if (mCountFloat >= mTypeFloat.size) {
            mTypeFloat = mTypeFloat.copyOf(mTypeFloat.size * 2)
            mValueFloat = mValueFloat.copyOf(mValueFloat.size * 2)
        }
        mTypeFloat[mCountFloat] = type
        mValueFloat[mCountFloat++] = value
    }

    /**
     * @TODO: add description
     */
    fun addIfNotNull(type: Int, value: String?) {
        value?.let { add(type, it) }
    }

    /**
     * @TODO: add description
     */
    fun add(type: Int, value: String?) {
        if (mCountString >= mTypeString.size) {
            mTypeString = mTypeString.copyOf(mTypeString.size * 2)
            mValueString = mValueString.copyOf(mValueString.size * 2)
        }
        mTypeString[mCountString] = type
        mValueString[mCountString++] = value
    }

    /**
     * @TODO: add description
     */
    fun add(type: Int, value: Boolean) {
        if (mCountBoolean >= mTypeBoolean.size) {
            mTypeBoolean = mTypeBoolean.copyOf(mTypeBoolean.size * 2)
            mValueBoolean = mValueBoolean.copyOf(mValueBoolean.size * 2)
        }
        mTypeBoolean[mCountBoolean] = type
        mValueBoolean[mCountBoolean++] = value
    }

    /**
     * @TODO: add description
     */
    fun applyDelta(values: TypedValues) {
        for (i in 0 until mCountInt) {
            values.setValue(mTypeInt[i], mValueInt[i])
        }
        for (i in 0 until mCountFloat) {
            values.setValue(mTypeFloat[i], mValueFloat[i])
        }
        for (i in 0 until mCountString) {
            values.setValue(mTypeString[i], mValueString[i])
        }
        for (i in 0 until mCountBoolean) {
            values.setValue(mTypeBoolean[i], mValueBoolean[i])
        }
    }

    /**
     * @TODO: add description
     */
    fun applyDelta(values: TypedBundle) {
        for (i in 0 until mCountInt) {
            values.add(mTypeInt[i], mValueInt[i])
        }
        for (i in 0 until mCountFloat) {
            values.add(mTypeFloat[i], mValueFloat[i])
        }
        for (i in 0 until mCountString) {
            values.add(mTypeString[i], mValueString[i])
        }
        for (i in 0 until mCountBoolean) {
            values.add(mTypeBoolean[i], mValueBoolean[i])
        }
    }

    /**
     * @TODO: add description
     */
    fun clear() {
        mCountBoolean = 0
        mCountString = 0
        mCountFloat = 0
        mCountInt = 0
    }

    override fun toString(): String {
        return "TypedBundle{" +
                "mCountInt=" + mCountInt +
                ", mCountFloat=" + mCountFloat +
                ", mCountString=" + mCountString +
                ", mCountBoolean=" + mCountBoolean +
                '}'
    }

    companion object {
        private const val INITIAL_BOOLEAN = 4
        private const val INITIAL_INT = 10
        private const val INITIAL_FLOAT = 10
        private const val INITIAL_STRING = 5
    }
}
