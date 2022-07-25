/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.constraintlayout.core.dsl

class KeyCycle internal constructor(frame: Int, target: String?) : KeyAttribute(frame, target) {
    var shape: Wave? = null
    var period = Float.NaN
    var offset = Float.NaN
    var phase = Float.NaN

    init {
        TYPE = "KeyCycle"
    }

    enum class Wave {
        SIN, SQUARE, TRIANGLE, SAW, REVERSE_SAW, COS
    }

    override fun attributesToString(builder: StringBuilder) {
        super.attributesToString(builder)
        if (shape != null) {
            builder.append("shape:'").append(shape).append("',\n")
        }
        append(builder, "period", period)
        append(builder, "offset", offset)
        append(builder, "phase", phase)
    }

    companion object {
        private const val TAG = "KeyCycle"
    }
}
