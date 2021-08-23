import androidx.compose.runtime.Composable
import com.soywiz.korio.async.launch
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import tk.zwander.common.util.ChangelogHandler

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
                    onClick { downloadModel.job = downloadModel.scope.launch { doCheck() } }
                }
            ) {
                Text("Retrieve")
            }

            if (downloadModel.fw.isNotBlank() && downloadModel.model.isNotBlank() && downloadModel.region.isNotBlank()
                && downloadModel.job == null) {
                Button(
                    attrs = {
                        onClick { downloadModel.job = downloadModel.scope.launch { doDownload() } }
                    }
                ) {
                    Text("Download")
                }
            }

            Text("Status: ${downloadModel.statusText}")
            Text("Progress: ${downloadModel.progress}")
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
    val (path, fileName, size, crc32) = Request.getBinaryFile(client, downloadModel.fw, downloadModel.model, downloadModel.region)
    val request = Request.createBinaryInit(fileName, client.nonce)

    client.makeReq(FusClient.Request.BINARY_INIT, request)

    val fullFileName = fileName.replace(".zip",
        "_${downloadModel.fw.replace("/", "_")}_${downloadModel.region}.zip")

    val saveFile = PlatformFile("${fullFileName}.enc")

    val (response, md5) = client.downloadFile(
        path + fileName,
        saveFile.length
    )

    Downloader.download(
        response,
        size,
        saveFile.openOutputStream(true),
        saveFile.length
    ) { current, max, bps ->
        downloadModel.progress = current to max
        downloadModel.speed = bps
    }

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
}
