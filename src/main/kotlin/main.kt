import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import tools.*
import util.MD5
import java.io.File
import java.nio.file.Paths
import javax.swing.JFileChooser
import javax.swing.JFrame
import kotlin.math.roundToInt

val client = FusClient()
val main = Main()

fun main() = Window {
    val model = mutableStateOf("")
    val region = mutableStateOf("")
    val fw = mutableStateOf("")
    val progress = mutableStateOf(0L to 1L)
    val speed = mutableStateOf(0L)
    val automatic = mutableStateOf(true)
    val statusText = mutableStateOf("")
    val downloadJob = mutableStateOf<Job?>(null)

    MaterialTheme {
        val scope = rememberCoroutineScope()

        val canCheckVersion = automatic.value && model.value.isNotBlank()
                && region.value.isNotBlank()

        val canDownload = model.value.isNotBlank() && region.value.isNotBlank() && fw.value.isNotBlank()

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = automatic.value,
                    onCheckedChange = {
                        automatic.value = it
                    },
                )

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        downloadJob.value = scope.launch(Dispatchers.Main) {
                            statusText.value = "Downloading"
                            val (path, fileName, size, crc32) = main.getBinaryFile(client, fw.value, model.value, region.value)
                            val request = Request.binaryInit(fileName, client.nonce)
                            val resp = client.makeReq("NF_DownloadBinaryInitForMass.do", request)

                            val chooser = JFileChooser()
                            chooser.selectedFile = File(Paths.get("").toAbsolutePath().toString(), fileName)
                            val res = chooser.showSaveDialog(JFrame())

                            if (res == JFileChooser.APPROVE_OPTION) {
                                val output = chooser.selectedFile
                                val offset = if (output.exists()) output.length() else 0

                                val response = client.downloadFile(path + fileName, offset)
                                val md5 = if (response.headers.containsKey("Content-MD5")) response.headers["Content-MD5"].first() else null

                                Downloader.download(response, size, output) { current, max, bps ->
                                    progress.value = current to max
                                    speed.value = bps
                                }

                                speed.value = 0L

                                if (crc32 != null) {
                                    statusText.value = "Checking CRC"
                                    val result = Crypt.checkCrc32(output, crc32) { current, max, bps ->
                                        progress.value = current to max
                                        speed.value = bps
                                    }

                                    if (!result) {
                                        statusText.value = "CRC check failed. Please download again."
                                        output.delete()
                                        return@launch
                                    }
                                }

                                if (md5 != null) {
                                    statusText.value = "Checking MD5"
                                    progress.value = 1L to 2L

                                    val result = withContext(Dispatchers.IO) {
                                        MD5.checkMD5(md5, output)
                                    }

                                    if (!result) {
                                        statusText.value = "MD5 check failed. Please download again."
                                        output.delete()
                                        return@launch
                                    }
                                }

                                statusText.value = "Decrypting Firmware"
                                val decFile = File(output.parentFile, fileName.replace(".enc4", "").replace(".enc2", ""))
                                val key = if (fileName.endsWith(".enc2")) Crypt.getV2Key(fw.value, model.value, region.value) else
                                    Crypt.getV4Key(fw.value, model.value, region.value)

                                Crypt.decryptProgress(output, decFile.outputStream(), key, output.length()) { current, max, bps ->
                                    progress.value = current to max
                                    speed.value = bps
                                }

                                statusText.value = "Done"
                                speed.value = 0
                            } else {
                                statusText.value = ""
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
                        fw.value = VersionFetch.getLatestVer(model.value, region.value)
                    },
                    enabled = canCheckVersion
                ) {
                    Text("Check for Updates")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        downloadJob.value?.apply {
                            cancelChildren()
                            cancel()
                        }
                        downloadJob.value = null
                        progress.value = 0L to 1L
                        statusText.value = ""
                        speed.value = 0
                    },
                    enabled = downloadJob.value != null
                ) {
                    Text("Cancel")
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = model.value,
                    onValueChange = { model.value = it },
                    label = { Text("Model") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                TextField(
                    value = region.value,
                    onValueChange = { region.value = it },
                    label = { Text("Region") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = fw.value,
                    onValueChange = { fw.value = it },
                    label = { Text("Firmware") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = automatic.value
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = (progress.value.first.toFloat() / progress.value.second),
                        modifier = Modifier.height(8.dp).weight(1f).align(Alignment.CenterVertically)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "${(progress.value.first.toFloat() / progress.value.second * 10000).roundToInt() / 100.0}%",
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "${speed.value / 1024} KB/s",
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = statusText.value
                )
            }
        }
    }
}