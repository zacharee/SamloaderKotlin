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
package androidx.constraintlayout.core.state

/**
 * This defines the interface to motionScene functionality
 */
interface CoreMotionScene {
    /**
     * set the Transitions string onto the MotionScene
     *
     * @param elementName the name of the element
     */
    fun setTransitionContent(elementName: String, toJSON: String)

    /**
     * Get the ConstraintSet as a string
     */
    fun getConstraintSet(ext: String): String?

    /**
     * set the constraintSet json string
     *
     * @param csName the name of the constraint set
     * @param toJSON the json string of the constraintset
     */
    fun setConstraintSetContent(csName: String, toJSON: String)

    /**
     * set the debug name for remote access
     *
     * @param name name to call this motion scene
     */
    fun setDebugName(name: String?)

    /**
     * get a transition give the name
     *
     * @param str the name of the transition
     * @return the json of the transition
     */
    fun getTransition(str: String?): String?

    /**
     * get a constraintset
     *
     * @param index of the constraintset
     */
    fun getConstraintSet(index: Int): String?
}
