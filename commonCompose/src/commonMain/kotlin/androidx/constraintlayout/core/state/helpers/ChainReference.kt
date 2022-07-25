/*
 * Copyright (C) 2019 The Android Open Source Project
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
package androidx.constraintlayout.core.state.helpers

import androidx.constraintlayout.core.state.*
import androidx.constraintlayout.core.state.HelperReference
import androidx.constraintlayout.core.widgets.ConstraintWidget

open class ChainReference(state: State, type: State.Helper) : HelperReference(state, type) {
    var bias = 0.5f
        protected set
    protected var mMapWeights: HashMap<String, Float>? = null
    protected var mMapPreMargin: HashMap<String, Float>? = null
    protected var mMapPostMargin: HashMap<String, Float>? = null
    protected var mStyle = State.Chain.SPREAD
    val style: State.Chain
        get() = State.Chain.SPREAD

    /**
     * @TODO: add description
     */
    fun style(style: State.Chain): ChainReference {
        mStyle = style
        return this
    }

    fun addChainElement(id: String, weight: Float, preMargin: Float, postMargin: Float) {
        super.add(id)
        if (!weight.isNaN()) {
            if (mMapWeights == null) {
                mMapWeights = HashMap()
            }
            mMapWeights!![id] = weight
        }
        if (!preMargin.isNaN()) {
            if (mMapPreMargin == null) {
                mMapPreMargin = HashMap()
            }
            mMapPreMargin!![id] = preMargin
        }
        if (!postMargin.isNaN()) {
            if (mMapPostMargin == null) {
                mMapPostMargin = HashMap()
            }
            mMapPostMargin!![id] = postMargin
        }
    }

    protected fun getWeight(id: String?): Float {
        if (mMapWeights == null) {
            return ConstraintWidget.UNKNOWN.toFloat()
        }
        return if (mMapWeights!!.containsKey(id)) {
            mMapWeights!![id]!!
        } else ConstraintWidget.UNKNOWN.toFloat()
    }

    protected fun getPostMargin(id: String?): Float {
        return if (mMapPostMargin != null && mMapPostMargin!!.containsKey(id)) {
            mMapPostMargin!![id]!!
        } else 0f
    }

    protected fun getPreMargin(id: String?): Float {
        return if (mMapPreMargin != null && mMapPreMargin!!.containsKey(id)) {
            mMapPreMargin!![id]!!
        } else 0f
    }

    /**
     * @TODO: add description
     */
    override fun bias(bias: Float): ChainReference {
        this.bias = bias
        return this
    }
}
