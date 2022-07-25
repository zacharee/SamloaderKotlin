/*
 * Copyright (C) 2020 The Android Open Source Project
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

/**
 * Schlick's bias and gain functions
 * curve for use in an easing function including quantize functions
 */
class Schlick internal constructor(configString: String) : Easing() {
    var mS: Double
    var mT: Double
    var mEps = 0.0

    init {
        // done this way for efficiency
        mStr = configString
        val start = configString.indexOf('(')
        val off1 = configString.indexOf(',', start)
        mS = configString.substring(start + 1, off1).trim { it <= ' ' }.toDouble()
        val off2 = configString.indexOf(',', off1 + 1)
        mT = configString.substring(off1 + 1, off2).trim { it <= ' ' }.toDouble()
    }

    private fun func(x: Double): Double {
        return if (x < mT) {
            mT * x / (x + mS * (mT - x))
        } else (1 - mT) * (x - 1) / (1 - x - mS * (mT - x))
    }

    private fun dfunc(x: Double): Double {
        return if (x < mT) {
            mS * mT * mT / ((mS * (mT - x) + x) * (mS * (mT - x) + x))
        } else mS * (mT - 1) * (mT - 1) / ((-mS * (mT - x) - x + 1) * (-mS * (mT - x) - x + 1))
    }

    /**
     * @TODO: add description
     */
    override fun getDiff(x: Double): Double {
        return dfunc(x)
    }

    /**
     * @TODO: add description
     */
    override fun get(x: Double): Double {
        return func(x)
    }

    companion object {
        private const val DEBUG = false
    }
}
