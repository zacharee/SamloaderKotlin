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

class CLString(content: CharArray?) : CLElement(content) {
    override fun toJSON(): String {
        return "'" + content() + "'"
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json = StringBuilder()
        addIndent(json, indent)
        json.append("'")
        json.append(content())
        json.append("'")
        return json.toString()
    }

    companion object {
        /**
         * @TODO: add description
         */
        fun allocate(content: CharArray?): CLElement {
            return CLString(content)
        }
    }
}
