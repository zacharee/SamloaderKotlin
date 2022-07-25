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
 * Create a Transition Object.
 * Transition objects reference the start and end Constraints
 */
class Transition {
    private var mOnSwipe: OnSwipe? = null
    val UNSET = -1
    private val DEFAULT_DURATION = 400
    private val DEFAULT_STAGGER = 0f
    var id: String? = null
    private var mConstraintSetEnd: String? = null
    private var mConstraintSetStart: String? = null
    private val mDefaultInterpolator = 0
    private val mDefaultInterpolatorString: String? = null
    private val mDefaultInterpolatorID = -1
    private var mDuration = DEFAULT_DURATION
    private var mStagger = DEFAULT_STAGGER
    private val mKeyFrames = KeyFrames()
    fun setOnSwipe(onSwipe: OnSwipe?) {
        mOnSwipe = onSwipe
    }

    fun setKeyFrames(keyFrames: Keys) {
        mKeyFrames.add(keyFrames)
    }

    constructor(from: String?, to: String?) {
        id = "default"
        mConstraintSetStart = from
        mConstraintSetEnd = to
    }

    constructor(id: String?, from: String?, to: String?) {
        this.id = id
        mConstraintSetStart = from
        mConstraintSetEnd = to
    }

    fun toJson(): String {
        return toString()
    }

    fun setTo(constraintSetEnd: String?) {
        mConstraintSetEnd = constraintSetEnd
    }

    fun setFrom(constraintSetStart: String?) {
        mConstraintSetStart = constraintSetStart
    }

    fun setDuration(duration: Int) {
        mDuration = duration
    }

    fun setStagger(stagger: Float) {
        mStagger = stagger
    }

    override fun toString(): String {
        var ret = "${id}:{from:'$mConstraintSetStart', to:'$mConstraintSetEnd',"
        if (mDuration != DEFAULT_DURATION) {
            ret += "duration:$mDuration,\n"
        }
        if (mStagger != DEFAULT_STAGGER) {
            ret += "stagger:$mStagger,\n"
        }
        if (mOnSwipe != null) {
            ret += mOnSwipe.toString()
        }
        ret += mKeyFrames.toString()
        ret += "}\n"
        return ret
    }
}
