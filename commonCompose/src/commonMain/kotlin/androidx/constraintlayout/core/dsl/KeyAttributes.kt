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
 * This allows multiple KeyAttribute positions to defined in one object.
 */
open class KeyAttributes internal constructor(numOfFrames: Int, vararg targets: String?) : Keys() {
    protected var TYPE = "KeyAttributes"
    var target: Array<out String?>? = null
    var transitionEasing: String? = null
    var curveFit: Fit? = null
    private var mFrames: IntArray? = null
    var visibility: Array<out Visibility>? = null
        private set
    var alpha: FloatArray? = null
        private set
    var rotation: FloatArray? = null
        private set
    var rotationX: FloatArray? = null
        private set
    var rotationY: FloatArray? = null
        private set
    var pivotX: FloatArray? = null
        private set
    var pivotY: FloatArray? = null
        private set
    var transitionPathRotate: FloatArray? = null
        private set
    var scaleX: FloatArray? = null
    var scaleY: FloatArray? = null
    var translationX: FloatArray? = null
    var translationY: FloatArray? = null
    var translationZ: FloatArray? = null

    enum class Fit {
        spline, linear
    }

    enum class Visibility {
        VISIBLE, INVISIBLE, GONE
    }

    init {
        target = targets
        mFrames = IntArray(numOfFrames)
        // the default is evenly spaced  1 at 50, 2 at 33 & 66, 3 at 25,50,75
        val gap = 100f / (mFrames!!.size + 1)
        for (i in mFrames!!.indices) {
            mFrames!![i] = (i * gap + gap).toInt()
        }
    }

    fun setVisibility(vararg visibility: Visibility) {
        this.visibility = visibility
    }

    fun setAlpha(vararg alpha: Float) {
        this.alpha = alpha
    }

    fun setRotation(vararg rotation: Float) {
        this.rotation = rotation
    }

    fun setRotationX(vararg rotationX: Float) {
        this.rotationX = rotationX
    }

    fun setRotationY(vararg rotationY: Float) {
        this.rotationY = rotationY
    }

    fun setPivotX(vararg pivotX: Float) {
        this.pivotX = pivotX
    }

    fun setPivotY(vararg pivotY: Float) {
        this.pivotY = pivotY
    }

    fun setTransitionPathRotate(vararg transitionPathRotate: Float) {
        this.transitionPathRotate = transitionPathRotate
    }

    override fun toString(): String {
        val ret: StringBuilder = StringBuilder()
        ret.append(TYPE)
        ret.append(":{\n")
        attributesToString(ret)
        ret.append("}\n")
        return ret.toString()
    }

    protected open fun attributesToString(builder: StringBuilder) {
        append(builder, "target", target.contentToString())
        builder.append("frame:").append(mFrames?.toList()).append(",\n")
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
