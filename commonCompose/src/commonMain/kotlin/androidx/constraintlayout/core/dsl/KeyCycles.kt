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

/**
 * Provides the API for creating a KeyCycle Object for use in the Core
 * ConstraintLayout & MotionLayout system
 * This allows multiple KeyCycle positions to defined in one object.
 */
class KeyCycles internal constructor(numOfFrames: Int, vararg targets: String?) : KeyAttributes(numOfFrames, *targets) {
    enum class Wave {
        SIN, SQUARE, TRIANGLE, SAW, REVERSE_SAW, COS
    }

    var waveShape: Wave? = null
    var wavePeriod: FloatArray? = null
        private set
    var waveOffset: FloatArray? = null
        private set
    var wavePhase: FloatArray? = null
        private set

    init {
        TYPE = "KeyCycle"
    }

    fun setWavePeriod(vararg wavePeriod: Float) {
        this.wavePeriod = wavePeriod
    }

    fun setWaveOffset(vararg waveOffset: Float) {
        this.waveOffset = waveOffset
    }

    fun setWavePhase(vararg wavePhase: Float) {
        this.wavePhase = wavePhase
    }

    override fun attributesToString(builder: StringBuilder) {
        super.attributesToString(builder)
        if (waveShape != null) {
            builder.append("shape:'").append(waveShape).append("',\n")
        }
        append(builder, "period", wavePeriod)
        append(builder, "offset", waveOffset)
        append(builder, "phase", wavePhase)
    }
}
