package tk.zwander.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.soywiz.korim.format.nativeImageFormatProvider
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.format.readBitmapInfo
import com.soywiz.korim.format.readNativeImage
import com.soywiz.korio.async.launch
import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.std.applicationVfs
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.tools.*
import tk.zwander.common.util.MD5
import tk.zwander.common.util.imageResource
import kotlin.time.ExperimentalTime

@OptIn(DangerousInternalIoApi::class)
val client = FusClient()
@OptIn(DangerousInternalIoApi::class)
val main = Main()

expect object PlatformDownloadView {
    suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit)
    suspend fun deleteFile(path: String)
}

@DangerousInternalIoApi
@ExperimentalTime
@Composable
fun DownloadView(model: DownloadModel) {
    val canCheckVersion = !model.manual && model.model.isNotBlank()
            && model.region.isNotBlank() && model.job == null

    val canDownload = model.model.isNotBlank() && model.region.isNotBlank() && model.fw.isNotBlank()
            && model.job == null

    val canChangeOption = model.job == null

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val rowSize = remember { mutableStateOf(0.dp) }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .onSizeChanged { rowSize.value = it.width.dp },
        ) {
            HybridButton(
                onClick = {
                    model.job = model.scope.launch(Dispatchers.Main) {
                        try {
                            model.statusText = "Downloading"
                            val (path, fileName, size, crc32) = main.getBinaryFile(client, model.fw, model.model, model.region)
                            val request = Request.binaryInit(fileName, client.nonce)
                            val resp = client.makeReq("NF_DownloadBinaryInitForMass.do", request)

                            val fullFileName = fileName.replace(".zip",
                                "_${model.fw.replace("/", "_")}_${model.region}.zip")

                            PlatformDownloadView.getInput(fullFileName) { info ->
                                if (info != null) {
                                    val output = info.output
                                    val input = info.input
                                    val offset = info.existingSize

                                    val (response, md5) = client.downloadFile(path + fileName, offset)

                                    Downloader.download(response, size, output, offset) { current, max, bps ->
                                        model.progress = current to max
                                        model.speed = bps
                                    }

                                    model.speed = 0L

                                    if (crc32 != null) {
                                        model.statusText = "Checking CRC"
                                        val result = Crypt.checkCrc32(input(), size, crc32) { current, max, bps ->
                                            model.progress = current to max
                                            model.speed = bps
                                        }

                                        if (!result) {
                                            model.endJob("CRC check failed. Please download again.")
                                            PlatformDownloadView.deleteFile(info.path)
                                            return@getInput
                                        }
                                    }

                                    if (md5 != null) {
                                        model.statusText = "Checking MD5"
                                        model.progress = 1L to 2L

                                        val result = withContext(Dispatchers.Default) {
                                            MD5.checkMD5(md5, input())
                                        }

                                        if (!result) {
                                            model.endJob("MD5 check failed. Please download again.")
                                            PlatformDownloadView.deleteFile(info.path)
                                            return@getInput
                                        }
                                    }

                                    model.statusText = "Decrypting Firmware"

                                    val key = if (fullFileName.endsWith(".enc2")) Crypt.getV2Key(model.fw, model.model, model.region) else
                                        Crypt.getV4Key(model.fw, model.model, model.region)

                                    Crypt.decryptProgress(info.input(), info.decryptOutput, key, size) { current, max, bps ->
                                        model.progress = current to max
                                        model.speed = bps
                                    }

                                    model.endJob("Done")
                                } else {
                                    model.endJob("")
                                }
                            }
                        } catch (e: Exception) {
                            if (e !is CancellationException) {
                                e.printStackTrace()
                                model.endJob("Error: ${e.message}")
                            }
                        }
                    }
                },
                enabled = canDownload,
                icon = imageResource("download.png"),
                text = "Download",
                description = "Download Firmware",
                parentSize = rowSize.value
            )

            Spacer(Modifier.width(8.dp))

            HybridButton(
                onClick = {
                    model.job = model.scope.launch {
                        model.fw = try {
                            VersionFetch.getLatestVer(model.model, model.region).also {
                                model.endJob("")
                            }
                        } catch (e: Exception) {
                            model.endJob("Error checking for firmware. Make sure the model and region are correct.\nMore info: ${e.message}")
                            ""
                        }
                    }
                },
                enabled = canCheckVersion,
                text = "Check for Updates",
                icon = imageResource("refresh.png"),
                description = "Check for Firmware Updates",
                parentSize = rowSize.value
            )

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

        val boxSource = remember { MutableInteractionSource() }

        Row(
            modifier = Modifier.align(Alignment.End)
                .clickable(
                    interactionSource = boxSource,
                    indication = null
                ) {
                    model.manual = !model.manual
                }
                .padding(4.dp)
        ) {
            Text(
                text = "Manual",
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.width(8.dp))

            Checkbox(
                checked = model.manual,
                onCheckedChange = {
                    model.manual = it
                },
                modifier = Modifier.align(Alignment.CenterVertically),
                enabled = canChangeOption,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colors.primary,
                ),
                interactionSource = boxSource
            )
        }

        Spacer(Modifier.height(16.dp))

        MRFLayout(model, canChangeOption, model.manual && canChangeOption)

        Spacer(Modifier.height(16.dp))

        ProgressInfo(model)
    }
}