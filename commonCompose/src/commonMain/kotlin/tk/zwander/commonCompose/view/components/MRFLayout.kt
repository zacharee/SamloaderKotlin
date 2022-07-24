package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.commonCompose.util.rememberIsOverScaledThreshold
import tk.zwander.samloaderkotlin.strings

/**
 * A common container for the model, region, and firmware text inputs used in [DownloadView] and [DecryptView]
 * @param model the view model.
 * @param canChangeOption whether the model and region fields should be editable.
 * @param canChangeFirmware whether the firmware field should be editable.
 * @param showFirmware whether to show the firmware field.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmware: Boolean, showFirmware: Boolean = true) {
    val (m, r, f) = remember { FocusRequester.createRefs() }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(m)
                    .focusProperties {
                        next = r
                    },
                label = { Text(strings.modelHint()) },
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
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(r)
                    .focusProperties {
                        previous = m
                        next = if (showFirmware) f else FocusRequester.Default
                    },
                label = { Text(strings.regionHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true
            )
        }

        val constraint = rememberIsOverScaledThreshold(constraints.maxWidth)

        AnimatedVisibility(
            visible = constraint,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
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
        }

        AnimatedVisibility(
            visible = !constraint,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
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
            label = { Text(strings.firmwareHint()) },
            modifier = Modifier.fillMaxWidth()
                .focusRequester(f)
                .focusProperties {
                    previous = r
                },
            readOnly = !canChangeFirmware,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
            singleLine = true
        )
    }
}
