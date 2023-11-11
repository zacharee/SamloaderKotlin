package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
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
@Composable
internal fun MRFLayout(
    model: BaseModel,
    canChangeOption: Boolean,
    canChangeFirmware: Boolean,
    showFirmware: Boolean = true,
) {
    val settings = remember { Settings() }

    var showingCscChooser by remember {
        mutableStateOf(false)
    }

    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)

    SplitComponent(
        startComponent = {
            OutlinedTextField(
                value = model.model.collectAsState().value,
                onValueChange = {
                    model.model.value = it.transformText(settings)
                    if ((model is DownloadModel && !model.manual.value)) {
                        model.fw.value = ""
                        model.osCode.value = ""
                    }
                },
                modifier = Modifier,
                label = { Text(strings.modelHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true,
            )
        },
        endComponent = {
            OutlinedTextField(
                value = model.region.collectAsState().value,
                onValueChange = {
                    model.region.value = it.transformText(settings)
                    if ((model is DownloadModel && !model.manual.value)) {
                        model.fw.value = ""
                        model.osCode.value = ""
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
            value = model.fw.collectAsState().value,
            onValueChange = { model.fw.value = it.transformText(settings) },
            label = { Text(strings.firmwareHint()) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !canChangeFirmware,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
            singleLine = true
        )
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
}

private fun String.transformText(settings: Settings): String {
    val trimmed = trim()

    return if (settings.getBoolean("allowLowercaseCharacters", false)) trimmed else trimmed.uppercase()
}
