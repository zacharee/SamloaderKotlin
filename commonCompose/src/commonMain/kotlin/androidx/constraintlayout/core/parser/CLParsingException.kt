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

class CLParsingException(private val mReason: String, element: CLElement?) : Exception() {
    private var mLineNumber = 0
    private var mElementClass: String? = null

    init {
        if (element != null) {
            mElementClass = element.strClass
            mLineNumber = element.line
        } else {
            mElementClass = "unknown"
            mLineNumber = 0
        }
    }

    /**
     * @TODO: add description
     */
    fun reason(): String {
        return "$mReason ($mElementClass at line $mLineNumber)"
    }

    override fun toString(): String {
        return "CLParsingException (${this.hashCode()}) : ${reason()}"
    }
}
