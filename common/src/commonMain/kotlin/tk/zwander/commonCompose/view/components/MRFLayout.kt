package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import tk.zwander.common.util.BifrostSettings
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.commonCompose.model.DownloadModel
import tk.zwander.commonCompose.util.OffsetCorrectedIdentityTransformation
import tk.zwander.commonCompose.util.collectAsImmediateMutableState
import tk.zwander.samloaderkotlin.resources.MR

/**
 * A common container for the model, region, and firmware text inputs used in
 * [tk.zwander.commonCompose.view.pages.DownloadView] and [tk.zwander.commonCompose.view.pages.DecryptView]
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
    var modelState by model.model.collectAsImmediateMutableState()
    var regionState by model.region.collectAsImmediateMutableState()
    var firmwareState by model.fw.collectAsImmediateMutableState()

    var showingCscChooser by remember {
        mutableStateOf(false)
    }

    val allowLowercase by BifrostSettings.Keys.allowLowercaseCharacters.asMutableStateFlow().collectAsImmediateMutableState()
    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)

    SplitComponent(
        startComponent = {
            OutlinedTextField(
                value = modelState,
                onValueChange = {
                    modelState = it.transformText(allowLowercase)
                    if (model is DownloadModel && !model.manual.value) {
                        model.fw.value = ""
                        model.osCode.value = ""
                    }
                },
                modifier = Modifier,
                label = { Text(text = stringResource(MR.strings.modelHint)) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true,
                visualTransformation = OffsetCorrectedIdentityTransformation(modelState),
            )
        },
        endComponent = {
            OutlinedTextField(
                value = regionState,
                onValueChange = {
                    regionState = it.transformText(allowLowercase)
                    if (model is DownloadModel && !model.manual.value) {
                        model.fw.value = ""
                        model.osCode.value = ""
                    }
                },
                modifier = Modifier,
                label = { Text(text = stringResource(MR.strings.regionHint)) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { showingCscChooser = true }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(MR.strings.chooseCsc),
                        )
                    }
                },
                visualTransformation = OffsetCorrectedIdentityTransformation(regionState),
            )
        },
        startRatio = 0.6,
        endRatio = 0.4,
        modifier = Modifier.fillMaxWidth()
    )

    val firmwareImeiSerial by remember(canChangeFirmware, canChangeOption) {
        derivedStateOf {
            val items = mutableListOf<DynamicField>()

            if (showFirmware) {
                items.add(
                    DynamicField(
                        value = firmwareState,
                        onValueChange = { firmwareState = it },
                        labelRes = MR.strings.firmware,
                        readOnly = !canChangeFirmware,
                        transform = {
                            it.transformText(allowLowercase)
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

            first.Render(modifier = Modifier.fillMaxWidth())
        } else {
            val first = firmwareImeiSerial.first()
            val second = firmwareImeiSerial[1]

            SplitComponent(
                startComponent = {
                    first.Render()
                },
                endComponent = {
                    second.Render()
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
        },
    )
}

private fun String.transformText(allowLowercase: Boolean): String {
    val trimmed = trim().replace(" ", "")

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
    fun Render(modifier: Modifier = Modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(transform(it))
            },
            modifier = modifier,
            label = { Text(text = stringResource(labelRes)) },
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            maxLines = maxLines,
            leadingIcon = leadingIcon,
            visualTransformation = OffsetCorrectedIdentityTransformation(value),
        )
    }
}
