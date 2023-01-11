package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soywiz.korio.async.launch
import com.soywiz.korio.lang.substr
import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.serialization.xml.firstDescendant
import tk.zwander.common.data.HistoryInfo
import tk.zwander.commonCompose.model.HistoryModel
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.getFirmwareHistoryString
import tk.zwander.common.util.getFirmwareHistoryStringFromSamsung
import tk.zwander.commonCompose.locals.LocalHistoryModel
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.commonCompose.view.components.HistoryItem
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.samloaderkotlin.resources.MR
import tk.zwander.samloaderkotlin.strings

/**
 * Delegate HTML parsing to the platform until there's an MPP library.
 */
expect object PlatformHistoryView {
    suspend fun parseHistory(body: String): List<HistoryInfo>
}

private fun parseHistoryXml(xml: String): List<HistoryInfo> {
    val doc = Xml(xml)

    val latest = doc.firstDescendant("firmware")?.firstDescendant("version")?.firstDescendant("latest")
    val historical = doc.firstDescendant("firmware")?.firstDescendant("version")?.firstDescendant("upgrade")
        ?.get("value")

    val items = arrayListOf<HistoryInfo>()

    fun parseFirmware(string: String): String {
        val firmwareParts = string.split("/").filterNot { it.isBlank() }.toMutableList()

        if (firmwareParts.size == 2) {
            firmwareParts.add(firmwareParts[0])
            firmwareParts.add(firmwareParts[0])
        }

        if (firmwareParts.size == 3) {
            firmwareParts.add(firmwareParts[0])
        }

        return firmwareParts.joinToString("/")
    }

    latest?.apply {
        val androidVersion = latest.attribute("o")

        items.add(
            HistoryInfo(
                date = null,
                androidVersion = androidVersion,
                firmwareString = parseFirmware(latest.text)
            )
        )
    }

    historical?.apply {
        items.addAll(
            mapNotNull {
                val firmware = parseFirmware(it.text)

                if (firmware.isNotBlank()) {
                    HistoryInfo(
                        date = null,
                        androidVersion = null,
                        firmwareString = firmware
                    )
                } else {
                    null
                }
            }.sortedByDescending {
                it.firmwareString.let { f ->
                    f.substr(f.lastIndex - 3)
                }
            }
        )
    }

    return items
}

private suspend fun onFetch(model: HistoryModel) {
    val historyString = getFirmwareHistoryString(model.model, model.region)
    val historyStringXml = getFirmwareHistoryStringFromSamsung(model.model, model.region)

    if (historyString == null && historyStringXml == null) {
        model.endJob(strings.historyError())
    } else {
        try {
            val parsed = when {
                historyString != null -> {
                    PlatformHistoryView.parseHistory(historyString)
                }

                historyStringXml != null -> {
                    parseHistoryXml(historyStringXml)
                }

                else -> {
                    model.endJob(strings.historyError())
                    return
                }
            }

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
            model.endJob(strings.historyErrorFormat(e.message.toString()))
        }
    }
}

/**
 * The History View.
 * @param onDownload a callback for when the user hits the "Download" button on an item.
 * @param onDecrypt a callback for when the user hits the "Decrypt" button on an item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryView(
    onDownload: (model: String, region: String, fw: String) -> Unit,
    onDecrypt: (model: String, region: String, fw: String) -> Unit
) {
    val model = LocalHistoryModel.current
    val canCheckHistory = model.model.isNotBlank()
            && model.region.isNotBlank() && model.job == null

    val odinRomSource = buildAnnotatedString {
        pushStyle(
            SpanStyle(
                color = LocalContentColor.current,
                fontSize = 16.sp
            )
        )
        append(strings.source())
        append(" ")
        pushStyle(
            SpanStyle(
                color = MaterialTheme.colors.primary,
                textDecoration = TextDecoration.Underline
            )
        )
        pushStringAnnotation("OdinRomLink", "https://odinrom.com")
        append(strings.odinRom())
        pop()
    }

    Column(
        Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val constraints = constraints

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                HybridButton(
                    onClick = {
                        model.historyItems = listOf()

                        model.job = model.scope.launch {
                            onFetch(model)
                        }
                    },
                    enabled = canCheckHistory,
                    text = strings.checkHistory(),
                    description = strings.checkHistory(),
                    vectorIcon = vectorResource(MR.assets.refresh),
                    parentSize = constraints.maxWidth
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
                    text = strings.cancel(),
                    description = strings.cancel(),
                    vectorIcon = vectorResource(MR.assets.cancel),
                    parentSize = constraints.maxWidth
                )
            }
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

        val expanded = remember {
            mutableStateMapOf<String, Boolean>()
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (model.statusText.isNotBlank()) {
                Text(
                    text = model.statusText,
                )
            }

            AnimatedVisibility(
                visible = model.historyItems.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxWidth(),
                    columns = StaggeredGridCells.Adaptive(minSize = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    itemsIndexed(model.historyItems, { _, item -> item.toString() }) { index, historyInfo ->
                        HistoryItem(
                            index = index,
                            info = historyInfo,
                            changelog = model.changelogs?.changelogs?.get(historyInfo.firmwareString.split("/")[0]),
                            changelogExpanded = expanded[historyInfo.toString()] ?: false,
                            onChangelogExpanded =  { expanded[historyInfo.toString()] = it },
                            onDownload = { onDownload(model.model, model.region, it) },
                            onDecrypt = { onDecrypt(model.model, model.region, it) }
                        )
                    }
                }
            }
        }
    }
}
