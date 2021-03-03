package tk.zwander.common.view.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.soywiz.korio.async.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.model.HistoryModel
import tk.zwander.common.util.getFirmwareHistoryString
import tk.zwander.common.util.imageResource
import tk.zwander.common.view.HistoryItem
import tk.zwander.common.view.HybridButton
import tk.zwander.common.view.MRFLayout

expect object PlatformHistoryView {
    suspend fun parseHistory(body: String): List<HistoryInfo>
}

@Composable
fun HistoryView(model: HistoryModel) {
    val canCheckHistory = model.model.isNotBlank()
            && model.region.isNotBlank() && model.job == null

    Column(
        Modifier.fillMaxWidth()
    ) {
        val rowSize = remember { mutableStateOf(0.dp) }
        Row(
            modifier = Modifier.fillMaxWidth()
                .onSizeChanged { rowSize.value = it.width.dp }
        ) {
            HybridButton(
                onClick = {
                    model.historyItems = listOf()

                    model.job = model.scope.launch {
                          withContext(Dispatchers.Main) {
                              val historyString = getFirmwareHistoryString(model.model, model.region)

                              if (historyString == null) {
                                  model.endJob("Unable to retrieve firmware history. Make sure the model and region are correct.")
                              } else {
                                  try {
                                      val parsed = PlatformHistoryView.parseHistory(
                                          historyString
                                      )

                                      model.historyItems = parsed
                                      model.endJob("")
                                  } catch (e: Exception) {
                                      model.endJob("Error retrieving firmware history. Make sure the model and region are correct.")
                                  }
                              }
                          }
                      }
                },
                enabled = canCheckHistory,
                text = "Check History",
                description = "Check History",
                icon = imageResource("refresh.png"),
                parentSize = rowSize.value
            )

            if (model.job != null) {
                Spacer(Modifier.width(8.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                        .align(Alignment.CenterVertically),
                    strokeWidth = 2.dp
                )
            }

            Spacer(Modifier.weight(1f))

            HybridButton(
                onClick = {
                      model.endJob("")
                },
                enabled = model.job != null,
                text = "Cancel",
                description = "Cancel",
                icon = imageResource("cancel.png"),
                parentSize = rowSize.value
            )
        }

        Spacer(Modifier.height(16.dp))

        MRFLayout(model, model.job == null, model.job == null, false)

        Spacer(Modifier.height(16.dp))

        Box {
            Text(
                text = model.statusText
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(model.historyItems) {
                    HistoryItem(it)
                }
            }
        }
    }
}