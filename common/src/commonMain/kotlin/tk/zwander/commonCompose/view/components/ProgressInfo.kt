package tk.zwander.commonCompose.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import korlibs.io.util.toStringDecimal
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.flow.FlowMainAxisAlignment
import tk.zwander.commonCompose.flow.FlowRow
import tk.zwander.commonCompose.flow.SizeMode
import tk.zwander.commonCompose.model.BaseModel
import tk.zwander.samloaderkotlin.resources.MR
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * A progress indicator.
 * @param model the view model to use as a data source.
 */
@Composable
internal fun ProgressInfo(model: BaseModel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val statusText by model.statusText.collectAsState()
        val progress by model.progress.collectAsState()
        val hasRunningJobs by model.hasRunningJobs.collectAsState(false)
        val speed by model.speed.collectAsState()

        if (statusText.isNotBlank()) {
            Text(
                text = statusText
            )
        }

        val isIndeterminate = progress.first <= 0 || progress.second <= 0
        val hasProgress = hasRunningJobs || (progress.first > 0 && progress.second > 0)

        AnimatedVisibility(
            visible = hasProgress
        ) {
            Column {
                Spacer(Modifier.size(8.dp))

                if (isIndeterminate) {
                    LinearProgressIndicator(
                        modifier = Modifier.height(16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                    )
                } else {
                    val newProgress = (progress.first.toFloat() / progress.second)

                    val animatedProgress by animateFloatAsState(
                        targetValue = ceil(newProgress * 100f) / 100f, // Get rid of some precision to reduce lag.
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )

                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.height(16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                    )

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
                                (progress.first.toFloat() / progress.second * 100 * 100.0).roundToInt() / 100.0
                            } catch (e: IllegalArgumentException) {
                                0.0
                            }.toStringDecimal(2),
                        )

                        val speedKBps = speed / 1024.0
                        val shouldUseMB = speedKBps >= 1 * 1024
                        val finalSpeed =
                            (((if (shouldUseMB) (speedKBps / 1024.0) else speedKBps) * 100.0).roundToInt() / 100.0).toStringDecimal(2)

                        FormatText(
                            text = if (shouldUseMB) MR.strings.mibs() else MR.strings.kibs(),
                            textFormat = finalSpeed,
                        )

                        val currentMB = ((progress.first.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0).toStringDecimal(2)
                        val totalMB = ((progress.second.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0).toStringDecimal(2)

                        FormatText(
                            text = MR.strings.mib(),
                            textFormat = "$currentMB / $totalMB",
                        )
                    }
                }
            }
        }
    }
}
