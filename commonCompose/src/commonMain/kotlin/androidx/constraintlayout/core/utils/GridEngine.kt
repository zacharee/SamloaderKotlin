/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.constraintlayout.core.utils

import kotlin.math.sqrt

/**
 * GridEngine class contains the main logic of the Grid Helper
 */
class GridEngine {
    /**
     * number of rows of the grid
     */
    private var mRows = 0

    /**
     * number of rows set by the XML or API
     */
    private var mRowsSet = 0

    /**
     * How many widgets need to be placed in the Grid
     */
    private var mNumWidgets = 0

    /**
     * number of columns of the grid
     */
    private var mColumns = 0

    /**
     * number of columns set by the XML or API
     */
    private var mColumnsSet = 0

    /**
     * string format of the input Spans
     */
    private var mStrSpans: String? = null

    /**
     * string format of the input Skips
     */
    private var mStrSkips: String? = null

    /**
     * orientation of the view arrangement - vertical or horizontal
     */
    private var mOrientation = 0

    /**
     * Indicates what is the next available position to place an widget
     */
    private var mNextAvailableIndex = 0

    /**
     * A boolean matrix that tracks the positions that are occupied by skips and spans
     * true: available position
     * false: non-available position
     */
    private var mPositionMatrix: Array<BooleanArray>? = null

    /**
     * A int matrix that contains the positions where a widget would constraint to at each direction
     * Each row contains 4 values that indicate the position to constraint of a widget.
     * Example row: [left, top, right, bottom]
     */
    private var mConstraintMatrix: Array<IntArray>? = null

    constructor() {}
    constructor(rows: Int, columns: Int) {
        mRowsSet = rows
        mColumnsSet = columns
        if (rows > MAX_ROWS) {
            mRowsSet = DEFAULT_SIZE
        }
        if (columns > MAX_COLUMNS) {
            mColumnsSet = DEFAULT_SIZE
        }
        updateActualRowsAndColumns()
        initVariables()
    }

    constructor(rows: Int, columns: Int, numWidgets: Int) {
        mRowsSet = rows
        mColumnsSet = columns
        mNumWidgets = numWidgets
        if (rows > MAX_ROWS) {
            mRowsSet = DEFAULT_SIZE
        }
        if (columns > MAX_COLUMNS) {
            mColumnsSet = DEFAULT_SIZE
        }
        updateActualRowsAndColumns()
        if (numWidgets > mRows * mColumns || numWidgets < 1) {
            mNumWidgets = mRows * mColumns
        }
        initVariables()
        fillConstraintMatrix(false)
    }

    /**
     * Initialize the relevant variables
     */
    private fun initVariables() {
        mPositionMatrix = Array(mRows) { BooleanArray(mColumns) }
        for (row in mPositionMatrix!!) {
            row.fill(true)
        }
        if (mNumWidgets > 0) {
            mConstraintMatrix = Array(mNumWidgets) { IntArray(4) }
            for (row in mConstraintMatrix!!) {
                row.fill(-1)
            }
        }
    }

    /**
     * Convert a 1D index to a 2D index that has index for row and index for column
     *
     * @param index index in 1D
     * @return row as its values.
     */
    private fun getRowByIndex(index: Int): Int {
        return if (mOrientation == 1) {
            index % mRows
        } else {
            index / mColumns
        }
    }

    /**
     * Convert a 1D index to a 2D index that has index for row and index for column
     *
     * @param index index in 1D
     * @return column as its values.
     */
    private fun getColByIndex(index: Int): Int {
        return if (mOrientation == 1) {
            index / mRows
        } else {
            index % mColumns
        }
    }

    /**
     * Check if the value of the spans/skips is valid
     *
     * @param str spans/skips in string format
     * @return true if it is valid else false
     */
    private fun isSpansValid(str: CharSequence): Boolean {
        return true
    }

    /**
     * parse the skips/spans in the string format into a int matrix
     * that each row has the information - [index, row_span, col_span]
     * the format of the input string is index:row_spanxcol_span.
     * index - the index of the starting position
     * row_span - the number of rows to span
     * col_span- the number of columns to span
     *
     * @param str string format of skips or spans
     * @return a int matrix that contains skip information.
     */
    private fun parseSpans(str: String): Array<IntArray>? {
        if (!isSpansValid(str)) {
            return null
        }
        val spans = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val spanMatrix = Array(spans.size) { IntArray(3) }
        var indexAndSpan: Array<String>
        var rowAndCol: Array<String>
        for (i in spans.indices) {
            indexAndSpan =
                spans[i].trim { it <= ' ' }.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            rowAndCol = indexAndSpan[1].split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            spanMatrix[i][0] = indexAndSpan[0].toInt()
            spanMatrix[i][1] = rowAndCol[0].toInt()
            spanMatrix[i][2] = rowAndCol[1].toInt()
        }
        return spanMatrix
    }

    /**
     * fill the constraintMatrix based on the input attributes
     *
     * @param isUpdate whether to update the existing grid (true) or create a new one (false)
     */
    private fun fillConstraintMatrix(isUpdate: Boolean) {
        if (isUpdate) {
            for (i in mPositionMatrix!!.indices) {
                for (j in mPositionMatrix!![0].indices) {
                    mPositionMatrix!![i][j] = true
                }
            }
            for (i in mConstraintMatrix!!.indices) {
                for (j in mConstraintMatrix!![0].indices) {
                    mConstraintMatrix!![i][j] = -1
                }
            }
        }
        mNextAvailableIndex = 0
        if (mStrSkips != null && !mStrSkips!!.trim { it <= ' ' }.isEmpty()) {
            val mSkips = parseSpans(mStrSkips!!)
            mSkips?.let { handleSkips(it) }
        }
        if (mStrSpans != null && !mStrSpans!!.trim { it <= ' ' }.isEmpty()) {
            val mSpans = parseSpans(mStrSpans!!)
            mSpans?.let { handleSpans(it) }
        }
        addAllConstraintPositions()
    }

    /**
     * Get the next available position for widget arrangement.
     * @return int[] -> [row, column]
     */
    private val nextPosition: Int
        get() {
            var position = 0
            var positionFound = false
            while (!positionFound) {
                if (mNextAvailableIndex >= mRows * mColumns) {
                    return -1
                }
                position = mNextAvailableIndex
                val row = getRowByIndex(mNextAvailableIndex)
                val col = getColByIndex(mNextAvailableIndex)
                if (mPositionMatrix!![row][col]) {
                    mPositionMatrix!![row][col] = false
                    positionFound = true
                }
                mNextAvailableIndex++
            }
            return position
        }

    /**
     * add the constraint position info of a widget based on the input params
     *
     * @param widgetId the Id of the widget
     * @param row row position to place the view
     * @param column column position to place the view
     */
    private fun addConstraintPosition(
        widgetId: Int, row: Int, column: Int,
        rowSpan: Int, columnSpan: Int
    ) {
        mConstraintMatrix!![widgetId][0] = column
        mConstraintMatrix!![widgetId][1] = row
        mConstraintMatrix!![widgetId][2] = column + columnSpan - 1
        mConstraintMatrix!![widgetId][3] = row + rowSpan - 1
    }

    /**
     * Handle the span use cases
     *
     * @param spansMatrix a int matrix that contains span information
     */
    private fun handleSpans(spansMatrix: Array<IntArray>) {
        for (i in spansMatrix.indices) {
            val row = getRowByIndex(spansMatrix[i][0])
            val col = getColByIndex(spansMatrix[i][0])
            if (!invalidatePositions(
                    row, col,
                    spansMatrix[i][1], spansMatrix[i][2]
                )
            ) {
                return
            }
            addConstraintPosition(
                i, row, col,
                spansMatrix[i][1], spansMatrix[i][2]
            )
        }
    }

    /**
     * Make positions in the grid unavailable based on the skips attr
     *
     * @param skipsMatrix a int matrix that contains skip information
     */
    private fun handleSkips(skipsMatrix: Array<IntArray>) {
        for (i in skipsMatrix.indices) {
            val row = getRowByIndex(skipsMatrix[i][0])
            val col = getColByIndex(skipsMatrix[i][0])
            if (!invalidatePositions(
                    row, col,
                    skipsMatrix[i][1], skipsMatrix[i][2]
                )
            ) {
                return
            }
        }
    }

    /**
     * Make the specified positions in the grid unavailable.
     *
     * @param startRow the row of the staring position
     * @param startColumn the column of the staring position
     * @param rowSpan how many rows to span
     * @param columnSpan how many columns to span
     * @return true if we could properly invalidate the positions else false
     */
    private fun invalidatePositions(
        startRow: Int, startColumn: Int,
        rowSpan: Int, columnSpan: Int
    ): Boolean {
        for (i in startRow until startRow + rowSpan) {
            for (j in startColumn until startColumn + columnSpan) {
                if (i >= mPositionMatrix!!.size || j >= mPositionMatrix!![0].size || !mPositionMatrix!![i][j]) {
                    // the position is already occupied.
                    return false
                }
                mPositionMatrix!![i][j] = false
            }
        }
        return true
    }

    /**
     * Arrange the views in the constraint_referenced_ids
     */
    private fun addAllConstraintPositions() {
        var position: Int
        for (i in 0 until mNumWidgets) {

            // Already added ConstraintPosition
            if (leftOfWidget(i) != -1) {
                continue
            }
            position = nextPosition
            val row = getRowByIndex(position)
            val col = getColByIndex(position)
            if (position == -1) {
                // no more available position.
                return
            }
            addConstraintPosition(i, row, col, 1, 1)
        }
    }

    /**
     * Compute the actual rows and columns given what was set
     * if 0,0 find the most square rows and columns that fits
     * if 0,n or n,0 scale to fit
     */
    private fun updateActualRowsAndColumns() {
        if (mRowsSet == 0 || mColumnsSet == 0) {
            if (mColumnsSet > 0) {
                mColumns = mColumnsSet
                mRows = (mNumWidgets + mColumns - 1) / mColumnsSet // round up
            } else if (mRowsSet > 0) {
                mRows = mRowsSet
                mColumns = (mNumWidgets + mRowsSet - 1) / mRowsSet // round up
            } else { // as close to square as possible favoring more rows
                mRows = (1.5 + sqrt(mNumWidgets.toDouble())).toInt()
                mColumns = (mNumWidgets + mRows - 1) / mRows
            }
        } else {
            mRows = mRowsSet
            mColumns = mColumnsSet
        }
    }

    /**
     * Set up the Grid engine.
     */
    fun setup() {
        var isUpdate = true
        if (mConstraintMatrix == null || mConstraintMatrix!!.size != mNumWidgets || mPositionMatrix == null || mPositionMatrix!!.size != mRows || mPositionMatrix!![0].size != mColumns) {
            isUpdate = false
        }
        if (!isUpdate) {
            initVariables()
        }
        fillConstraintMatrix(isUpdate)
    }

    /**
     * set new spans value
     *
     * @param spans new spans value
     */
    fun setSpans(spans: CharSequence) {
        if (mStrSpans != null && mStrSpans == spans) {
            return
        }
        mStrSpans = spans.toString()
    }

    /**
     * set new skips value
     *
     * @param skips new spans value
     */
    fun setSkips(skips: String) {
        if (mStrSkips != null && mStrSkips == skips) {
            return
        }
        mStrSkips = skips
    }

    /**
     * set new orientation value
     *
     * @param orientation new orientation value
     */
    fun setOrientation(orientation: Int) {
        if (!(orientation == HORIZONTAL || orientation == VERTICAL)) {
            return
        }
        if (mOrientation == orientation) {
            return
        }
        mOrientation = orientation
    }

    /**
     * Set new NumWidgets value
     * @param num how man widgets to be arranged in Grid
     */
    fun setNumWidgets(num: Int) {
        if (num > mRows * mColumns) {
            return
        }
        mNumWidgets = num
    }

    /**
     * set new rows value
     *
     * @param rows new rows value
     */
    fun setRows(rows: Int) {
        if (rows > MAX_ROWS) {
            return
        }
        if (mRowsSet == rows) {
            return
        }
        mRowsSet = rows
        updateActualRowsAndColumns()
    }

    /**
     * set new columns value
     *
     * @param columns new rows value
     */
    fun setColumns(columns: Int) {
        if (columns > MAX_COLUMNS) {
            return
        }
        if (mColumnsSet == columns) {
            return
        }
        mColumnsSet = columns
        updateActualRowsAndColumns()
    }

    /**
     * Get the boxView for the widget i to add a constraint on the left
     *
     * @param i the widget that has the order as i in the constraint_reference_ids
     * @return the boxView to add a constraint on the left
     */
    fun leftOfWidget(i: Int): Int {
        return if (mConstraintMatrix == null || i >= mConstraintMatrix!!.size) {
            0
        } else mConstraintMatrix!![i][0]
    }

    /**
     * Get the boxView for the widget i to add a constraint on the top
     *
     * @param i the widget that has the order as i in the constraint_reference_ids
     * @return the boxView to add a constraint on the top
     */
    fun topOfWidget(i: Int): Int {
        return if (mConstraintMatrix == null || i >= mConstraintMatrix!!.size) {
            0
        } else mConstraintMatrix!![i][1]
    }

    /**
     * Get the boxView for the widget i to add a constraint on the right
     *
     * @param i the widget that has the order as i in the constraint_reference_ids
     * @return the boxView to add a constraint on the right
     */
    fun rightOfWidget(i: Int): Int {
        return if (mConstraintMatrix == null || i >= mConstraintMatrix!!.size) {
            0
        } else mConstraintMatrix!![i][2]
    }

    /**
     * Get the boxView for the widget i to add a constraint on the bottom
     *
     * @param i the widget that has the order as i in the constraint_reference_ids
     * @return the boxView to add a constraint on the bottom
     */
    fun bottomOfWidget(i: Int): Int {
        return if (mConstraintMatrix == null || i >= mConstraintMatrix!!.size) {
            0
        } else mConstraintMatrix!![i][3]
    }

    companion object {
        const val VERTICAL = 1
        const val HORIZONTAL = 0
        private const val MAX_ROWS = 50 // maximum number of rows can be specified.
        private const val MAX_COLUMNS = 50 // maximum number of columns can be specified.
        private const val DEFAULT_SIZE = 3 // default rows and columns.
    }
}
