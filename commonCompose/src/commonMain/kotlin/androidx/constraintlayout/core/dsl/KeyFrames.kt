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
 * Provides the API for creating a KeyFrames Object for use in the Core
 * ConstraintLayout & MotionLayout system
 * KeyFrames is a container for KeyAttribute,KeyCycle,KeyPosition etc.
 */
class KeyFrames {
    var mKeys: ArrayList<Keys> = ArrayList()

    fun add(keyFrame: Keys) {
        mKeys.add(keyFrame)
    }

    override fun toString(): String {
        val ret: StringBuilder = StringBuilder()
        if (!mKeys.isEmpty()) {
            ret.append("keyFrames:{\n")
            for (key in mKeys) {
                ret.append(key.toString())
            }
            ret.append("}\n")
        }
        return ret.toString()
    }
}
