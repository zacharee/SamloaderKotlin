package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.StateFlow
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
    var showingCscChooser by remember {
        mutableStateOf(false)
    }
    var showingImeiHelp by remember {
        mutableStateOf(false)
    }

    val allowLowercase by BifrostSettings.Keys.allowLowercaseCharacters.collectAsMutableState()
    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)

    SplitComponent(
        startComponent = {
            OutlinedTextField(
                value = model.model.collectAsState().value,
                onValueChange = {
                    model.model.value = it.transformText(allowLowercase ?: false)
                    if ((model is DownloadModel && !model.manual.value)) {
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
                value = model.region.collectAsState().value,
                onValueChange = {
                    model.region.value = it.transformText(allowLowercase ?: false)
                    if ((model is DownloadModel && !model.manual.value)) {
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
                        value = model.fw,
                        onValueChange = { model.fw.value = it },
                        labelRes = MR.strings.firmware,
                        readOnly = !canChangeFirmware,
                    )
                )
            }

            if (showImeiSerial) {
                items.add(
                    DynamicField(
                        value = model.imeiSerial,
                        onValueChange = { model.imeiSerial.value = it },
                        labelRes = MR.strings.imei_serial,
                        readOnly = !canChangeOption,
                        trailingIcon = {
                            IconButton(
                                onClick = { showingImeiHelp = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = MR.strings.chooseCsc()
                                )
                            }
                        },
                        singleLine = false,
                    )
                )
            }

            items
        }
    }

    if (firmwareImeiSerial.isNotEmpty()) {
        Spacer(Modifier.size(8.dp))

        if (firmwareImeiSerial.size == 1) {
            val first = firmwareImeiSerial.first()

            OutlinedTextField(
                value = first.value.collectAsState().value,
                onValueChange = { first.onValueChange(it.transformText(allowLowercase ?: false)) },
                label = { Text(text = first.labelRes()) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = first.readOnly,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true,
                trailingIcon = first.trailingIcon,
            )
        } else {
            val first = firmwareImeiSerial.first()
            val second = firmwareImeiSerial[1]

            SplitComponent(
                startComponent = {
                    OutlinedTextField(
                        value = first.value.collectAsState().value,
                        onValueChange = {
                            first.onValueChange(
                                it.transformText(
                                    allowLowercase ?: false
                                )
                            )
                        },
                        modifier = Modifier,
                        label = { Text(text = first.labelRes()) },
                        readOnly = first.readOnly,
                        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                        singleLine = first.singleLine,
                        trailingIcon = first.trailingIcon,
                    )
                },
                endComponent = {
                    OutlinedTextField(
                        value = second.value.collectAsState().value,
                        onValueChange = {
                            second.onValueChange(
                                it.transformText(
                                    allowLowercase ?: false
                                )
                            )
                        },
                        modifier = Modifier,
                        label = { Text(text = second.labelRes()) },
                        readOnly = second.readOnly,
                        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                        singleLine = second.singleLine,
                        trailingIcon = second.trailingIcon,
                    )
                },
                startRatio = 0.6,
                endRatio = 0.4,
                modifier = Modifier.fillMaxWidth()
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

    AlertDialogDef(
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
}

private fun String.transformText(allowLowercase: Boolean): String {
    val trimmed = trim()

    return if (allowLowercase) trimmed else trimmed.uppercase()
}

private data class DynamicField(
    val value: StateFlow<String>,
    val onValueChange: (String) -> Unit,
    val labelRes: StringResource,
    val readOnly: Boolean,
    val singleLine: Boolean = true,
    val trailingIcon: (@Composable () -> Unit)? = null,
)
