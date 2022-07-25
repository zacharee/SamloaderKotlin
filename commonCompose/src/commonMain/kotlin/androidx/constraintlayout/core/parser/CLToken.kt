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

class CLToken(content: CharArray?) : CLElement(content) {
    var mIndex = 0
    var type = Type.UNKNOWN

    /**
     * @TODO: add description
     */
    @get:Throws(CLParsingException::class)
    val boolean: Boolean
        get() {
            if (type == Type.TRUE) {
                return true
            }
            if (type == Type.FALSE) {
                return false
            }
            throw CLParsingException("this token is not a boolean: <" + content() + ">", this)
        }

    /**
     * @TODO: add description
     */
    @get:Throws(CLParsingException::class)
    val isNull: Boolean
        get() {
            if (type == Type.NULL) {
                return true
            }
            throw CLParsingException("this token is not a null: <" + content() + ">", this)
        }

    enum class Type {
        UNKNOWN, TRUE, FALSE, NULL
    }

    var mTokenTrue = "true".toCharArray()
    var mTokenFalse = "false".toCharArray()
    var mTokenNull = "null".toCharArray()
    override fun toJSON(): String {
        return if (CLParser.Companion.sDebug) {
            "<" + content() + ">"
        } else {
            content()
        }
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json: StringBuilder = StringBuilder()
        addIndent(json, indent)
        json.append(content())
        return json.toString()
    }

    /**
     * @TODO: add description
     */
    fun validate(c: Char, position: Long): Boolean {
        var isValid = false
        when (type) {
            Type.TRUE -> {
                isValid = mTokenTrue[mIndex] == c
                if (isValid && mIndex + 1 == mTokenTrue.size) {
                    end = position
                }
            }

            Type.FALSE -> {
                isValid = mTokenFalse[mIndex] == c
                if (isValid && mIndex + 1 == mTokenFalse.size) {
                    end = position
                }
            }

            Type.NULL -> {
                isValid = mTokenNull[mIndex] == c
                if (isValid && mIndex + 1 == mTokenNull.size) {
                    end = position
                }
            }

            Type.UNKNOWN -> {
                if (mTokenTrue[mIndex] == c) {
                    type = Type.TRUE
                    isValid = true
                } else if (mTokenFalse[mIndex] == c) {
                    type = Type.FALSE
                    isValid = true
                } else if (mTokenNull[mIndex] == c) {
                    type = Type.NULL
                    isValid = true
                }
            }
        }
        mIndex++
        return isValid
    }

    companion object {
        /**
         * @TODO: add description
         */
        fun allocate(content: CharArray?): CLElement {
            return CLToken(content)
        }
    }
}
