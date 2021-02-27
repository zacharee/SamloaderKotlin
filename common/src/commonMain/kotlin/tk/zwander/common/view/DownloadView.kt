package tk.zwander.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soywiz.korio.async.launch
import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.tools.*
import tk.zwander.common.util.MD5
import kotlin.time.ExperimentalTime

@OptIn(DangerousInternalIoApi::class)
val client = FusClient()
@OptIn(DangerousInternalIoApi::class)
val main = Main()

expect object PlatformDownloadView {
    suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit)
    suspend fun openDecryptOutput(encPath: String, encName: String, callback: suspend CoroutineScope.(AsyncOutputStream) -> Unit)
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
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
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

                                    PlatformDownloadView.openDecryptOutput(info.path, fullFileName) {
                                        val key = if (fullFileName.endsWith(".enc2")) Crypt.getV2Key(model.fw, model.model, model.region) else
                                            Crypt.getV4Key(model.fw, model.model, model.region)

                                        Crypt.decryptProgress(info.input(), it, key, size) { current, max, bps ->
                                            model.progress = current to max
                                            model.speed = bps
                                        }

                                        model.endJob("Done")
                                    }
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
                enabled = canDownload
            ) {
                Text("Download")
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
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
                enabled = canCheckVersion
            ) {
                Text("Check for Updates")
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    model.endJob("")
                },
                enabled = model.job != null
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.weight(1f))

            val boxSource = remember { MutableInteractionSource() }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically)
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
        }

        Spacer(Modifier.height(16.dp))

        MRFLayout(model, canChangeOption, model.manual && canChangeOption)

        Spacer(Modifier.height(16.dp))

        ProgressInfo(model)
    }
}