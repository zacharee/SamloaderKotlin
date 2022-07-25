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
package androidx.constraintlayout.core.motion.utils

import androidx.constraintlayout.core.motion.MotionWidget

class ViewState {
    var rotation = 0f
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0

    /**
     * @TODO: add description
     */
    fun getState(v: MotionWidget) {
        left = v.left
        top = v.top
        right = v.right
        bottom = v.bottom
        rotation = v.rotationZ.toInt().toFloat()
    }

    /**
     * @TODO: add description
     */
    fun width(): Int {
        return right - left
    }

    /**
     * @TODO: add description
     */
    fun height(): Int {
        return bottom - top
    }
}
