/*
 * Copyright 2021 The Android Open Source Project
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

interface RegistryCallback {
    /**
     * @TODO: add description
     */
    fun onNewMotionScene(content: String?)

    /**
     * @TODO: add description
     */
    fun onProgress(progress: Float)

    /**
     * @TODO: add description
     */
    fun onDimensions(width: Int, height: Int)

    /**
     * @TODO: add description
     */
    fun currentMotionScene(): String?

    /**
     * @TODO: add description
     */
    fun setDrawDebug(debugMode: Int)

    /**
     * @TODO: add description
     */
    fun currentLayoutInformation(): String?

    /**
     * @TODO: add description
     */
    fun setLayoutInformationMode(layoutInformationMode: Int)

    /**
     * @TODO: add description
     */
    val lastModified: Long
}
