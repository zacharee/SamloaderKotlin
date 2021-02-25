package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.BaseModel
import kotlin.math.roundToInt

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

            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "${(model.progress.first.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0} / ${(model.progress.second.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0} MB",
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = "${model.speed / 1024} KB/s",
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "${(model.progress.first.toFloat() / model.progress.second * 10000).roundToInt() / 100.0}%",
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        Text(
            text = model.statusText
        )
    }
}