package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
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
@OptIn(ExperimentalMotionApi::class)
@Composable
fun MRFLayout(model: BaseModel, canChangeOption: Boolean, canChangeFirmware: Boolean, showFirmware: Boolean = true) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val constraint = rememberIsOverScaledThreshold(constraints.maxWidth)
        val constraints = this.constraints

        val progress by animateFloatAsState(if (constraint) 0f else 1f)

        val wideSet = ConstraintSet {
            val modelRef = createRefFor("model")
            val regionRef = createRefFor("region")

//            val horizontalRef = createHorizontalChain(modelRef, regionRef)
//
//            constrain(horizontalRef) {
//                start.linkTo(parent.start)
//                end.linkTo(parent.end)
//            }

            constrain(modelRef) {
                start.linkTo(parent.start)
                end.linkTo(regionRef.start)
                top.linkTo(parent.top)
                this.horizontalChainWeight = 0.6f
                width = Dimension.fillToConstraints
            }

            constrain(regionRef) {
                start.linkTo(modelRef.end)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                this.horizontalChainWeight = 0.4f
                width = Dimension.fillToConstraints
            }
        }

        val narrowSet = ConstraintSet {
            val modelRef = createRefFor("model")
            val regionRef = createRefFor("region")

            constrain(modelRef) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(regionRef.top)
                width = Dimension.fillToConstraints
            }

            constrain(regionRef) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(modelRef.bottom)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            }
        }

        MotionLayout(
            start = wideSet,
            end = narrowSet,
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            transition = Transition("""
                {
                    default: {
                        from: 'start',
                        to: 'end',
                        pathMotionArc: 'startHorizontal'
                    }
                }
            """.trimIndent())
        ) {
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

//        Column {
//            BoxWithConstraints {
//                val constraints = constraints
//
//                Row {
//                    val widthAnimator by animateIntAsState(if (!constraint) constraints.maxWidth else (constraints.maxWidth * 0.6f).toInt())
//                    val offsetAnimator by animateIntOffsetAsState(IntOffset(if (!constraint) (constraints.maxWidth * 0.4f).toInt() else 0, 0))
//                    val regionWidthAnimator by animateIntAsState(if (!constraint) 0 else (constraints.maxWidth * 0.4f).toInt())
//
//                    with (LocalDensity.current) {
//                        Box(
//                            modifier = Modifier.width(widthAnimator.toDp())
//                        ) {
//                        }
//
//                        Row(
//                            modifier = Modifier
//                                .wrapContentHeight()
//                                .width(regionWidthAnimator.toDp())
//                                .offset { offsetAnimator }
//                        ) {
//                            Spacer(Modifier.size(8.dp))
//
//                            regionField()
//                        }
//                    }
//                }
//            }
//
//            AnimatedVisibility(
//                visible = !constraint
//            ) {
//                Column {
//                    Spacer(Modifier.size(8.dp))
//
//                    regionField()
//                }
//            }
//        }
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
