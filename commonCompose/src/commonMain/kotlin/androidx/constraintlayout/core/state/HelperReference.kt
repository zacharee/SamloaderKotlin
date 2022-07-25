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
package androidx.constraintlayout.core.stateimport

import androidx.constraintlayout.core.state.*
import androidx.constraintlayout.core.state.helpers.*
import androidx.constraintlayout.core.state.helpersimportimport.Facade
import androidx.constraintlayout.core.widgets.ConstraintWidget
import androidx.constraintlayout.core.widgets.HelperWidget

open class HelperReference(protected override val mState: State, val type: State.Helper) : ConstraintReference(
    mState
), Facade {
    var mReferences: ArrayList<Any> = ArrayList<Any>()
    open var helperWidget: HelperWidget? = null

    /**
     * @TODO: add description
     */
    fun add(vararg objects: Any): HelperReference {
        mReferences.addAll(objects.toList())
        return this
    }

    override var constraintWidget: ConstraintWidget?
        get() = helperWidget
        set(value) {
            helperWidget = value as? HelperWidget
        }

    /**
     * @TODO: add description
     */
    override fun apply() {
        // nothing
    }
}
