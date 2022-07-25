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

class CLArray(content: CharArray?) : CLContainer(content) {
    override fun toJSON(): String {
        val content: StringBuilder = StringBuilder(debugName + "[")
        var first = true
        for (i in mElements.indices) {
            if (!first) {
                content.append(", ")
            } else {
                first = false
            }
            content.append(mElements[i]?.toJSON())
        }
        return content.toString() + "]"
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json: StringBuilder = StringBuilder()
        val `val` = toJSON()
        if (forceIndent <= 0 && `val`.length + indent < CLElement.Companion.sMaxLine) {
            json.append(`val`)
        } else {
            json.append("[\n")
            var first = true
            for (element in mElements) {
                if (!first) {
                    json.append(",\n")
                } else {
                    first = false
                }
                addIndent(json, indent + CLElement.Companion.sBaseIndent)
                json.append(element?.toFormattedJSON(indent + CLElement.Companion.sBaseIndent, forceIndent - 1))
            }
            json.append("\n")
            addIndent(json, indent)
            json.append("]")
        }
        return json.toString()
    }

    companion object {
        /**
         * @TODO: add description
         */
        fun allocate(content: CharArray?): CLElement {
            return CLArray(content)
        }
    }
}
