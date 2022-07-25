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
    private val mCallbacks: java.util.HashMap<String, RegistryCallback> = java.util.HashMap<String, RegistryCallback>()

    /**
     * @TODO: add description
     */
    fun register(name: String?, callback: RegistryCallback?) {
        mCallbacks.put(name, callback)
    }

    /**
     * @TODO: add description
     */
    fun unregister(name: String?, callback: RegistryCallback?) {
        mCallbacks.remove(name)
    }

    /**
     * @TODO: add description
     */
    fun updateContent(name: String?, content: String?) {
        val callback: RegistryCallback = mCallbacks.get(name)
        if (callback != null) {
            callback.onNewMotionScene(content)
        }
    }

    /**
     * @TODO: add description
     */
    fun updateProgress(name: String?, progress: Float) {
        val callback: RegistryCallback = mCallbacks.get(name)
        if (callback != null) {
            callback.onProgress(progress)
        }
    }

    /**
     * @TODO: add description
     */
    fun currentContent(name: String?): String? {
        val callback: RegistryCallback = mCallbacks.get(name)
        return if (callback != null) {
            callback.currentMotionScene()
        } else null
    }

    /**
     * @TODO: add description
     */
    fun currentLayoutInformation(name: String?): String? {
        val callback: RegistryCallback = mCallbacks.get(name)
        return if (callback != null) {
            callback.currentLayoutInformation()
        } else null
    }

    /**
     * @TODO: add description
     */
    fun setDrawDebug(name: String?, debugMode: Int) {
        val callback: RegistryCallback = mCallbacks.get(name)
        if (callback != null) {
            callback.setDrawDebug(debugMode)
        }
    }

    /**
     * @TODO: add description
     */
    fun setLayoutInformationMode(name: String?, mode: Int) {
        val callback: RegistryCallback = mCallbacks.get(name)
        if (callback != null) {
            callback.setLayoutInformationMode(mode)
        }
    }

    val layoutList: Set<String>
        get() = mCallbacks.keys

    /**
     * @TODO: add description
     */
    fun getLastModified(name: String?): Long {
        val callback: RegistryCallback = mCallbacks.get(name)
        return callback?.lastModified ?: Long.MAX_VALUE
    }

    /**
     * @TODO: add description
     */
    fun updateDimensions(name: String?, width: Int, height: Int) {
        val callback: RegistryCallback = mCallbacks.get(name)
        if (callback != null) {
            callback.onDimensions(width, height)
        }
    }

    companion object {
        val instance = Registry()
    }
}
