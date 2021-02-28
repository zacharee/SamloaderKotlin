package tk.zwander.samsungfirmwaredownloader

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.util.toAsync
import tk.zwander.common.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        private const val REQ_SAVE_DECRYPT = 10001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlatformDownloadView.getInputCallback = { fileName, callback ->
            val outputFile = File(cacheDir, fileName)
            callback(
                DownloadFileInfo(
                    outputFile.absolutePath,
                    FileOutputStream(outputFile, true).toAsync(),
                    { FileInputStream(outputFile).toAsync() },
                    outputFile.length()
                )
            )
        }

        PlatformDownloadView.getDecryptOutputCallback = { encPath, encName, callback ->
            val encFile = File(encPath)
            val decFile = File(encFile.parentFile,
                encName.replace(".enc2", "")
                    .replace(".enc4", ""))

            callback(decFile.outputStream().toAsync())
        }

        setContent {
            val page = remember { mutableStateOf(Page.DOWNLOADER) }

            val downloadModel = remember { DownloadModel() }
            val decryptModel = remember { DecryptModel() }

            CustomMaterialTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            TabView(page)

                            Divider(
                                thickness = 1.dp,
                                color = MaterialTheme.colors.onSurface
                            )

                            Spacer(Modifier.height(16.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                when (page.value) {
                                    Page.DOWNLOADER -> DownloadView(downloadModel)
                                    Page.DECRYPTER -> DecryptView(decryptModel)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        FooterView()
                    }
                }
            }
        }
    }
}