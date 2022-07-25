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
package androidx.constraintlayout.core.motion.key

class MotionConstraintSet {
    private val mValidate = false
    var mIdString: String? = null
    var mRotate = 0

    companion object {
        private const val ERROR_MESSAGE = "XML parser error must be within a Constraint "
        private const val INTERNAL_MATCH_PARENT = -1
        private const val INTERNAL_WRAP_CONTENT = -2
        private const val INTERNAL_MATCH_CONSTRAINT = -3
        private const val INTERNAL_WRAP_CONTENT_CONSTRAINED = -4
        const val ROTATE_NONE = 0
        const val ROTATE_PORTRATE_OF_RIGHT = 1
        const val ROTATE_PORTRATE_OF_LEFT = 2
        const val ROTATE_RIGHT_OF_PORTRATE = 3
        const val ROTATE_LEFT_OF_PORTRATE = 4
    }
}
