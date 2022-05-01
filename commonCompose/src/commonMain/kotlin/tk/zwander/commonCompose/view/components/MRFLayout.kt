package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import tk.zwander.common.model.BaseModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.res.Strings

/**
 * A common container for the model, region, and firmware text inputs used in [DownloadView] and [DecryptView]
 * @param model the view model.
 * @param canChangeOption whether the model and region fields should be editable.
 * @param canChangeFirmware whether the firmware field should be editable.
 * @param showFirmware whether to show the firmware field.
 */
@Composable
fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmware: Boolean, showFirmware: Boolean = true) {
    val fontScale = LocalDensity.current.fontScale
    val density = LocalDensity.current.density
    val size = remember { mutableStateOf(0.dp) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
            .onSizeChanged { size.value = it.width.dp }
    ) {
        val modelField = @Composable() {
            OutlinedTextField(
                value = model.model,
                onValueChange = {
                    model.model = it.uppercase().trim()
                    if ((model is DownloadModel && !model.manual)) {
                        model.fw = ""
                        model.osCode = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Strings.modelHint) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true
            )
        }

        val regionField = @Composable() {
            OutlinedTextField(
                value = model.region,
                onValueChange = {
                    model.region = it.uppercase().trim()
                    if ((model is DownloadModel && !model.manual)) {
                        model.fw = ""
                        model.osCode = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Strings.regionHint) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true
            )
        }

        if (size.value / (fontScale * density) >= 400.dp) {
            Row {
                Box(
                    modifier = Modifier.weight(0.6f, true)
                ) {
                    modelField()
                }

                Spacer(Modifier.size(8.dp))

                Box(
                    modifier = Modifier.weight(0.4f, true)
                ) {
                    regionField()
                }
            }
        } else {
            Column {
                modelField()

                Spacer(Modifier.size(8.dp))

                regionField()
            }
        }
    }

    if (showFirmware) {
        Spacer(Modifier.size(8.dp))

        OutlinedTextField(
            value = model.fw,
            onValueChange = { model.fw = it.uppercase().trim() },
            label = { Text(Strings.firmwareHint) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !canChangeFirmware,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
            singleLine = true
        )
    }
}
