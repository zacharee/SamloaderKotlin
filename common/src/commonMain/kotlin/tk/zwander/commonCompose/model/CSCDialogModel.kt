package tk.zwander.commonCompose.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import tk.zwander.common.data.csc.CSCDB
import tk.zwander.common.data.csc.CSCDB.findForGeneralQuery

object CSCDialogModel {
    val filter = MutableStateFlow("")
    val selectedColumn = MutableStateFlow(Column.CSC)
    val sortState = MutableStateFlow(SelectionState.ASCENDING)
    val scrollPosition = MutableStateFlow(Pair(0, 0))
    val sortBy = combine(selectedColumn, sortState) { selectedColumn, sortState ->
        when (selectedColumn) {
            Column.CSC -> CSCDB.SortBy.Code(sortState == SelectionState.ASCENDING)
            Column.COUNTRY -> CSCDB.SortBy.Country(sortState == SelectionState.ASCENDING)
            Column.CARRIER -> CSCDB.SortBy.Carrier(sortState == SelectionState.ASCENDING)
        }
    }
    val filteredItems = combine(CSCDB.items, filter, sortBy) { items, filter, sortBy ->
        findForGeneralQuery(filter, items).run {
            if (sortBy.ascending) {
                sortedBy { sortBy.sortKey(it) }
            } else {
                sortedByDescending { sortBy.sortKey(it) }
            }
        }
    }
}

enum class SelectionState {
    ASCENDING,
    DESCENDING,
    NONE,
}

enum class Column {
    CSC,
    COUNTRY,
    CARRIER
}
