package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmware: Boolean, showFirmware: Boolean = true) {
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
                singleLine = true
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
}
