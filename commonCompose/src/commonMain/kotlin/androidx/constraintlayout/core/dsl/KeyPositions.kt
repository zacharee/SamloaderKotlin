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
 * Provides the API for creating a KeyPosition Object for use in the Core
 * ConstraintLayout & MotionLayout system
 * This allows multiple KeyPosition positions to defined in one object.
 */
class KeyPositions(numOfFrames: Int, vararg targets: String?) : Keys() {
    var target: Array<out String?>? = null
    var transitionEasing: String? = null
    var positionType: Type? = null
    var frames: IntArray? = null
        private set
    var percentWidth: FloatArray? = null
        private set
    var percentHeight: FloatArray? = null
        private set
    var percentX: FloatArray? = null
        private set
    var percentY: FloatArray? = null
        private set

    enum class Type {
        CARTESIAN, SCREEN, PATH
    }

    init {
        target = targets
        frames = IntArray(numOfFrames)
        // the default is evenly spaced  1 at 50, 2 at 33 & 66, 3 at 25,50,75
        val gap = 100f / (frames!!.size + 1)
        for (i in frames!!.indices) {
            frames!![i] = (i * gap + gap).toInt()
        }
    }

    fun setFrames(vararg frames: Int) {
        this.frames = frames
    }

    fun setPercentWidth(vararg percentWidth: Float) {
        this.percentWidth = percentWidth
    }

    fun setPercentHeight(vararg percentHeight: Float) {
        this.percentHeight = percentHeight
    }

    fun setPercentX(vararg percentX: Float) {
        this.percentX = percentX
    }

    fun setPercentY(vararg percentY: Float) {
        this.percentY = percentY
    }

    override fun toString(): String {
        val ret: StringBuilder = StringBuilder()
        ret.append("KeyPositions:{\n")
        append(ret, "target", target.contentToString())
        ret.append("frame:").append(frames?.contentToString()).append(",\n")
        if (positionType != null) {
            ret.append("type:'").append(positionType).append("',\n")
        }
        append(ret, "easing", transitionEasing)
        append(ret, "percentX", percentX)
        append(ret, "percentX", percentY)
        append(ret, "percentWidth", percentWidth)
        append(ret, "percentHeight", percentHeight)
        ret.append("}\n")
        return ret.toString()
    }
}
