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
package androidx.constraintlayout.core.parser

class CLNumber : CLElement {
    var mValue = Float.NaN

    constructor(content: CharArray?) : super(content) {}
    constructor(value: Float) : super(null) {
        mValue = value
    }

    override fun toJSON(): String {
        val value = float
        val intValue = value.toInt()
        return if (intValue.toFloat() == value) {
            "" + intValue
        } else "" + value
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json: StringBuilder = StringBuilder()
        addIndent(json, indent)
        val value = float
        val intValue = value.toInt()
        if (intValue.toFloat() == value) {
            json.append(intValue)
        } else {
            json.append(value)
        }
        return json.toString()
    }

    /**
     * @TODO: add description
     */
    val isInt: Boolean
        get() {
            val value = float
            val intValue = value.toInt()
            return intValue.toFloat() == value
        }

    override val int: Int
        get() {
            if (mValue.isNaN()) {
                mValue = content().toInt().toFloat()
            }
            return mValue.toInt()
        }

    override val float: Float
        get() {
            if (mValue.isNaN()) {
                mValue = content().toFloat()
            }
            return mValue
        }

    /**
     * @TODO: add description
     */
    fun putValue(value: Float) {
        mValue = value
    }

    companion object {
        /**
         * @TODO: add description
         */
        fun allocate(content: CharArray?): CLElement {
            return CLNumber(content)
        }
    }
}
