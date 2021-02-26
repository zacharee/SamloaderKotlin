package view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import model.DownloadModel
import tools.*
import util.MD5
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

val client = FusClient()
val main = Main()

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
            Button(
                onClick = {
                    model.job = model.scope.launch(Dispatchers.Main) {
                        try {
                            model.statusText = "Downloading"
                            val (path, fileName, size, crc32) = main.getBinaryFile(client, model.fw, model.model, model.region)
                            val request = Request.binaryInit(fileName, client.nonce)
                            val resp = client.makeReq("NF_DownloadBinaryInitForMass.do", request)

                            val fullFileName = fileName.replace(".zip",
                                "_${model.fw.replace("/", "_")}_${model.region}.zip")

                            val chooser = FileDialog(Frame())
                            chooser.file = fullFileName
                            chooser.mode = FileDialog.SAVE
                            chooser.isVisible = true

                            if (chooser.file != null) {
                                val output = File(chooser.directory, chooser.file)
                                val offset = if (output.exists()) output.length() else 0

                                val response = client.downloadFile(path + fileName, offset)
                                val md5 = if (response.headers().firstValue("Content-MD5").isPresent) response.headers().firstValue("Content-MD5").get() else null

                                Downloader.download(response, size, output) { current, max, bps ->
                                    model.progress = current to max
                                    model.speed = bps
                                }

                                model.speed = 0L

                                if (crc32 != null) {
                                    model.statusText = "Checking CRC"
                                    val result = Crypt.checkCrc32(output, crc32) { current, max, bps ->
                                        model.progress = current to max
                                        model.speed = bps
                                    }

                                    if (!result) {
                                        model.endJob("CRC check failed. Please download again.")
                                        output.delete()
                                        return@launch
                                    }
                                }

                                if (md5 != null) {
                                    model.statusText = "Checking MD5"
                                    model.progress = 1L to 2L

                                    val result = withContext(Dispatchers.IO) {
                                        MD5.checkMD5(md5, output)
                                    }

                                    if (!result) {
                                        model.endJob("MD5 check failed. Please download again.")
                                        output.delete()
                                        return@launch
                                    }
                                }

                                model.statusText = "Decrypting Firmware"
                                val decFile = File(output.parentFile, fullFileName.replace(".enc4", "").replace(".enc2", ""))
                                val key = if (fullFileName.endsWith(".enc2")) Crypt.getV2Key(model.fw, model.model, model.region) else
                                    Crypt.getV4Key(model.fw, model.model, model.region)

                                Crypt.decryptProgress(output, decFile.outputStream(), key, output.length()) { current, max, bps ->
                                    model.progress = current to max
                                    model.speed = bps
                                }
                                model.endJob("Done")
                            } else {
                                model.endJob("")
                            }
                        } catch (e: Exception) {
                            model.endJob("Error: ${e.message}")
                        }
                    }
                },
                enabled = canDownload
            ) {
                Text("Download")
            }

            Spacer(Modifier.width(8.dp))

            Button(
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

            Button(
                onClick = {
                    model.endJob("")
                },
                enabled = model.job != null
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = "Manual",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = model.manual,
                    onCheckedChange = {
                        model.manual = it
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    enabled = canChangeOption
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = model.model,
                onValueChange = { model.model = it.toUpperCase() },
                label = { Text("Model") },
                modifier = Modifier.weight(1f),
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
            )

            Spacer(Modifier.width(8.dp))

            TextField(
                value = model.region,
                onValueChange = { model.region = it.toUpperCase() },
                label = { Text("Region") },
                modifier = Modifier.weight(1f),
                readOnly = !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = model.fw,
                onValueChange = { model.fw = it.toUpperCase() },
                label = { Text("Firmware") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !model.manual || !canChangeOption,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters)
            )
        }

        Spacer(Modifier.height(16.dp))

        ProgressInfo(model)
    }
}