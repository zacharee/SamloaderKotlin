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
 */
class KeyPosition(firstTarget: String?, frame: Int) : Keys() {
    var target: String? = null
    var transitionEasing: String? = null
    var frames = 0
    var percentWidth = Float.NaN
    var percentHeight = Float.NaN
    var percentX = Float.NaN
    var percentY = Float.NaN
    var positionType: Type? = Type.CARTESIAN

    enum class Type {
        CARTESIAN, SCREEN, PATH
    }

    init {
        target = firstTarget
        frames = frame
    }

    override fun toString(): String {
        val ret: StringBuilder = StringBuilder()
        ret.append("KeyPositions:{\n")
        append(ret, "target", target)
        ret.append("frame:").append(frames).append(",\n")
        if (positionType != null) {
            ret.append("type:'").append(positionType).append("',\n")
        }
        append(ret, "easing", transitionEasing)
        append(ret, "percentX", percentX)
        append(ret, "percentY", percentY)
        append(ret, "percentWidth", percentWidth)
        append(ret, "percentHeight", percentHeight)
        ret.append("}\n")
        return ret.toString()
    }
}
