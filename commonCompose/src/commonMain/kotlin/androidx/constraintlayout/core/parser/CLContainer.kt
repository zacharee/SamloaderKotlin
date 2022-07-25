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
package androidx.constraintlayout.core.parser

open class CLContainer(content: CharArray?) : CLElement(content) {
    var mElements: ArrayList<CLElement?> = ArrayList()

    /**
     * @TODO: add description
     */
    fun add(element: CLElement) {
        mElements.add(element)
        if (CLParser.Companion.sDebug) {
            println("added element $element to $this")
        }
    }

    override fun toString(): String {
        val list: StringBuilder = StringBuilder()
        for (element in mElements) {
            if (list.length > 0) {
                list.append("; ")
            }
            list.append(element)
        }
        return super.toString() + " = <" + list + " >"
    }

    /**
     * @TODO: add description
     */
    fun size(): Int {
        return mElements.size
    }

    /**
     * @TODO: add description
     */
    fun names(): ArrayList<String> {
        val names: ArrayList<String> = ArrayList<String>()
        for (element in mElements) {
            if (element is CLKey) {
                val key = element as CLKey
                names.add(key.content())
            }
        }
        return names
    }

    /**
     * @TODO: add description
     */
    fun has(name: String): Boolean {
        for (element in mElements) {
            if (element is CLKey) {
                val key = element as CLKey
                if (key.content() == name) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * @TODO: add description
     */
    fun put(name: String, value: CLElement?) {
        for (element in mElements) {
            val key = element as CLKey
            if (key.content() == name) {
                key.set(value)
                return
            }
        }
        val key = CLKey.Companion.allocate(name, value) as CLKey
        mElements.add(key)
    }

    /**
     * @TODO: add description
     */
    fun putNumber(name: String, value: Float) {
        put(name, CLNumber(value))
    }

    /**
     * @TODO: add description
     */
    fun remove(name: String) {
        val toRemove: ArrayList<CLElement> = ArrayList<CLElement>()
        for (element in mElements) {
            val key = element as CLKey
            if (key.content() == name) {
                toRemove.add(element)
            }
        }
        for (element in toRemove) {
            mElements.remove(element)
        }
    }
    /////////////////////////////////////////////////////////////////////////
    // By name
    /////////////////////////////////////////////////////////////////////////
    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    operator fun get(name: String): CLElement? {
        for (element in mElements) {
            val key = element as CLKey
            if (key.content() == name) {
                return key.value
            }
        }
        throw CLParsingException("no element for key <$name>", this)
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getInt(name: String): Int {
        val element = get(name)
        if (element != null) {
            return element.int
        }
        throw CLParsingException(
            "no int found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getFloat(name: String): Float {
        val element = get(name)
        if (element != null) {
            return element.float
        }
        throw CLParsingException(
            "no float found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getArray(name: String): CLArray {
        val element = get(name)
        if (element is CLArray) {
            return element
        }
        throw CLParsingException(
            "no array found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getObject(name: String): CLObject {
        val element = get(name)
        if (element is CLObject) {
            return element
        }
        throw CLParsingException(
            "no object found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getString(name: String): String? {
        val element = get(name)
        if (element is CLString) {
            return element.content()
        }
        var strClass: String? = null
        if (element != null) {
            strClass = element.strClass
        }
        throw CLParsingException(
            "no string found for key <" + name + ">,"
                    + " found [" + strClass + "] : " + element, this
        )
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getBoolean(name: String): Boolean {
        val element = get(name)
        if (element is CLToken) {
            return element.boolean
        }
        throw CLParsingException(
            "no boolean found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }
    /////////////////////////////////////////////////////////////////////////
    // Optional
    /////////////////////////////////////////////////////////////////////////
    /**
     * @TODO: add description
     */
    fun getOrNull(name: String): CLElement? {
        for (element in mElements) {
            val key = element as CLKey
            if (key.content() == name) {
                return key.value
            }
        }
        return null
    }

    /**
     * @TODO: add description
     */
    fun getObjectOrNull(name: String): CLObject? {
        val element = getOrNull(name)
        return if (element is CLObject) {
            element
        } else null
    }

    /**
     * @TODO: add description
     */
    fun getArrayOrNull(name: String): CLArray? {
        val element = getOrNull(name)
        return if (element is CLArray) {
            element
        } else null
    }

    /**
     * @TODO: add description
     */
    fun getStringOrNull(name: String): String? {
        val element = getOrNull(name)
        return (element as? CLString)?.content()
    }

    /**
     * @TODO: add description
     */
    fun getFloatOrNaN(name: String): Float {
        val element = getOrNull(name)
        return (element as? CLNumber)?.float ?: Float.NaN
    }
    /////////////////////////////////////////////////////////////////////////
    // By index
    /////////////////////////////////////////////////////////////////////////
    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    operator fun get(index: Int): CLElement? {
        if (index >= 0 && index < mElements.size) {
            return mElements.get(index)
        }
        throw CLParsingException("no element at index $index", this)
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getInt(index: Int): Int {
        val element = get(index)
        if (element != null) {
            return element.int
        }
        throw CLParsingException("no int at index $index", this)
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getFloat(index: Int): Float {
        val element = get(index)
        if (element != null) {
            return element.float
        }
        throw CLParsingException("no float at index $index", this)
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getArray(index: Int): CLArray {
        val element = get(index)
        if (element is CLArray) {
            return element
        }
        throw CLParsingException("no array at index $index", this)
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getObject(index: Int): CLObject {
        val element = get(index)
        if (element is CLObject) {
            return element
        }
        throw CLParsingException("no object at index $index", this)
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getString(index: Int): String? {
        val element = get(index)
        if (element is CLString) {
            return element.content()
        }
        throw CLParsingException("no string at index $index", this)
    }

    /**
     * @TODO: add description
     */
    @Throws(CLParsingException::class)
    fun getBoolean(index: Int): Boolean {
        val element = get(index)
        if (element is CLToken) {
            return element.boolean
        }
        throw CLParsingException("no boolean at index $index", this)
    }
    /////////////////////////////////////////////////////////////////////////
    // Optional
    /////////////////////////////////////////////////////////////////////////
    /**
     * @TODO: add description
     */
    fun getOrNull(index: Int): CLElement? {
        return if (index >= 0 && index < mElements.size) {
            mElements.get(index)
        } else null
    }

    /**
     * @TODO: add description
     */
    fun getStringOrNull(index: Int): String? {
        val element = getOrNull(index)
        return (element as? CLString)?.content()
    }

    companion object {
        /**
         * @TODO: add description
         */
        fun allocate(content: CharArray?): CLElement {
            return CLContainer(content)
        }
    }
}
