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
 * Provides the API for creating a KeyAttribute Object for use in the Core
 * ConstraintLayout & MotionLayout system
 */
open class KeyAttribute(frame: Int, target: String?) : Keys() {
    protected var TYPE = "KeyAttributes"
    var target: String? = null
    private var mFrame = 0
    var transitionEasing: String? = null
    var curveFit: Fit? = null
    var visibility: Visibility? = null
    var alpha = Float.NaN
    var rotation = Float.NaN
    var rotationX = Float.NaN
    var rotationY = Float.NaN
    var pivotX = Float.NaN
    var pivotY = Float.NaN
    var transitionPathRotate = Float.NaN
    var scaleX = Float.NaN
    var scaleY = Float.NaN
    var translationX = Float.NaN
    var translationY = Float.NaN
    var translationZ = Float.NaN

    init {
        this.target = target
        mFrame = frame
    }

    enum class Fit {
        spline, linear
    }

    enum class Visibility {
        VISIBLE, INVISIBLE, GONE
    }

    override fun toString(): String {
        val ret = StringBuilder()
        ret.append(TYPE)
        ret.append(":{\n")
        attributesToString(ret)
        ret.append("}\n")
        return ret.toString()
    }

    protected open fun attributesToString(builder: StringBuilder) {
        append(builder, "target", target)
        builder.append("frame:").append(mFrame).append(",\n")
        append(builder, "easing", transitionEasing)
        if (curveFit != null) {
            builder.append("fit:'").append(curveFit).append("',\n")
        }
        if (visibility != null) {
            builder.append("visibility:'").append(visibility).append("',\n")
        }
        append(builder, "alpha", alpha)
        append(builder, "rotationX", rotationX)
        append(builder, "rotationY", rotationY)
        append(builder, "rotationZ", rotation)
        append(builder, "pivotX", pivotX)
        append(builder, "pivotY", pivotY)
        append(builder, "pathRotate", transitionPathRotate)
        append(builder, "scaleX", scaleX)
        append(builder, "scaleY", scaleY)
        append(builder, "translationX", translationX)
        append(builder, "translationY", translationY)
        append(builder, "translationZ", translationZ)
    }
}
