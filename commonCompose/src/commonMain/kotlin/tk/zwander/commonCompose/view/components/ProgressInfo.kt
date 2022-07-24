package tk.zwander.commonCompose.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.soywiz.korio.lang.format
import com.soywiz.korio.util.toStringDecimal
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.commonCompose.flow.FlowMainAxisAlignment
import tk.zwander.commonCompose.flow.FlowRow
import tk.zwander.commonCompose.flow.MainAxisAlignment
import tk.zwander.commonCompose.flow.SizeMode
import tk.zwander.samloaderkotlin.strings
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * A progress indicator.
 * @param model the view model to use as a data source.
 */
@Composable
fun ProgressInfo(model: BaseModel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = model.statusText
        )

        val hasProgress = model.progress.first > 0 && model.progress.second > 0

        AnimatedVisibility(
            visible = hasProgress
        ) {
            Column {
                Spacer(Modifier.size(8.dp))

                val newProgress = (model.progress.first.toFloat() / model.progress.second)

                val animatedProgress by animateFloatAsState(
                    targetValue = ceil(newProgress * 100f) / 100f, // Get rid of some precision to reduce lag.
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.height(16.dp).weight(1f).align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(Modifier.size(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly,
                    mainAxisSize = SizeMode.Expand,
                    mainAxisSpacing = 16.dp,
                    crossAxisSpacing = 16.dp
                ) {
                    FormatText(
                        text = "%",
                        textFormat = try {
                            (model.progress.first.toFloat() / model.progress.second * 100 * 100.0).roundToInt() / 100.0
                        } catch (e: IllegalArgumentException) {
                            0.0
                        }.toStringDecimal(2),
                    )

                    val speedKBps = model.speed / 1024.0
                    val shouldUseMB = speedKBps >= 1 * 1024
                    val finalSpeed =
                        (((if (shouldUseMB) (speedKBps / 1024.0) else speedKBps) * 100.0).roundToInt() / 100.0).toStringDecimal(2)

                    FormatText(
                        text = if (shouldUseMB) strings.mibs() else strings.kibs(),
                        textFormat = finalSpeed,
                    )

                    val currentMB = ((model.progress.first.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0).toStringDecimal(2)
                    val totalMB = ((model.progress.second.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0).toStringDecimal(2)

                    FormatText(
                        text = strings.mib(),
                        textFormat = "$currentMB / $totalMB",
                    )
                }
            }
        }
    }
}
