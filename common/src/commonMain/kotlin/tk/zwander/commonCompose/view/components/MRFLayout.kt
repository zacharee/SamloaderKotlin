package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.flow.compose.collectAsMutableState
import dev.icerock.moko.resources.StringResource
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.samloaderkotlin.resources.MR

/**
 * A common container for the model, region, and firmware text inputs used in [DownloadView] and [DecryptView]
 * @param model the view model.
 * @param canChangeOption whether the model and region fields should be editable.
 * @param canChangeFirmware whether the firmware field should be editable.
 * @param showFirmware whether to show the firmware field.
 */
@Composable
internal fun MRFLayout(
    model: BaseModel,
    canChangeOption: Boolean,
    canChangeFirmware: Boolean,
    showFirmware: Boolean = true,
    showImeiSerial: Boolean = false,
) {
    var modelState by model.model.collectAsMutableState()
    var regionState by model.region.collectAsMutableState()
    var firmwareState by model.fw.collectAsMutableState()
    var imeiState by model.imeiSerial.collectAsMutableState()

    var showingCscChooser by remember {
        mutableStateOf(false)
    }
    var showingImeiHelp by remember {
        mutableStateOf(false)
    }
    var showingImeiEditor by remember {
        mutableStateOf(false)
    }

    val allowLowercase by BifrostSettings.Keys.allowLowercaseCharacters.collectAsMutableState()
    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)

    SplitComponent(
        startComponent = {
            OutlinedTextField(
                value = modelState ?: "",
                onValueChange = {
                    modelState = it.transformText(allowLowercase ?: false)
                    if ((model is DownloadModel && model.manual.value == false)) {
                        model.fw.value = ""
                        model.osCode.value = ""
                    }
                },
                modifier = Modifier,
                label = { Text(MR.strings.modelHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true,
            )
        },
        endComponent = {
            OutlinedTextField(
                value = regionState ?: "",
                onValueChange = {
                    regionState = it.transformText(allowLowercase ?: false)
                    if ((model is DownloadModel && model.manual.value == false)) {
                        model.fw.value = ""
                        model.osCode.value = ""
                    }
                },
                modifier = Modifier,
                label = { Text(MR.strings.regionHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { showingCscChooser = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = MR.strings.chooseCsc()
                        )
                    }
                },
            )
        },
        startRatio = 0.6,
        endRatio = 0.4,
        modifier = Modifier.fillMaxWidth()
    )

    val firmwareImeiSerial by remember {
        derivedStateOf {
            val items = mutableListOf<DynamicField>()

            if (showFirmware) {
                items.add(
                    DynamicField(
                        value = firmwareState ?: "",
                        onValueChange = { firmwareState = it },
                        labelRes = MR.strings.firmware,
                        readOnly = !canChangeFirmware,
                        transform = {
                            it.transformText(
                                allowLowercase ?: false
                            )
                        },
                    ),
                )
            }

            if (showImeiSerial) {
                items.add(
                    DynamicField(
                        value = imeiState?.replace("\n", ";") ?: "",
                        onValueChange = { imeiState = it.replace(";", "\n") },
                        labelRes = MR.strings.imei_serial,
                        readOnly = !canChangeOption,
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = { showingImeiEditor = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = MR.strings.edit(),
                                    )
                                }

                                IconButton(
                                    onClick = { showingImeiHelp = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = MR.strings.help()
                                    )
                                }
                            }
                        },
                    ),
                )
            }

            items
        }
    }

    if (firmwareImeiSerial.isNotEmpty()) {
        Spacer(Modifier.size(8.dp))

        if (firmwareImeiSerial.size == 1) {
            val first = firmwareImeiSerial.first()

            first.render()
        } else {
            val first = firmwareImeiSerial.first()
            val second = firmwareImeiSerial[1]

            SplitComponent(
                startComponent = {
                    first.render()
                },
                endComponent = {
                    second.render()
                },
                startRatio = 0.6,
                endRatio = 0.4,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    CSCChooserDialog(
        showing = showingCscChooser,
        onDismissRequest = { showingCscChooser = false },
        onCscSelected = {
            if (!hasRunningJobs) {
                model.region.value = it
            }
        }
    )

    InWindowAlertDialog(
        showing = showingImeiHelp,
        onDismissRequest = { showingImeiHelp = false },
        title = { Text(text = MR.strings.imei_serial()) },
        text = { Text(text = MR.strings.imei_serial_help()) },
        buttons = {
            TextButton(onClick = { showingImeiHelp = false }) {
                Text(text = MR.strings.ok())
            }
        },
    )

    InWindowAlertDialog(
        showing = showingImeiEditor,
        onDismissRequest = { showingImeiEditor = false },
        title = { Text(text = MR.strings.edit()) },
        text = {
            TextField(
                value = imeiState ?: "",
                onValueChange = { imeiState = it },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        buttons = {
            TextButton(onClick = { showingImeiEditor = false }) {
                Text(text = MR.strings.close())
            }
        },
    )
}

private fun String.transformText(allowLowercase: Boolean): String {
    val trimmed = trim()

    return if (allowLowercase) trimmed else trimmed.uppercase()
}

private data class DynamicField(
    val value: String,
    val onValueChange: (String) -> Unit,
    val labelRes: StringResource,
    val readOnly: Boolean,
    val transform: (String) -> String = { it },
    val singleLine: Boolean = true,
    val maxLines: Int = Int.MAX_VALUE,
    val trailingIcon: (@Composable () -> Unit)? = null,
    val leadingIcon: (@Composable () -> Unit)? = null,
) {
    @Composable
    fun render(modifier: Modifier = Modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(transform(it))
            },
            modifier = modifier,
            label = { Text(text = labelRes()) },
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            maxLines = maxLines,
            leadingIcon = leadingIcon,
        )
    }
}
