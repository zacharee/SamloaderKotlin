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

class Registry {
    private val mCallbacks: HashMap<String, RegistryCallback> = HashMap()

    /**
     * @TODO: add description
     */
    fun register(name: String, callback: RegistryCallback) {
        mCallbacks[name] = callback
    }

    /**
     * @TODO: add description
     */
    fun unregister(name: String, callback: RegistryCallback?) {
        mCallbacks.remove(name)
    }

    /**
     * @TODO: add description
     */
    fun updateContent(name: String?, content: String?) {
        mCallbacks.get(name)?.onNewMotionScene(content)
    }

    /**
     * @TODO: add description
     */
    fun updateProgress(name: String?, progress: Float) {
        mCallbacks[name]?.onProgress(progress)
    }

    /**
     * @TODO: add description
     */
    fun currentContent(name: String?): String? {
        return mCallbacks[name]?.currentMotionScene()
    }

    /**
     * @TODO: add description
     */
    fun currentLayoutInformation(name: String?): String? {
        return mCallbacks[name]?.currentLayoutInformation()
    }

    /**
     * @TODO: add description
     */
    fun setDrawDebug(name: String?, debugMode: Int) {
        mCallbacks[name]?.setDrawDebug(debugMode)
    }

    /**
     * @TODO: add description
     */
    fun setLayoutInformationMode(name: String?, mode: Int) {
        mCallbacks[name]?.setLayoutInformationMode(mode)
    }

    val layoutList: Set<String>
        get() = mCallbacks.keys

    /**
     * @TODO: add description
     */
    fun getLastModified(name: String?): Long {
        val callback = mCallbacks[name]
        return callback?.lastModified ?: Long.MAX_VALUE
    }

    /**
     * @TODO: add description
     */
    fun updateDimensions(name: String?, width: Int, height: Int) {
        mCallbacks[name]?.onDimensions(width, height)
    }

    companion object {
        val instance = Registry()
    }
}
