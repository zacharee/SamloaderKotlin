package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MultiMeasureLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
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
@Composable
fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmware: Boolean, showFirmware: Boolean = true) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val density = LocalDensity.current
        val constraint = rememberIsOverScaledThreshold(constraints.maxWidth)
        val constraints = this.constraints

        val padding = remember(density) { with(density) { 4.dp.toPx() } }

        var fieldHeight by remember(density) { mutableStateOf(0) }
        val layoutHeight by animateIntAsState(if (constraint) fieldHeight else (2 * fieldHeight + 2 * padding).toInt())

        val modelFieldWidth by derivedStateOf { constraints.maxWidth * 0.6 - padding }
        val regionFieldWidth by derivedStateOf { constraints.maxWidth * 0.4 - padding }

        val transition = updateTransition(constraint)

        val regionFieldOffset by transition.animateIntOffset {
            IntOffset(
                x = if (it) (modelFieldWidth + (padding * 2)).toInt() else 0,
                y = if (it) 0 else (fieldHeight + 2 * padding).toInt()
            )
        }
        val modelFieldAnimWidth by transition.animateInt {
            if (it) modelFieldWidth.toInt() else constraints.maxWidth
        }
        val regionFieldAnimWidth by transition.animateInt {
            if (constraint) regionFieldWidth.toInt() else constraints.maxWidth
        }

        val lHeight by rememberUpdatedState(layoutHeight)
        val rOffset by rememberUpdatedState(regionFieldOffset)
        val mWidth by rememberUpdatedState(modelFieldAnimWidth)
        val rWidth by rememberUpdatedState(regionFieldAnimWidth)

        MultiMeasureLayout(
            modifier = Modifier.fillMaxWidth(),
            content = {
                OutlinedTextField(
                    value = model.model,
                    onValueChange = {
                        model.model = it.uppercase().trim()
                        if ((model is DownloadModel && !model.manual)) {
                            model.fw = ""
                            model.osCode = ""
                        }
                    },
                    modifier = Modifier.layoutId("model"),
                    label = { Text(strings.modelHint()) },
                    readOnly = !canChangeOption,
                    keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                    singleLine = true
                )

                OutlinedTextField(
                    value = model.region,
                    onValueChange = {
                        model.region = it.uppercase().trim()
                        if ((model is DownloadModel && !model.manual)) {
                            model.fw = ""
                            model.osCode = ""
                        }
                    },
                    modifier = Modifier.layoutId("region"),
                    label = { Text(strings.regionHint()) },
                    readOnly = !canChangeOption,
                    keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                    singleLine = true
                )
            }
        ) { measurables, c ->
            if (fieldHeight == 0) {
                fieldHeight = measurables[0].measure(constraints).height
            }

            layout(c.maxWidth, lHeight) {
                measurables.forEachIndexed { index, measurable ->
                    val placeable = measurable.measure(
                        Constraints(
                            maxWidth = when (index) {
                                0 -> mWidth
                                else -> rWidth
                            },
                            minWidth = when (index) {
                                0 -> mWidth
                                else -> rWidth
                            }
                        )
                    )

                    placeable.place(
                        if (index == 0) IntOffset.Zero else rOffset
                    )
                }
            }
        }
    }

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
