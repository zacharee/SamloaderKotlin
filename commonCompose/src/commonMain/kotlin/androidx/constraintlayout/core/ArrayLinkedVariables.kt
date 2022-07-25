/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.constraintlayout.coreimport

import androidx.constraintlayout.core.*
import androidx.constraintlayout.core.ArrayRow.ArrayRowVariables

androidx.constraintlayout.core.dsl.OnSwipe.Drag

/**
 * Store a set of variables and their values in an array-based linked list.
 *
 * The general idea is that we want to store a list of variables that need to be ordered,
 * space efficient, and relatively fast to maintain (add/remove).
 *
 * ArrayBackedVariables implements a sparse array, so is rather space efficient, but maintaining
 * the array sorted is costly,
 * as we spend quite a bit of time recopying parts of the array on element deletion.
 *
 * LinkedVariables implements a standard linked list structure,
 * and is able to be faster than ArrayBackedVariables
 * even though it's more costly to set up (pool of objects...),
 * as the elements removal and maintenance of the structure is a lot more efficient.
 *
 * This ArrayLinkedVariables class takes inspiration from both of the above,
 * and implement a linked list stored in several arrays.
 * This allows us to be a lot more efficient in terms of setup (no need to deal with pool
 * of objects...), resetting the structure, and insertion/deletion of elements.
 */
class ArrayLinkedVariables internal constructor(// our owner
    private val mRow: ArrayRow, // pointer to the system-wide cache, allowing access to SolverVariables
    protected val mCache: Cache
) : ArrayRowVariables {
    // private static final boolean FULL_NEW_CHECK = false   full validation (debug purposes)
    var mCurrentSize = 0 // current size, accessed by ArrayRow and LinearSystem
    private var mRowSize = 8 // default array size
    private var mCandidate: SolverVariable? = null

    // mArrayIndices point to indexes in mCache.mIndexedVariables (i.e., the SolverVariables)
    private var mArrayIndices = IntArray(mRowSize)

    // mArrayNextIndices point to indexes in mArrayIndices
    private var mArrayNextIndices = IntArray(mRowSize)

    // mArrayValues contain the associated value from mArrayIndices
    private var mArrayValues = FloatArray(mRowSize)

    // mHead point to indexes in mArrayIndices
    var head: Int = ArrayLinkedVariables.Companion.NONE
        private set

    // mLast point to indexes in mArrayIndices
    //
    // While mDidFillOnce is not set, mLast is simply incremented
    // monotonically in order to be sure to traverse the entire array; the idea here is that
    // when we clear a linked list, we only set the counters to zero without traversing the array
    // to fill it with NONE values, which would be costly.
    // But if we do not fill the array with NONE values, we cannot safely simply check if an entry
    // is set to NONE to know if we can use it or not, as it might contains a previous value...
    // So, when adding elements, we first ensure with this mechanism of mLast/mDidFillOnce
    // that we do traverse the array linearly,
    // avoiding for that first pass the need to check for the value of the item in mArrayIndices.
    // This does mean that removed elements will leave empty spaces,
    // but we /then/ set the removed element to NONE,
    // so that once we did that first traversal filling the array,
    // we can safely revert to linear traversal
    // finding an empty spot by checking the values of mArrayIndices
    // (i.e. finding an item containing NONE).
    private var mLast: Int = ArrayLinkedVariables.Companion.NONE

    // flag to keep trace if we did a full pass of the array or not, see above description
    private var mDidFillOnce = false
    // Example of a basic loop
    // current or previous point to mArrayIndices
    //
    // int current = mHead;
    // int counter = 0;
    // while (current != NONE && counter < currentSize) {
    //  SolverVariable currentVariable = mCache.mIndexedVariables[mArrayIndices[current]];
    //  float currentValue = mArrayValues[current];
    //  ...
    //  current = mArrayNextIndices[current]; counter++;
    // }
    /**
     * Constructor
     *
     * @param arrayRow the row owning us
     * @param cache    instances cache
     */
    init {
        if (ArrayLinkedVariables.Companion.DEBUG) {
            for (i in mArrayIndices.indices) {
                mArrayIndices[i] = ArrayLinkedVariables.Companion.NONE
            }
        }
    }

    /**
     * Insert a variable with a given value in the linked list
     *
     * @param variable the variable to add in the list
     * @param value    the value of the variable
     */
    override fun put(variable: SolverVariable, value: Float) {
        if (value == 0f) {
            remove(variable, true)
            return
        }
        // Special casing empty list...
        if (head == ArrayLinkedVariables.Companion.NONE) {
            head = 0
            mArrayValues[head] = value
            mArrayIndices[head] = variable.id
            mArrayNextIndices[head] = ArrayLinkedVariables.Companion.NONE
            variable.usageInRowCount++
            variable.addToRow(mRow)
            mCurrentSize++
            if (!mDidFillOnce) {
                // only increment mLast if we haven't done the first filling pass
                mLast++
                if (mLast >= mArrayIndices.size) {
                    mDidFillOnce = true
                    mLast = mArrayIndices.size - 1
                }
            }
            return
        }
        var current = head
        var previous: Int = ArrayLinkedVariables.Companion.NONE
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            if (mArrayIndices[current] == variable.id) {
                mArrayValues[current] = value
                return
            }
            if (mArrayIndices[current] < variable.id) {
                previous = current
            }
            current = mArrayNextIndices[current]
            counter++
        }

        // Not found, we need to insert

        // First, let's find an available spot
        var availableIndice = mLast + 1 // start from the previous spot
        if (mDidFillOnce) {
            // ... but if we traversed the array once, check the last index, which might have been
            // set by an element removed
            availableIndice = if (mArrayIndices[mLast] == ArrayLinkedVariables.Companion.NONE) {
                mLast
            } else {
                mArrayIndices.size
            }
        }
        if (availableIndice >= mArrayIndices.size) {
            if (mCurrentSize < mArrayIndices.size) {
                // find an available spot
                for (i in mArrayIndices.indices) {
                    if (mArrayIndices[i] == ArrayLinkedVariables.Companion.NONE) {
                        availableIndice = i
                        break
                    }
                }
            }
        }
        // ... make sure to grow the array as needed
        if (availableIndice >= mArrayIndices.size) {
            availableIndice = mArrayIndices.size
            mRowSize *= 2
            mDidFillOnce = false
            mLast = availableIndice - 1
            mArrayValues = java.util.Arrays.copyOf(mArrayValues, mRowSize)
            mArrayIndices = java.util.Arrays.copyOf(mArrayIndices, mRowSize)
            mArrayNextIndices = java.util.Arrays.copyOf(mArrayNextIndices, mRowSize)
        }

        // Finally, let's insert the element
        mArrayIndices[availableIndice] = variable.id
        mArrayValues[availableIndice] = value
        if (previous != ArrayLinkedVariables.Companion.NONE) {
            mArrayNextIndices[availableIndice] = mArrayNextIndices[previous]
            mArrayNextIndices[previous] = availableIndice
        } else {
            mArrayNextIndices[availableIndice] = head
            head = availableIndice
        }
        variable.usageInRowCount++
        variable.addToRow(mRow)
        mCurrentSize++
        if (!mDidFillOnce) {
            // only increment mLast if we haven't done the first filling pass
            mLast++
        }
        if (mCurrentSize >= mArrayIndices.size) {
            mDidFillOnce = true
        }
        if (mLast >= mArrayIndices.size) {
            mDidFillOnce = true
            mLast = mArrayIndices.size - 1
        }
    }

    /**
     * Add value to an existing variable
     *
     * The code is broadly identical to the put() method, only differing
     * in in-line deletion, and of course doing an add rather than a put
     *
     * @param variable the variable we want to add
     * @param value    its value
     */
    override fun add(variable: SolverVariable, value: Float, removeFromDefinition: Boolean) {
        if (value > -ArrayLinkedVariables.Companion.sEpsilon && value < ArrayLinkedVariables.Companion.sEpsilon) {
            return
        }
        // Special casing empty list...
        if (head == ArrayLinkedVariables.Companion.NONE) {
            head = 0
            mArrayValues[head] = value
            mArrayIndices[head] = variable.id
            mArrayNextIndices[head] = ArrayLinkedVariables.Companion.NONE
            variable.usageInRowCount++
            variable.addToRow(mRow)
            mCurrentSize++
            if (!mDidFillOnce) {
                // only increment mLast if we haven't done the first filling pass
                mLast++
                if (mLast >= mArrayIndices.size) {
                    mDidFillOnce = true
                    mLast = mArrayIndices.size - 1
                }
            }
            return
        }
        var current = head
        var previous: Int = ArrayLinkedVariables.Companion.NONE
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            val idx = mArrayIndices[current]
            if (idx == variable.id) {
                var v = mArrayValues[current] + value
                if (v > -ArrayLinkedVariables.Companion.sEpsilon && v < ArrayLinkedVariables.Companion.sEpsilon) {
                    v = 0f
                }
                mArrayValues[current] = v
                // Possibly delete immediately
                if (v == 0f) {
                    if (current == head) {
                        head = mArrayNextIndices[current]
                    } else {
                        mArrayNextIndices[previous] = mArrayNextIndices[current]
                    }
                    if (removeFromDefinition) {
                        variable.removeFromRow(mRow)
                    }
                    if (mDidFillOnce) {
                        // If we did a full pass already, remember that spot
                        mLast = current
                    }
                    variable.usageInRowCount--
                    mCurrentSize--
                }
                return
            }
            if (mArrayIndices[current] < variable.id) {
                previous = current
            }
            current = mArrayNextIndices[current]
            counter++
        }

        // Not found, we need to insert

        // First, let's find an available spot
        var availableIndice = mLast + 1 // start from the previous spot
        if (mDidFillOnce) {
            // ... but if we traversed the array once, check the last index, which might have been
            // set by an element removed
            availableIndice = if (mArrayIndices[mLast] == ArrayLinkedVariables.Companion.NONE) {
                mLast
            } else {
                mArrayIndices.size
            }
        }
        if (availableIndice >= mArrayIndices.size) {
            if (mCurrentSize < mArrayIndices.size) {
                // find an available spot
                for (i in mArrayIndices.indices) {
                    if (mArrayIndices[i] == ArrayLinkedVariables.Companion.NONE) {
                        availableIndice = i
                        break
                    }
                }
            }
        }
        // ... make sure to grow the array as needed
        if (availableIndice >= mArrayIndices.size) {
            availableIndice = mArrayIndices.size
            mRowSize *= 2
            mDidFillOnce = false
            mLast = availableIndice - 1
            mArrayValues = java.util.Arrays.copyOf(mArrayValues, mRowSize)
            mArrayIndices = java.util.Arrays.copyOf(mArrayIndices, mRowSize)
            mArrayNextIndices = java.util.Arrays.copyOf(mArrayNextIndices, mRowSize)
        }

        // Finally, let's insert the element
        mArrayIndices[availableIndice] = variable.id
        mArrayValues[availableIndice] = value
        if (previous != ArrayLinkedVariables.Companion.NONE) {
            mArrayNextIndices[availableIndice] = mArrayNextIndices[previous]
            mArrayNextIndices[previous] = availableIndice
        } else {
            mArrayNextIndices[availableIndice] = head
            head = availableIndice
        }
        variable.usageInRowCount++
        variable.addToRow(mRow)
        mCurrentSize++
        if (!mDidFillOnce) {
            // only increment mLast if we haven't done the first filling pass
            mLast++
        }
        if (mLast >= mArrayIndices.size) {
            mDidFillOnce = true
            mLast = mArrayIndices.size - 1
        }
    }

    /**
     * Update the current list with a new definition
     *
     * @param definition the row containing the definition
     */
    override fun use(definition: ArrayRow, removeFromDefinition: Boolean): Float {
        val value = get(definition.mVariable)
        remove(definition.mVariable, removeFromDefinition)
        val definitionVariables: ArrayRowVariables = definition.variables
        val definitionSize = definitionVariables.currentSize
        for (i in 0 until definitionSize) {
            val definitionVariable = definitionVariables.getVariable(i)
            val definitionValue = definitionVariables[definitionVariable]
            add(definitionVariable, definitionValue * value, removeFromDefinition)
        }
        return value
    }

    /**
     * Remove a variable from the list
     *
     * @param variable the variable we want to remove
     * @return the value of the removed variable
     */
    override fun remove(variable: SolverVariable, removeFromDefinition: Boolean): Float {
        if (mCandidate === variable) {
            mCandidate = null
        }
        if (head == ArrayLinkedVariables.Companion.NONE) {
            return 0
        }
        var current = head
        var previous: Int = ArrayLinkedVariables.Companion.NONE
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            val idx = mArrayIndices[current]
            if (idx == variable.id) {
                if (current == head) {
                    head = mArrayNextIndices[current]
                } else {
                    mArrayNextIndices[previous] = mArrayNextIndices[current]
                }
                if (removeFromDefinition) {
                    variable.removeFromRow(mRow)
                }
                variable.usageInRowCount--
                mCurrentSize--
                mArrayIndices[current] = ArrayLinkedVariables.Companion.NONE
                if (mDidFillOnce) {
                    // If we did a full pass already, remember that spot
                    mLast = current
                }
                return mArrayValues[current]
            }
            previous = current
            current = mArrayNextIndices[current]
            counter++
        }
        return 0
    }

    /**
     * Clear the list of variables
     */
    override fun clear() {
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            val variable = mCache.mIndexedVariables[mArrayIndices[current]]
            variable?.removeFromRow(mRow)
            current = mArrayNextIndices[current]
            counter++
        }
        head = ArrayLinkedVariables.Companion.NONE
        mLast = ArrayLinkedVariables.Companion.NONE
        mDidFillOnce = false
        mCurrentSize = 0
    }

    /**
     * Returns true if the variable is contained in the list
     *
     * @param variable the variable we are looking for
     * @return return true if we found the variable
     */
    override fun contains(variable: SolverVariable): Boolean {
        if (head == ArrayLinkedVariables.Companion.NONE) {
            return false
        }
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            if (mArrayIndices[current] == variable.id) {
                return true
            }
            current = mArrayNextIndices[current]
            counter++
        }
        return false
    }

    override fun indexOf(variable: SolverVariable): Int {
        if (head == ArrayLinkedVariables.Companion.NONE) {
            return -1
        }
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            if (mArrayIndices[current] == variable.id) {
                return current
            }
            current = mArrayNextIndices[current]
            counter++
        }
        return -1
    }

    /**
     * Returns true if at least one of the variable is positive
     *
     * @return true if at least one of the variable is positive
     */
    fun hasAtLeastOnePositiveVariable(): Boolean {
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            if (mArrayValues[current] > 0) {
                return true
            }
            current = mArrayNextIndices[current]
            counter++
        }
        return false
    }

    /**
     * Invert the values of all the variables in the list
     */
    override fun invert() {
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            mArrayValues[current] *= -1f
            current = mArrayNextIndices[current]
            counter++
        }
    }

    /**
     * Divide the values of all the variables in the list
     * by the given amount
     *
     * @param amount amount to divide by
     */
    override fun divideByAmount(amount: Float) {
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            mArrayValues[current] /= amount
            current = mArrayNextIndices[current]
            counter++
        }
    }

    override fun getCurrentSize(): Int {
        return mCurrentSize
    }

    /**
     * get Id in mCache.mIndexedVariables given the index
     */
    fun getId(index: Int): Int {
        return mArrayIndices[index]
    }

    /**
     * get value in mArrayValues given the index
     */
    fun getValue(index: Int): Float {
        return mArrayValues[index]
    }

    /**
     * Get the next index in mArrayIndices given the current one
     */
    fun getNextIndice(index: Int): Int {
        return mArrayNextIndices[index]
    }// We can return the first negative candidate as in ArrayLinkedVariables
    // they are already sorted by id
// if no candidate is known, let's figure it out
    /**
     * TODO: check if still needed
     * Return a pivot candidate
     *
     * @return return a variable we can pivot on
     */
    val pivotCandidate: SolverVariable?
        get() {
            if (mCandidate == null) {
                // if no candidate is known, let's figure it out
                var current = head
                var counter = 0
                var pivot: SolverVariable? = null
                while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
                    if (mArrayValues[current] < 0) {
                        // We can return the first negative candidate as in ArrayLinkedVariables
                        // they are already sorted by id
                        val v = mCache.mIndexedVariables[mArrayIndices[current]]
                        if (pivot == null || pivot.strength < v.strength) {
                            pivot = v
                        }
                    }
                    current = mArrayNextIndices[current]
                    counter++
                }
                return pivot
            }
            return mCandidate
        }

    /**
     * Return a variable from its position in the linked list
     *
     * @param index the index of the variable we want to return
     * @return the variable found, or null
     */
    override fun getVariable(index: Int): SolverVariable? {
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            if (counter == index) {
                return mCache.mIndexedVariables[mArrayIndices[current]]
            }
            current = mArrayNextIndices[current]
            counter++
        }
        return null
    }

    /**
     * Return the value of a variable from its position in the linked list
     *
     * @param index the index of the variable we want to look up
     * @return the value of the found variable, or 0 if not found
     */
    override fun getVariableValue(index: Int): Float {
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            if (counter == index) {
                return mArrayValues[current]
            }
            current = mArrayNextIndices[current]
            counter++
        }
        return 0
    }

    /**
     * Return the value of a variable, 0 if not found
     *
     * @param v the variable we are looking up
     * @return the value of the found variable, or 0 if not found
     */
    override fun get(v: SolverVariable): Float {
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            if (mArrayIndices[current] == v.id) {
                return mArrayValues[current]
            }
            current = mArrayNextIndices[current]
            counter++
        }
        return 0
    }

    /**
     * Show size in bytes
     *
     * @return size in bytes
     */
    override fun sizeInBytes(): Int {
        var size = 0
        size += 3 * (mArrayIndices.size * 4)
        size += 9 * 4
        return size
    }

    /**
     * print out the variables and their values
     */
    override fun display() {
        val count = mCurrentSize
        print("{ ")
        for (i in 0 until count) {
            val v = getVariable(i) ?: continue
            print(v.toString() + " = " + getVariableValue(i) + " ")
        }
        println(" }")
    }

    /**
     * Returns a string representation of the list
     *
     * @return a string containing a representation of the list
     */
    override fun toString(): String {
        var result = ""
        var current = head
        var counter = 0
        while (current != ArrayLinkedVariables.Companion.NONE && counter < mCurrentSize) {
            result += " -> "
            result += mArrayValues[current].toString() + " : "
            result += mCache.mIndexedVariables[mArrayIndices[current]]
            current = mArrayNextIndices[current]
            counter++
        }
        return result
    }

    companion object {
        private const val DEBUG = false
        const val NONE = -1
        private const val sEpsilon = 0.001f
    }
}
