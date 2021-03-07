package tk.zwander.common.view.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soywiz.korio.async.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.model.HistoryModel
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.getFirmwareHistoryString
import tk.zwander.common.util.vectorResource
import tk.zwander.common.view.HistoryItem
import tk.zwander.common.view.HybridButton
import tk.zwander.common.view.MRFLayout

expect object PlatformHistoryView {
    suspend fun parseHistory(body: String): List<HistoryInfo>
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryView(model: HistoryModel, onDownload: (model: String, region: String, fw: String) -> Unit, onDecrypt: (model: String, region: String, fw: String) -> Unit) {
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
                vectorIcon = vectorResource("refresh.xml"),
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
                vectorIcon = vectorResource("cancel.xml"),
                parentSize = rowSize.value
            )
        }

        Spacer(Modifier.height(8.dp))

        MRFLayout(model, model.job == null, model.job == null, false)

        Spacer(Modifier.height(8.dp))

        Box {
            if (model.statusText.isNotBlank()) {
                Text(
                    text = model.statusText,
                )
            }

            if (model.historyItems.isNotEmpty()) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxWidth(),
                    cells = GridCells.Adaptive(400.dp)
                ) {
                    item {
                        val sammobileSource = buildAnnotatedString {
                            pushStyle(
                                SpanStyle(
                                    color = LocalContentColor.current,
                                    fontSize = 16.sp
                                )
                            )
                            append("Source: ")
                            pushStyle(
                                SpanStyle(
                                    color = MaterialTheme.colors.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            pushStringAnnotation("SammobileLink", "https://sammobile.com")
                            append("SamMobile")
                            pop()
                        }

                        ClickableText(
                            text = sammobileSource,
                            modifier = Modifier.padding(start = 4.dp),
                            onClick = {
                                sammobileSource.getStringAnnotations("SammobileLink", it, it)
                                    .firstOrNull()?.let { item ->
                                        UrlHandler.launchUrl(item.item)
                                    }
                            }
                        )
                    }
                    items(model.historyItems) { historyInfo ->
                        HistoryItem(
                            historyInfo,
                            { onDownload(model.model, model.region, it) },
                            { onDecrypt(model.model, model.region, it) }
                        )
                    }
                }
            }
        }
    }
}