package tk.zwander.commonCompose.model

import kotlinx.coroutines.flow.MutableStateFlow

object CSCDialogModel {
    val filter = MutableStateFlow("")
    val selectedColumn = MutableStateFlow(Column.CSC)
    val sortState = MutableStateFlow(SelectionState.ASCENDING)
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
