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
package androidx.constraintlayout.core.motion.parse

import androidx.constraintlayout.core.motion.utils.TypedBundle
import androidx.constraintlayout.core.motion.utils.TypedValues
import androidx.constraintlayout.core.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.core.parser.CLKey
import androidx.constraintlayout.core.parser.CLObject
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.parser.CLParsingException
import kotlin.jvm.JvmStatic

object KeyParser {
    private fun parse(str: String, table: Ids, dtype: DataType): TypedBundle {
        val bundle = TypedBundle()
        try {
            val parsedContent: CLObject = CLParser.parse(str)
            val n: Int = parsedContent.size()
            for (i in 0 until n) {
                val clkey = parsedContent.get(i) as CLKey
                val type: String = clkey.content()
                val value = clkey.value
                val id = table[type]
                if (id == -1) {
                    println("unknown type $type")
                    continue
                }
                when (dtype[id]) {
                    TypedValues.FLOAT_MASK -> {
                        bundle.add(id, value!!.float)
                        println("parse " + type + " FLOAT_MASK > " + value.float)
                    }

                    TypedValues.STRING_MASK -> {
                        bundle.add(id, value!!.content())
                        println("parse " + type + " STRING_MASK > " + value.content())
                    }

                    TypedValues.INT_MASK -> {
                        bundle.add(id, value!!.int)
                        println("parse " + type + " INT_MASK > " + value.int)
                    }

                    TypedValues.BOOLEAN_MASK -> bundle.add(id, parsedContent.getBoolean(i))
                }
            }
        } catch (e: CLParsingException) {
            e.printStackTrace()
        }
        return bundle
    }

    /**
     * @TODO: add description
     */
    fun parseAttributes(str: String): TypedBundle {
        return parse(
            str,
            object : Ids {
                override fun get(str: String?): Int {
                    return AttributesType.getId(str)
                }
            },
            object : DataType {
                override fun get(str: Int): Int {
                    return AttributesType.getType(str)
                }
            }
        )
    }

    /**
     * @TODO: add description
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val str = """
             {frame:22,
             target:'widget1',
             easing:'easeIn',
             curveFit:'spline',
             progress:0.3,
             alpha:0.2,
             elevation:0.7,
             rotationZ:23,
             rotationX:25.0,
             rotationY:27.0,
             pivotX:15,
             pivotY:17,
             pivotTarget:'32',
             pathRotate:23,
             scaleX:0.5,
             scaleY:0.7,
             translationX:5,
             translationY:7,
             translationZ:11,
             }
             """.trimIndent()
        parseAttributes(str)
    }

    private interface Ids {
        operator fun get(str: String?): Int
    }

    private interface DataType {
        operator fun get(str: Int): Int
    }
}
