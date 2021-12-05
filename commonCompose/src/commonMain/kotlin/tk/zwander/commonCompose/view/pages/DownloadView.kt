package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.soywiz.korio.async.launch
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.tools.*
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.commonCompose.downloadModel
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.ProgressInfo
import tk.zwander.commonCompose.view.components.ChangelogDisplay
import tk.zwander.commonCompose.view.components.ExpandButton
import kotlin.time.ExperimentalTime

/**
 * The FusClient for retrieving firmware.
 */
@OptIn(DangerousInternalIoApi::class)
val client = FusClient()

/**
 * Delegate retrieving the download location to the platform.
 */
expect object PlatformDownloadView {
    suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit)
    fun onStart()
    fun onFinish()
    fun onProgress(status: String, current: Long, max: Long)
}

/**
 * The Downloader View.
 * @param model the Download model.
 * @param scrollState a shared scroll state.
 */
@DangerousInternalIoApi
@ExperimentalTime
@Composable
fun DownloadView(model: DownloadModel, scrollState: ScrollState) {
    val canCheckVersion = !model.manual && model.model.isNotBlank()
            && model.region.isNotBlank() && model.job == null

    val canDownload = model.model.isNotBlank() && model.region.isNotBlank() && model.fw.isNotBlank()
            && model.job == null

    val canChangeOption = model.job == null

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        val rowSize = remember { mutableStateOf(0.dp) }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .onSizeChanged { rowSize.value = it.width.dp },
        ) {
            HybridButton(
                onClick = {
                    model.job = model.scope.launch(Dispatchers.Main) {
                        PlatformDownloadView.onStart()
                        model.statusText = "Downloading"

                        val (info, error, output) = Request.getBinaryFile(client, downloadModel.fw, downloadModel.model, downloadModel.region)

                        if (error != null) {
                            error.printStackTrace()
                            downloadModel.endJob("${error.message ?: "Error"}\n\n${output}")
                        } else {
                            val (path, fileName, size, crc32, v4Key) = info!!
                            val request = Request.createBinaryInit(fileName, client.getNonce())

                            client.makeReq(FusClient.Request.BINARY_INIT, request)

                            val fullFileName = fileName.replace(".zip",
                                "_${model.fw.replace("/", "_")}_${model.region}.zip")

                            PlatformDownloadView.getInput(fullFileName) { info ->
                                if (info != null) {
                                    val (response, md5) = client.downloadFile(
                                        path + fileName,
                                        info.downloadFile.getLength()
                                    )

                                    Downloader.download(
                                        response,
                                        size,
                                        info.downloadFile.openOutputStream(true),
                                        info.downloadFile.getLength()
                                    ) { current, max, bps ->
                                        model.progress = current to max
                                        model.speed = bps

                                        PlatformDownloadView.onProgress("Downloading", current, max)
                                    }

                                    model.speed = 0L

                                    if (crc32 != null) {
                                        model.statusText = "Checking CRC"
                                        val result = CryptUtils.checkCrc32(
                                            info.downloadFile.openInputStream(),
                                            size,
                                            crc32
                                        ) { current, max, bps ->
                                            model.progress = current to max
                                            model.speed = bps

                                            PlatformDownloadView.onProgress(
                                                "Checking CRC32",
                                                current,
                                                max
                                            )
                                        }

                                        if (!result) {
                                            model.endJob("CRC check failed. Please delete the file and download again.")
                                            return@getInput
                                        }
                                    }

                                    if (md5 != null) {
                                        model.statusText = "Checking MD5"
                                        model.progress = 1L to 2L

                                        PlatformDownloadView.onProgress("Checking MD5", 0, 1)

                                        val result = withContext(Dispatchers.Default) {
                                            CryptUtils.checkMD5(
                                                md5,
                                                info.downloadFile.openInputStream()
                                            )
                                        }

                                        if (!result) {
                                            model.endJob("MD5 check failed. Please delete the file and download again.")
                                            return@getInput
                                        }
                                    }

                                    model.statusText = "Decrypting Firmware"

                                    val key =
                                        if (fullFileName.endsWith(".enc2")) CryptUtils.getV2Key(
                                            model.fw,
                                            model.model,
                                            model.region
                                        ) else {
                                            v4Key ?: CryptUtils.getV4Key(client, model.fw, model.model, model.region)
                                        }

                                    CryptUtils.decryptProgress(
                                        info.downloadFile.openInputStream(),
                                        info.decryptFile.openOutputStream(),
                                        key,
                                        size
                                    ) { current, max, bps ->
                                        model.progress = current to max
                                        model.speed = bps

                                        PlatformDownloadView.onProgress("Decrypting", current, max)
                                    }

                                    model.endJob("Done")
                                } else {
                                    model.endJob("")
                                }
                            }
                        }

                        PlatformDownloadView.onFinish()
                    }
                },
                enabled = canDownload,
                vectorIcon = vectorResource("download.xml"),
                text = "Download",
                description = "Download Firmware",
                parentSize = rowSize.value
            )

            Spacer(Modifier.width(8.dp))

            HybridButton(
                onClick = {
                    model.job = model.scope.launch {
                        val (fw, os, error, output) = VersionFetch.getLatestVersion(model.model, model.region)

                        if (error != null) {
                            model.endJob("Error checking for firmware. Make sure the model and region are correct.\nMore info: ${error.message}\n\n$output")
                            return@launch
                        }

                        model.changelog = ChangelogHandler.getChangelog(model.model, model.region, fw.split("/")[0])

                        model.fw = fw
                        model.osCode = os

                        model.endJob("")
                    }
                },
                enabled = canCheckVersion,
                text = "Check for Updates",
                vectorIcon = vectorResource("refresh.xml"),
                description = "Check for Firmware Updates",
                parentSize = rowSize.value
            )

            Spacer(Modifier.weight(1f))

            HybridButton(
                onClick = {
                    PlatformDownloadView.onFinish()
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

//        val boxSource = remember { MutableInteractionSource() }
//
//        Row(
//            modifier = Modifier.align(Alignment.End)
//                .clickable(
//                    interactionSource = boxSource,
//                    indication = null
//                ) {
//                    model.manual = !model.manual
//                }
//                .padding(4.dp)
//        ) {
//            Text(
//                text = "Manual",
//                modifier = Modifier.align(Alignment.CenterVertically)
//            )
//
//            Spacer(Modifier.width(8.dp))
//
//            Checkbox(
//                checked = model.manual,
//                onCheckedChange = {
//                    model.manual = it
//                },
//                modifier = Modifier.align(Alignment.CenterVertically),
//                enabled = canChangeOption,
//                colors = CheckboxDefaults.colors(
//                    checkedColor = MaterialTheme.colors.primary,
//                ),
//                interactionSource = boxSource
//            )
//        }
//
//        Spacer(Modifier.height(8.dp))

        MRFLayout(model, canChangeOption, model.manual && canChangeOption)

        AnimatedVisibility(
            visible = !model.manual && model.osCode.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = "OS Version: ${model.osCode}"
                )
            }
        }

        AnimatedVisibility(
            visible = model.progress.first > 0 || model.progress.second > 0 || model.statusText.isNotBlank(),
        ) {
            Column {
                Spacer(Modifier.height(16.dp))

                ProgressInfo(model)
            }
        }

        val changelogCondition = model.changelog != null && !model.manual && model.job == null && model.fw.isNotBlank()

        AnimatedVisibility(
            visible = changelogCondition,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(8.dp))

                ExpandButton(
                    model.changelogExpanded,
                    "Changelog"
                ) { model.changelogExpanded = it }

                Spacer(Modifier.height(8.dp))
            }
        }

        AnimatedVisibility(
            visible = model.changelogExpanded && changelogCondition,
        ) {
            ChangelogDisplay(model.changelog!!)
        }
    }
}
