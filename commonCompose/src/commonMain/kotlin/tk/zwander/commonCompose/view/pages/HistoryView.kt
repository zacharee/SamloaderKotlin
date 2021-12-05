package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
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
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.getFirmwareHistoryString
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.commonCompose.view.components.HistoryItem
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.StaggeredVerticalGrid

/**
 * Delegate HTML parsing to the platform until there's an MPP library.
 */
expect object PlatformHistoryView {
    suspend fun parseHistory(body: String): List<HistoryInfo>
}

/**
 * The History View.
 * @param model the History model.
 * @param onDownload a callback for when the user hits the "Download" button on an item.
 * @param onDecrypt a callback for when the user hits the "Decrypt" button on an item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryView(model: HistoryModel, onDownload: (model: String, region: String, fw: String) -> Unit, onDecrypt: (model: String, region: String, fw: String) -> Unit) {
    val canCheckHistory = model.model.isNotBlank()
            && model.region.isNotBlank() && model.job == null

    val odinRomSource = buildAnnotatedString {
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
        pushStringAnnotation("OdinRomLink", "https://odinrom.com")
        append("OdinRom")
        pop()
    }

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

                                      model.changelogs = try {
                                          ChangelogHandler.getChangelogs(model.model, model.region)
                                      } catch (e: Exception) {
                                          println("Error retrieving changelogs")
                                          e.printStackTrace()
                                          null
                                      }
                                      model.historyItems = parsed
                                      model.endJob("")
                                  } catch (e: Exception) {
                                      e.printStackTrace()
                                      model.endJob("Error retrieving firmware history. Make sure the model and region are correct.\nError: ${e.message}")
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

        ClickableText(
            text = odinRomSource,
            modifier = Modifier.padding(start = 4.dp),
            onClick = {
                odinRomSource.getStringAnnotations("OdinRomLink", it, it)
                    .firstOrNull()?.let { item ->
                        UrlHandler.launchUrl(item.item)
                    }
            }
        )

        Spacer(Modifier.height(8.dp))

        Column {
            if (model.statusText.isNotBlank()) {
                Text(
                    text = model.statusText,
                )
            }

            @OptIn(ExperimentalAnimationApi::class)
            AnimatedVisibility(
                visible = model.historyItems.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn {
                    item {
                        StaggeredVerticalGrid(
                            modifier = Modifier.fillMaxWidth(),
                            maxColumnWidth = 400.dp
                        ) {
                            (model.historyItems).forEachIndexed { index, historyInfo ->
                                HistoryItem(
                                    index,
                                    historyInfo,
                                    model.changelogs?.changelogs?.get(historyInfo.firmwareString.split("/")[0]),
                                    { onDownload(model.model, model.region, it) },
                                    { onDecrypt(model.model, model.region, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
