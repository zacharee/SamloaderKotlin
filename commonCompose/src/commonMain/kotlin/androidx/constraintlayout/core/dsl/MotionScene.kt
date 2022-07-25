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
 * This defines to MotionScene container
 * It contains ConstraintSet and Transitions
 */
class MotionScene {
    var mTransitions: ArrayList<Transition> = ArrayList()
    var mConstraintSets: ArrayList<ConstraintSet> = ArrayList()

    // todo add support for variables, generate and helpers
    fun addTransition(transition: Transition) {
        mTransitions.add(transition)
    }

    fun addConstraintSet(constraintSet: ConstraintSet) {
        mConstraintSets.add(constraintSet)
    }

    override fun toString(): String {
        val ret = StringBuilder("{\n")
        if (!mTransitions.isEmpty()) {
            ret.append("Transitions:{\n")
            for (transition in mTransitions) {
                ret.append(transition.toString())
            }
            ret.append("}\n")
        }
        if (!mConstraintSets.isEmpty()) {
            ret.append("ConstraintSets:{\n")
            for (constraintSet in mConstraintSets) {
                ret.append(constraintSet.toString())
            }
            ret.append("}\n")
        }
        ret.append("}\n")
        return ret.toString()
    }
}
