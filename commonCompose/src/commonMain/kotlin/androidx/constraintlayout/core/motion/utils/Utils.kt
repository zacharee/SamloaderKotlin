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
package androidx.constraintlayout.core.motion.utils

import kotlin.math.pow

class Utils {
    /**
     * @TODO: add description
     */
    fun getInterpolatedColor(value: FloatArray): Int {
        val r = clamp(
            (value[0].toDouble().pow(1.0 / 2.2).toFloat() * 255.0f).toInt()
        )
        val g = clamp(
            (value[1].toDouble().pow(1.0 / 2.2).toFloat() * 255.0f).toInt()
        )
        val b = clamp(
            (value[2].toDouble().pow(1.0 / 2.2).toFloat() * 255.0f).toInt()
        )
        val a =
            clamp((value[3] * 255.0f).toInt())
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    interface DebugHandle {
        /**
         * @TODO: add description
         */
        fun message(str: String?)
    }

    companion object {
        /**
         * @TODO: add description
         */
        fun log(tag: String, value: String) {
            println("$tag : $value")
        }

        /**
         * @TODO: add description
         */
        fun loge(tag: String, value: String) {
            println("$tag : $value")
        }

        /**
         * @TODO: add description
         */
        fun socketSend(str: String) {
//            try {
//                val socket: Socket = Socket("127.0.0.1", 5327)
//                val out: java.io.OutputStream = socket.getOutputStream()
//                out.write(str.toByteArray())
//                out.close()
//            } catch (e: java.io.IOException) {
//                e.printStackTrace()
//            }
        }

        private fun clamp(c: Int): Int {
            var c = c
            val n = 255
            c = c and (c shr 31).inv()
            c -= n
            c = c and (c shr 31)
            c += n
            return c
        }

        /**
         * @TODO: add description
         */
        fun rgbaTocColor(r: Float, g: Float, b: Float, a: Float): Int {
            val ir =
                clamp((r * 255f).toInt())
            val ig =
                clamp((g * 255f).toInt())
            val ib =
                clamp((b * 255f).toInt())
            val ia =
                clamp((a * 255f).toInt())
            return ia shl 24 or (ir shl 16) or (ig shl 8) or ib
        }

        var sOurHandle: DebugHandle? = null
        fun setDebugHandle(handle: DebugHandle?) {
            sOurHandle = handle
        }

        /**
         * @TODO: add description
         */
        fun logStack(msg: String, n: Int) {
            Throwable(msg).printStackTrace()
        }

        /**
         * @TODO: add description
         */
        fun log(str: String) {
            println(str)
            if (sOurHandle != null) {
                sOurHandle!!.message(str)
            }
        }
    }
}
