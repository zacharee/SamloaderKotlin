package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.zwander.common.model.BaseModel
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
        val hasProgress = model.progress.first > 0 && model.progress.second > 0

        if (hasProgress) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = (model.progress.first.toFloat() / model.progress.second),
                    modifier = Modifier.height(8.dp).weight(1f).align(Alignment.CenterVertically)
                )
            }

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.align(Alignment.Start)
            ) {
                val currentMB = (model.progress.first.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0
                val totalMB = (model.progress.second.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0

                Text(
                    text = "$currentMB / $totalMB MiB",
                )

                Spacer(Modifier.height(8.dp))

                val speedKBps = model.speed / 1024.0
                val shouldUseMB = speedKBps >= 1 * 1024
                val finalSpeed = "${((if (shouldUseMB) (speedKBps / 1024.0) else speedKBps) * 100.0).roundToInt() / 100.0}"

                Text(
                    text = "$finalSpeed ${if (shouldUseMB) "MiB/s" else "KiB/s"}",
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "${(model.progress.first.toFloat() / model.progress.second * 100 * 100.0).roundToInt() / 100.0}%",
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        Text(
            text = model.statusText
        )
    }
}