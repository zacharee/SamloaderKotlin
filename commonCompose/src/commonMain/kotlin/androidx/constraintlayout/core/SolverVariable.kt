/*
 * Copyright (C) 2015 The Android Open Source Project
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
package androidx.constraintlayout.core

import kotlin.native.concurrent.ThreadLocal

/**
 * Represents a given variable used in the [linear expression solver][LinearSystem].
 */
class SolverVariable : Comparable<SolverVariable> {
    var inGoal = false

    /**
     * Accessor for the name
     *
     * @return the name of the variable
     */
    var name: String? = null
    var id = -1
    var mDefinitionId = -1
    var strength = 0
    var computedValue = 0f
    var isFinalValue = false
    var mStrengthVector = FloatArray(MAX_STRENGTH)
    var mGoalStrengthVector = FloatArray(MAX_STRENGTH)
    var mType: Type
    var mClientEquations = arrayOfNulls<ArrayRow>(16)
    var mClientEquationsCount = 0
    var usageInRowCount = 0
    var mIsSynonym = false
    var mSynonym = -1
    var mSynonymDelta = 0f

    /**
     * Type of variables
     */
    enum class Type {
        /**
         * The variable can take negative or positive values
         */
        UNRESTRICTED,

        /**
         * The variable is actually not a variable :) , but a constant number
         */
        CONSTANT,

        /**
         * The variable is restricted to positive values and represents a slack
         */
        SLACK,

        /**
         * The variable is restricted to positive values and represents an error
         */
        ERROR,

        /**
         * Unknown (invalid) type.
         */
        UNKNOWN
    }

    /**
     * Base constructor
     *
     * @param name the variable name
     * @param type the type of the variable
     */
    constructor(name: String?, type: Type) {
        this.name = name
        mType = type
    }

    constructor(type: Type, prefix: String?) {
        mType = type
        if (INTERNAL_DEBUG) {
            //mName = getUniqueName(type, prefix);
        }
    }

    fun clearStrengths() {
        for (i in 0 until MAX_STRENGTH) {
            mStrengthVector[i] = 0f
        }
    }

    fun strengthsToString(): String {
        var representation = "$this["
        var negative = false
        var empty = true
        for (j in mStrengthVector.indices) {
            representation += mStrengthVector[j]
            if (mStrengthVector[j] > 0) {
                negative = false
            } else if (mStrengthVector[j] < 0) {
                negative = true
            }
            if (mStrengthVector[j] != 0f) {
                empty = false
            }
            representation += if (j < mStrengthVector.size - 1) {
                ", "
            } else {
                "] "
            }
        }
        if (negative) {
            representation += " (-)"
        }
        if (empty) {
            representation += " (*)"
        }
        // representation += " {id: " + id + "}";
        return representation
    }

    var mInRows: HashSet<ArrayRow>? =
        if (VAR_USE_HASH) HashSet<ArrayRow>() else null

    /**
     * @TODO: add description
     */
    fun addToRow(row: ArrayRow) {
        if (VAR_USE_HASH) {
            mInRows?.add(row)
        } else {
            for (i in 0 until mClientEquationsCount) {
                if (mClientEquations[i] === row) {
                    return
                }
            }
            if (mClientEquationsCount >= mClientEquations.size) {
                mClientEquations = mClientEquations.copyOf(mClientEquations.size * 2)
            }
            mClientEquations[mClientEquationsCount] = row
            mClientEquationsCount++
        }
    }

    /**
     * @TODO: add description
     */
    fun removeFromRow(row: ArrayRow) {
        if (VAR_USE_HASH) {
            mInRows?.remove(row)
        } else {
            val count = mClientEquationsCount
            for (i in 0 until count) {
                if (mClientEquations[i] === row) {
                    for (j in i until count - 1) {
                        mClientEquations[j] = mClientEquations[j + 1]
                    }
                    mClientEquationsCount--
                    return
                }
            }
        }
    }

    /**
     * @TODO: add description
     */
    fun updateReferencesWithNewDefinition(system: LinearSystem?, definition: ArrayRow?) {
        if (VAR_USE_HASH) {
            for (row in mInRows!!) {
                row.updateFromRow(system, definition, false)
            }
            mInRows?.clear()
        } else {
            val count = mClientEquationsCount
            for (i in 0 until count) {
                mClientEquations[i]!!.updateFromRow(system, definition, false)
            }
            mClientEquationsCount = 0
        }
    }

    /**
     * @TODO: add description
     */
    fun setFinalValue(system: LinearSystem?, value: Float) {
        if (DO_NOT_USE && INTERNAL_DEBUG) {
            println("Set final value for $this of $value")
        }
        computedValue = value
        isFinalValue = true
        mIsSynonym = false
        mSynonym = -1
        mSynonymDelta = 0f
        val count = mClientEquationsCount
        mDefinitionId = -1
        for (i in 0 until count) {
            mClientEquations[i]!!.updateFromFinalVariable(system, this, false)
        }
        mClientEquationsCount = 0
    }

    /**
     * @TODO: add description
     */
    fun setSynonym(system: LinearSystem, synonymVariable: SolverVariable, value: Float) {
        if (INTERNAL_DEBUG) {
            println("Set synonym for $this = $synonymVariable + $value")
        }
        mIsSynonym = true
        mSynonym = synonymVariable.id
        mSynonymDelta = value
        val count = mClientEquationsCount
        mDefinitionId = -1
        for (i in 0 until count) {
            mClientEquations[i]!!.updateFromSynonymVariable(system, this, false)
        }
        mClientEquationsCount = 0
        system.displayReadableRows()
    }

    /**
     * @TODO: add description
     */
    fun reset() {
        name = null
        mType = Type.UNKNOWN
        strength = STRENGTH_NONE
        id = -1
        mDefinitionId = -1
        computedValue = 0f
        isFinalValue = false
        mIsSynonym = false
        mSynonym = -1
        mSynonymDelta = 0f
        if (VAR_USE_HASH) {
            mInRows?.clear()
        } else {
            val count = mClientEquationsCount
            for (i in 0 until count) {
                mClientEquations[i] = null
            }
            mClientEquationsCount = 0
        }
        usageInRowCount = 0
        inGoal = false
        mGoalStrengthVector.fill(0f)
    }

    /**
     * @TODO: add description
     */
    fun setType(type: Type, prefix: String?) {
        mType = type
        if (INTERNAL_DEBUG && name == null) {
            name = getUniqueName(type, prefix)
        }
    }

    override fun compareTo(v: SolverVariable): Int {
        return id - v.id
    }

    /**
     * Override the toString() method to display the variable
     */
    override fun toString(): String {
        var result = ""
        if (INTERNAL_DEBUG) {
            result += name + "(" + id + "):" + strength
            if (mIsSynonym) {
                result += ":S($mSynonym)"
            }
            if (isFinalValue) {
                result += ":F($computedValue)"
            }
        } else {
            if (name != null) {
                result += name
            } else {
                result += id
            }
        }
        return result
    }

    @ThreadLocal
    companion object {
        private val INTERNAL_DEBUG: Boolean = LinearSystem.FULL_DEBUG
        private const val VAR_USE_HASH = false
        private const val DO_NOT_USE = false
        const val STRENGTH_NONE = 0
        const val STRENGTH_LOW = 1
        const val STRENGTH_MEDIUM = 2
        const val STRENGTH_HIGH = 3
        const val STRENGTH_HIGHEST = 4
        const val STRENGTH_EQUALITY = 5
        const val STRENGTH_BARRIER = 6
        const val STRENGTH_CENTERING = 7
        const val STRENGTH_FIXED = 8
        private var sUniqueSlackId = 1
        private var sUniqueErrorId = 1
        private var sUniqueUnrestrictedId = 1
        private var sUniqueConstantId = 1
        private var sUniqueId = 1
        const val MAX_STRENGTH = 9
        fun increaseErrorId() {
            sUniqueErrorId++
        }

        private fun getUniqueName(type: Type, prefix: String?): String {
            return if (prefix != null) {
                prefix + sUniqueErrorId
            } else when (type) {
                Type.UNRESTRICTED -> "U" + ++sUniqueUnrestrictedId
                Type.CONSTANT -> "C" + ++sUniqueConstantId
                Type.SLACK -> "S" + ++sUniqueSlackId
                Type.ERROR -> {
                    "e" + ++sUniqueErrorId
                }

                Type.UNKNOWN -> "V" + ++sUniqueId
            }
            throw AssertionError(type.name)
        }
    }
}
