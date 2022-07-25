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
package androidx.constraintlayout.core.widgets.analyzer

open class DependencyNode(var mRun: WidgetRun) : Dependency {
    var updateDelegate: Dependency? = null
    var delegateToWidgetRun = false
    var readyToSolve = false

    enum class Type {
        UNKNOWN, HORIZONTAL_DIMENSION, VERTICAL_DIMENSION, LEFT, RIGHT, TOP, BOTTOM, BASELINE
    }

    var mType = Type.UNKNOWN
    var mMargin = 0
    var value = 0
    var mMarginFactor = 1
    var mMarginDependency: DimensionDependency? = null
    var resolved = false
    var mDependencies: MutableList<Dependency?> = ArrayList()
    var mTargets: MutableList<DependencyNode?> = ArrayList()
    override fun toString(): String {
        return (mRun.mWidget.debugName + ":" + mType + "("
                + (if (resolved) value else "unresolved") + ") <t="
                + mTargets.size + ":d=" + mDependencies.size + ">")
    }

    /**
     * @TODO: add description
     */
    open fun resolve(value: Int) {
        if (resolved) {
            return
        }
        resolved = true
        this.value = value
        for (node in mDependencies) {
            node!!.update(node)
        }
    }

    /**
     * @TODO: add description
     */
    override fun update(node: Dependency?) {
        for (target in mTargets) {
            if (!target!!.resolved) {
                return
            }
        }
        readyToSolve = true
        if (updateDelegate != null) {
            updateDelegate!!.update(this)
        }
        if (delegateToWidgetRun) {
            mRun.update(this)
            return
        }
        var target: DependencyNode? = null
        var numTargets = 0
        for (t in mTargets) {
            if (t is DimensionDependency) {
                continue
            }
            target = t
            numTargets++
        }
        if (target != null && numTargets == 1 && target.resolved) {
            if (mMarginDependency != null) {
                if (mMarginDependency!!.resolved) {
                    mMargin = mMarginFactor * mMarginDependency!!.value
                } else {
                    return
                }
            }
            resolve(target.value + mMargin)
        }
        if (updateDelegate != null) {
            updateDelegate!!.update(this)
        }
    }

    /**
     * @TODO: add description
     */
    fun addDependency(dependency: Dependency) {
        mDependencies.add(dependency)
        if (resolved) {
            dependency.update(dependency)
        }
    }

    /**
     * @TODO: add description
     */
    fun name(): String {
        var definition: String = mRun.mWidget.debugName ?: ""
        definition += if (mType == Type.LEFT
            || mType == Type.RIGHT
        ) {
            "_HORIZONTAL"
        } else {
            "_VERTICAL"
        }
        definition += ":" + mType.name
        return definition
    }

    /**
     * @TODO: add description
     */
    fun clear() {
        mTargets.clear()
        mDependencies.clear()
        resolved = false
        value = 0
        readyToSolve = false
        delegateToWidgetRun = false
    }
}
