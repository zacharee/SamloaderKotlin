package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.compose.alertdialog.InWindowAlertDialog
import my.nanihadesuka.compose.LazyColumnScrollbar
import tk.zwander.common.data.csc.CSCDB
import tk.zwander.commonCompose.util.OffsetCorrectedIdentityTransformation
import tk.zwander.commonCompose.util.ThemeConstants
import tk.zwander.samloaderkotlin.resources.MR

@Composable
fun CSCChooserDialog(
    modifier: Modifier = Modifier,
    showing: Boolean,
    onDismissRequest: () -> Unit,
    onCscSelected: (String) -> Unit,
) {
    InWindowAlertDialog(
        modifier = modifier,
        showing = showing,
        title = {
            Text(text = stringResource(MR.strings.chooseCsc))
        },
        text = {
            var filter by remember {
                mutableStateOf("")
            }

            var selectedColumn by remember {
                mutableStateOf(Column.CSC)
            }
            var sortState by remember(selectedColumn) {
                mutableStateOf(SelectionState.ASCENDING)
            }

            fun onColumnClick(column: Column) {
                if (column == selectedColumn) {
                    sortState = when (sortState) {
                        SelectionState.ASCENDING -> SelectionState.DESCENDING
                        else -> SelectionState.ASCENDING
                    }
                } else {
                    selectedColumn = column
                }
            }

            val items = CSCDB.rememberFilteredItems(
                query = filter,
                sortBy = when (selectedColumn) {
                    Column.CSC -> CSCDB.SortBy.Code(sortState == SelectionState.ASCENDING)
                    Column.COUNTRY -> CSCDB.SortBy.Country(sortState == SelectionState.ASCENDING)
                    Column.CARRIER -> CSCDB.SortBy.Carrier(sortState == SelectionState.ASCENDING)
                }
            )

            val columnStates by derivedStateOf {
                mapOf(
                    Column.CSC to if (selectedColumn == Column.CSC) sortState else SelectionState.NONE,
                    Column.CARRIER to if (selectedColumn == Column.CARRIER) sortState else SelectionState.NONE,
                    Column.COUNTRY to if (selectedColumn == Column.COUNTRY) sortState else SelectionState.NONE,
                )
            }

            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                label = {
                    Text(text = stringResource(MR.strings.search))
                },
                trailingIcon = if (filter.isNotBlank()) {
                    {
                        IconButton(
                            onClick = { filter = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(MR.strings.clear),
                            )
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = OffsetCorrectedIdentityTransformation(filter),
            )

            val listState = rememberLazyListState()

            LazyColumnScrollbar(
                state = listState,
                settings = ThemeConstants.ScrollBarSettings.Default,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = listState,
                ) {
                    val codeWeight = 0.2f
                    val carrierWeight = 0.4f
                    val countryWeight = 0.4f

                    stickyHeader {
                        Row(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            HeaderItem(
                                text = stringResource(MR.strings.csc),
                                modifier = Modifier.weight(codeWeight),
                                state = columnStates[Column.CSC]!!,
                                onClick = { onColumnClick(Column.CSC) }
                            )

                            HeaderItem(
                                text = stringResource(MR.strings.countries),
                                modifier = Modifier.weight(countryWeight),
                                state = columnStates[Column.COUNTRY]!!,
                                onClick = { onColumnClick(Column.COUNTRY) }
                            )

                            HeaderItem(
                                text = stringResource(MR.strings.carriers),
                                modifier = Modifier.weight(carrierWeight),
                                state = columnStates[Column.CARRIER]!!,
                                onClick = { onColumnClick(Column.CARRIER) }
                            )
                        }
                    }
                    items(items, { it.code }) {
                        OutlinedCard(
                            onClick = {
                                onCscSelected(it.code)
                                onDismissRequest()
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = it.code,
                                    modifier = Modifier.weight(codeWeight)
                                )

                                Text(
                                    text = it.countries.map { CSCDB.getCountryName(it) }
                                        .sortedBy { it.lowercase() }.joinToString(",\n"),
                                    modifier = Modifier.weight(countryWeight)
                                )

                                Text(
                                    text = it.carriers?.sortedBy { it.lowercase() }
                                        ?.joinToString(",\n") ?: "",
                                    modifier = Modifier.weight(carrierWeight)
                                )
                            }
                        }
                    }
                }
            }
        },
        buttons = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(MR.strings.cancel))
            }
        },
        onDismissRequest = onDismissRequest,
        contentsScrollable = false,
    )
}

@Composable
private fun HeaderItem(
    text: String,
    onClick: () -> Unit,
    state: SelectionState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
        )

        Icon(
            painter = when (state) {
                SelectionState.ASCENDING -> rememberVectorPainter(Icons.Default.KeyboardArrowUp)
                SelectionState.DESCENDING -> rememberVectorPainter(Icons.Default.KeyboardArrowDown)
                SelectionState.NONE -> painterResource(MR.images.unfold_more)
            },
            contentDescription = when (state) {
                SelectionState.ASCENDING -> stringResource(MR.strings.ascending)
                SelectionState.DESCENDING -> stringResource(MR.strings.descending)
                SelectionState.NONE -> stringResource(MR.strings.sort)
            },
            modifier = Modifier.alpha(if (state == SelectionState.NONE) 0.5f else 1f)
                .size(24.dp),
        )
    }
}

private enum class SelectionState {
    ASCENDING,
    DESCENDING,
    NONE,
}

private enum class Column {
    CSC,
    COUNTRY,
    CARRIER
}
