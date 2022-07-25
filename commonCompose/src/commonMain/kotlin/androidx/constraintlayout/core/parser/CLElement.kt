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

open class CLElement(private val mContent: CharArray?) {
    /**
     * The character index this element was started on
     */
    var start: Long = -1
    protected var mEnd = Long.MAX_VALUE
    protected var mContainer: CLContainer? = null

    /**
     * get the line Number
     *
     * @return return the line number this element was on
     */
    var line = 0

    /**
     * @TODO: add description
     */
    fun notStarted(): Boolean {
        return start == -1L
    }
    /**
     * The character index this element was ended on
     */
    /**
     * @TODO: add description
     */
    var end: Long
        get() = mEnd
        set(end) {
            if (mEnd != Long.MAX_VALUE) {
                return
            }
            mEnd = end
            if (CLParser.Companion.sDebug) {
                println("closing " + this.hashCode() + " -> " + this)
            }
            if (mContainer != null) {
                mContainer!!.add(this)
            }
        }

    protected fun addIndent(builder: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            builder.append(' ')
        }
    }

    override fun toString(): String {
        if (start > mEnd || mEnd == Long.MAX_VALUE) {
            return this::class.toString() + " (INVALID, " + start + "-" + mEnd + ")"
        }
        var content = mContent!!.concatToString()
        content = content.substring(start.toInt(), mEnd.toInt() + 1)
        return strClass + " (" + start + " : " + mEnd + ") <<" + content + ">>"
    }

    val strClass: String
        get() {
            val myClass: String = this::class.toString()
            return myClass.substring(myClass.lastIndexOf('.') + 1)
        }
    protected val debugName: String
        protected get() = if (CLParser.Companion.sDebug) {
            strClass + " -> "
        } else ""

    /**
     * @TODO: add description
     */
    fun content(): String {
        val content = mContent!!.concatToString()
        return if (mEnd == Long.MAX_VALUE || mEnd < start) {
            content.substring(start.toInt(), start.toInt() + 1)
        } else content.substring(start.toInt(), mEnd.toInt() + 1)
    }

    val isDone: Boolean
        get() = mEnd != Long.MAX_VALUE

    fun setContainer(element: CLContainer?) {
        mContainer = element
    }

    val container: CLElement?
        get() = mContainer
    val isStarted: Boolean
        get() = start > -1

    open fun toJSON(): String {
        return ""
    }

    open fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        return ""
    }

    /**
     * @TODO: add description
     */
    open val int: Int
        get() = 0

    /**
     * @TODO: add description
     */
    open val float: Float
        get() = Float.NaN

    companion object {
        var sMaxLine = 80 // Max number of characters before the formatter indents
        var sBaseIndent = 2 // default indentation value
    }
}
