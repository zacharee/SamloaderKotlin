import androidx.compose.runtime.Composable
import com.soywiz.korio.async.async
import com.soywiz.korio.async.launch
import io.ktor.utils.io.core.internal.*
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.dom.render
import react.dom.unmountComponentAtNode
import tk.zwander.common.data.PlatformFile
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.model.HistoryModel
import tk.zwander.common.tools.*
import tk.zwander.common.util.Averager
import tk.zwander.common.util.ChangelogHandler
import kotlin.math.roundToInt

val downloadModel = DownloadModel()
val decryptModel = DecryptModel()
val historyModel = HistoryModel()

/**
 * @param key - when UseReactEffect is invoked with a new [key], compose forces react to render with a new content.
 * @param content - the builder for the content managed by React
 */
@Composable
private fun ElementScope<HTMLElement>.UseReactEffect(
    key: Any?,
    content: RBuilder.() -> Unit
) {
    DomSideEffect(key = key) { htmlElement ->
        render(htmlElement) {
            content()
        }
    }

    DisposableRefEffect { htmlElement ->
        onDispose {
            unmountComponentAtNode(htmlElement)
        }
    }
}

@OptIn(DangerousInternalIoApi::class)
val client = FusClient(useProxy = true)

fun main() {
    js("""
            window.originalFetch = window.fetch;
            window.fetch = function (resource, init) {
                init = Object.assign({}, init);
                init.credentials = init.credentials !== undefined ? init.credentials : 'include';
                return window.originalFetch(resource, init);
            };
        """)

    renderComposable(rootElementId = "root") {
        Div {
            TextInput(downloadModel.model) {
                onChange { downloadModel.model = it.value }
            }

            TextInput(downloadModel.region) {
                onChange { downloadModel.region = it.value }
            }

            Text(downloadModel.fw)

            Button(
                attrs = {
                    onClick { downloadModel.job = downloadModel.scope.async { doCheck() } }
                }
            ) {
                Text("Retrieve")
            }

            if (downloadModel.fw.isNotBlank() && downloadModel.model.isNotBlank() && downloadModel.region.isNotBlank()
                && downloadModel.job == null) {
                Button(
                    attrs = {
                        onClick { downloadModel.job = downloadModel.scope.async { doDownload() } }
                    }
                ) {
                    Text("Download")
                }
            }

            Text("Status: ${downloadModel.statusText}")

            val currentMB = (downloadModel.progress.first.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0
            val totalMB = (downloadModel.progress.second.toFloat() / 1024.0 / 1024.0 * 100.0).roundToInt() / 100.0

            val speedKBps = downloadModel.speed / 1024.0
            val shouldUseMB = speedKBps >= 1 * 1024
            val finalSpeed = "${((if (shouldUseMB) (speedKBps / 1024.0) else speedKBps) * 100.0).roundToInt() / 100.0}"

            Text("Progress: $currentMB / $totalMB MiB,,, $finalSpeed ${if (shouldUseMB) "MiB/s" else "KiB/s"}")
        }
    }
}

suspend fun doCheck() {
    val (fw, os) = try {
        VersionFetch.getLatestVersion(downloadModel.model, downloadModel.region, true)
    } catch (e: Exception) {
        downloadModel.endJob("Error checking for firmware. Make sure the model and region are correct.\nMore info: ${e.message}")
        "" to ""
        return
    }

    downloadModel.changelog = ChangelogHandler.getChangelog(downloadModel.model, downloadModel.region, fw.split("/")[0], true)

    downloadModel.fw = fw
    downloadModel.osCode = os

    downloadModel.endJob("")
}

@OptIn(DangerousInternalIoApi::class, kotlin.time.ExperimentalTime::class)
suspend fun doDownload() {
    try {
        val (path, fileName, size, crc32) = Request.getBinaryFile(client, downloadModel.fw, downloadModel.model, downloadModel.region)
        val request = Request.createBinaryInit(fileName, client.nonce)

        println("binary init before")

        client.makeReq(FusClient.Request.BINARY_INIT, request)

        println("binary init after")

        val fullFileName = fileName.replace(".zip",
            "_${downloadModel.fw.replace("/", "_")}_${downloadModel.region}.zip")

        println("savefile before")

        val saveFile = PlatformFile("${fullFileName}.enc")

        println("savefile after ${saveFile.getLength()}")

        println("downloadfile before")

        val averager = Averager()
        var prevSent = 0L
        var prevCallTime = Clock.System.now().toEpochMilliseconds()

        val (response, md5) = client.downloadFile(
            path + fileName,
            saveFile.getLength(),
            true
        ) { bytesSentTotal, contentLength ->
            val newCallTime = Clock.System.now().toEpochMilliseconds()
            val nano = (newCallTime - prevCallTime) * 1000 * 1000

            averager.update(nano, bytesSentTotal - prevSent)

            val (totalTime, totalRead, _) = averager.sum()

            val bps = (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong()

            println("download progress $bytesSentTotal $contentLength $bps")

            downloadModel.progress = bytesSentTotal to contentLength
            downloadModel.speed = bps

            prevCallTime = newCallTime
            prevSent = bytesSentTotal
        }

        println("downloadfile after")

        Downloader.download(
            response,
            size,
            saveFile.openOutputStream(true),
            saveFile.getLength()
        ) { current, max, bps ->
            println("download progress $current $max $bps")

            downloadModel.progress = current to max
            downloadModel.speed = bps
        }

        println("download after")

        downloadModel.speed = 0L

        if (crc32 != null) {
            downloadModel.statusText = "Checking CRC"
            val result = CryptUtils.checkCrc32(
                saveFile.openInputStream(),
                size,
                crc32
            ) { current, max, bps ->
                downloadModel.progress = current to max
                downloadModel.speed = bps
            }

            if (!result) {
                downloadModel.endJob("CRC check failed. Please delete the file and download again.")
                return
            }
        }

        if (md5 != null) {
            downloadModel.statusText = "Checking MD5"
            downloadModel.progress = 1L to 2L

            val result = withContext(Dispatchers.Default) {
                CryptUtils.checkMD5(
                    md5,
                    saveFile.openInputStream()
                )
            }

            if (!result) {
                downloadModel.endJob("MD5 check failed. Please delete the file and download again.")
            }
        }

        downloadModel.statusText = "Decrypting Firmware"

        val key =
            if (fullFileName.endsWith(".enc2")) CryptUtils.getV2Key(
                downloadModel.fw,
                downloadModel.model,
                downloadModel.region
            ) else {
                CryptUtils.getV4Key(downloadModel.fw, downloadModel.model, downloadModel.region)
            }

        val decryptFile = PlatformFile(fullFileName.replace(".enc2", "").replace(".enc4", ""))

        CryptUtils.decryptProgress(
            saveFile.openInputStream(),
            decryptFile.openOutputStream(),
            key,
            size
        ) { current, max, bps ->
            downloadModel.progress = current to max
            downloadModel.speed = bps
        }

        downloadModel.endJob("Done")
    } catch (e: Exception) {
        e.printStackTrace()
        downloadModel.endJob(e.message ?: "Error")
    }
}
