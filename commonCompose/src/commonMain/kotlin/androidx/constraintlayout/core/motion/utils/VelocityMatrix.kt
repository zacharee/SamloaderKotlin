/*
 * Copyright (C) 2020 The Android Open Source Project
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
package androidx.constraintlayout.core.motion.utils

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * This is used to calculate the related velocity matrix for a post layout matrix
 *
 * @DoNotShow
 */
class VelocityMatrix {
    var mDScaleX = 0f
    var mDScaleY = 0f
    var mDTranslateX = 0f
    var mDTranslateY = 0f
    var mDRotate = 0f
    var mRotate = 0f

    /**
     * @TODO: add description
     */
    fun clear() {
        mDRotate = 0f
        mDTranslateY = mDRotate
        mDTranslateX = mDTranslateY
        mDScaleY = mDTranslateX
        mDScaleX = mDScaleY
    }

    /**
     * @TODO: add description
     */
    fun setRotationVelocity(rot: SplineSet?, position: Float) {
        if (rot != null) {
            mDRotate = rot.getSlope(position)
            mRotate = rot[position]
        }
    }

    /**
     * @TODO: add description
     */
    fun setTranslationVelocity(transX: SplineSet?, transY: SplineSet?, position: Float) {
        if (transX != null) {
            mDTranslateX = transX.getSlope(position)
        }
        if (transY != null) {
            mDTranslateY = transY.getSlope(position)
        }
    }

    /**
     * @TODO: add description
     */
    fun setScaleVelocity(scaleX: SplineSet?, scaleY: SplineSet?, position: Float) {
        if (scaleX != null) {
            mDScaleX = scaleX.getSlope(position)
        }
        if (scaleY != null) {
            mDScaleY = scaleY.getSlope(position)
        }
    }

    /**
     * @TODO: add description
     */
    fun setRotationVelocity(oscR: KeyCycleOscillator?, position: Float) {
        if (oscR != null) {
            mDRotate = oscR.getSlope(position)
        }
    }

    /**
     * @TODO: add description
     */
    fun setTranslationVelocity(
        oscX: KeyCycleOscillator?,
        oscY: KeyCycleOscillator?,
        position: Float
    ) {
        if (oscX != null) {
            mDTranslateX = oscX.getSlope(position)
        }
        if (oscY != null) {
            mDTranslateY = oscY.getSlope(position)
        }
    }

    /**
     * @TODO: add description
     */
    fun setScaleVelocity(
        oscSx: KeyCycleOscillator?,
        oscSy: KeyCycleOscillator?,
        position: Float
    ) {
        if (oscSx != null) {
            mDScaleX = oscSx.getSlope(position)
        }
        if (oscSy != null) {
            mDScaleY = oscSy.getSlope(position)
        }
    }

    /**
     * Apply the transform a velocity vector
     *
     * @DoNotShow
     */
    fun applyTransform(
        locationX: Float,
        locationY: Float,
        width: Int,
        height: Int,
        mAnchorDpDt: FloatArray
    ) {
        var dx = mAnchorDpDt[0]
        var dy = mAnchorDpDt[1]
        val offx = 2 * (locationX - 0.5f)
        val offy = 2 * (locationY - 0.5f)
        dx += mDTranslateX
        dy += mDTranslateY
        dx += offx * mDScaleX
        dy += offy * mDScaleY
        val r: Float = (mRotate.toDouble() * PI / 180.0).toFloat()
        val dr: Float = (mDRotate.toDouble() * PI / 180.0).toFloat()
        dx += dr * (-width * offx * sin(r.toDouble()) - height * offy * cos(r.toDouble())).toFloat()
        dy += dr * (width * offx * cos(r.toDouble()) - height * offy * sin(r.toDouble())).toFloat()
        mAnchorDpDt[0] = dx
        mAnchorDpDt[1] = dy
    }

    companion object {
        private const val sTag = "VelocityMatrix"
    }
}
