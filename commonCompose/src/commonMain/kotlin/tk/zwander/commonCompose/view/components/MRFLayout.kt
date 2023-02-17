package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import io.fluidsonic.country.Country
import io.fluidsonic.i18n.name
import tk.zwander.common.data.csc.CSCDB
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.samloaderkotlin.strings

/**
 * A common container for the model, region, and firmware text inputs used in [DownloadView] and [DecryptView]
 * @param model the view model.
 * @param canChangeOption whether the model and region fields should be editable.
 * @param canChangeFirmware whether the firmware field should be editable.
 * @param showFirmware whether to show the firmware field.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun MRFLayout(
    model: BaseModel,
    canChangeOption: Boolean,
    canChangeFirmware: Boolean,
    showFirmware: Boolean = true
) {
    var showingCscChooser by remember {
        mutableStateOf(false)
    }

    SplitComponent(
        startComponent = {
            OutlinedTextField(
                value = model.model,
                onValueChange = {
                    model.model = it.uppercase().trim()
                    if ((model is DownloadModel && !model.manual)) {
                        model.fw = ""
                        model.osCode = ""
                    }
                },
                modifier = Modifier,
                label = { Text(strings.modelHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true
            )
        },
        endComponent = {
            OutlinedTextField(
                value = model.region,
                onValueChange = {
                    model.region = it.uppercase().trim()
                    if ((model is DownloadModel && !model.manual)) {
                        model.fw = ""
                        model.osCode = ""
                    }
                },
                modifier = Modifier,
                label = { Text(strings.regionHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { showingCscChooser = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = strings.chooseCsc()
                        )
                    }
                }
            )
        },
        startRatio = 0.6,
        endRatio = 0.4,
        modifier = Modifier.fillMaxWidth()
    )

    if (showFirmware) {
        Spacer(Modifier.size(8.dp))

        OutlinedTextField(
            value = model.fw,
            onValueChange = { model.fw = it.uppercase().trim() },
            label = { Text(strings.firmwareHint()) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !canChangeFirmware,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
            singleLine = true
        )
    }

    var filter by remember {
        mutableStateOf("")
    }

    AlertDialogDef(
        showing = showingCscChooser,
        title = {
            Text(text = strings.chooseCsc())
        },
        text = {
            val items = CSCDB.rememberFilteredItems(filter)

            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                trailingIcon = if (filter.isNotBlank()) {
                    {
                        IconButton(
                            onClick = { filter = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = strings.clear(),
                            )
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.size(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val codeWeight = 0.2f
                val carrierWeight = 0.4f
                val countryWeight = 0.4f

                stickyHeader {
                    Row(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                    ) {
                        Text(
                            text = "CSC",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(codeWeight)
                        )

                        Text(
                            text = "Carriers",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(carrierWeight)
                        )

                        Text(
                            text = "Countries",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(countryWeight)
                        )
                    }
                }
                items(items, { it.code }) {
                    OutlinedCard(
                        onClick = {
                            model.region = it.code
                            showingCscChooser = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it.code,
                                modifier = Modifier.weight(codeWeight)
                            )

                            Text(
                                text = it.carriers?.joinToString("\n") ?: "",
                                modifier = Modifier.weight(carrierWeight)
                            )

                            Text(
                                text = it.countries.joinToString("\n") { CSCDB.getCountryName(it) },
                                modifier = Modifier.weight(countryWeight)
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            TextButton(
                onClick = { showingCscChooser = false }
            ) {
                Text(text = strings.cancel())
            }
        },
        onDismissRequest = { showingCscChooser = false }
    )
}
