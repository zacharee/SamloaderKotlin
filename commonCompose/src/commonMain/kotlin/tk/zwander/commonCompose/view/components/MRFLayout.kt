package tk.zwander.commonCompose.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import com.soywiz.kmem.toIntCeil
import com.soywiz.kmem.toIntFloor
import tk.zwander.commonCompose.flow.FlowRow
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
                modifier = Modifier.fillMaxWidth().layoutId("model"),
                label = { Text(strings.modelHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true
            )
        }

        val regionField = @Composable {
            OutlinedTextField(
                value = model.region,
                onValueChange = {
                    model.region = it.uppercase().trim()
                    if ((model is DownloadModel && !model.manual)) {
                        model.fw = ""
                        model.osCode = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().layoutId("region"),
                label = { Text(strings.regionHint()) },
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                singleLine = true
            )
        }

        val constraint = rememberIsOverScaledThreshold(constraints.maxWidth)
        val constraints = this.constraints

        val set = ConstraintSet {
            val modelRef = createRefFor("model")
            val regionRef = createRefFor("region")

            constrain(modelRef) {
                start.linkTo(parent.start)
                end.linkTo(if (constraint) regionRef.start else parent.end)
                top.linkTo(parent.top)
            }

            constrain(regionRef) {
                start.linkTo(if (constraint) modelRef.end else parent.start)
                end.linkTo(parent.end)
                top.linkTo(if (constraint) parent.top else modelRef.bottom)
            }
        }

        ConstraintLayout(
            constraintSet = set,
            animateChanges = true
        ) {
            modelField()

            regionField()
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
