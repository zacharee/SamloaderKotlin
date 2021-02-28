package tk.zwander.samsungfirmwaredownloader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.util.toAsync
import tk.zwander.common.view.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var openCallback: ((Uri?) -> Unit)? = null

    private val openDownloadTree = registerForActivityResult(object : ActivityResultContract<Uri?, Uri?>() {
        override fun createIntent(context: Context, input: Uri?): Intent {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && input != null
            ) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, input)
            }
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (intent == null || resultCode != RESULT_OK) null else intent.data
        }

        override fun getSynchronousResult(
            context: Context,
            input: Uri?
        ): SynchronousResult<Uri?>? {
            return null
        }
    }) {
        openCallback?.invoke(it)
    }

    private val openDecryptInput = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        openCallback?.invoke(it)
    }

    private val openDecryptOutput = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        openCallback?.invoke(it)
    }

    init {
        //TODO: This is an absolute mess and hopefully there will be a way to
        //TODO: fix it in the future.
        PlatformDownloadView.getInputCallback = input@{ fileName, callback ->
            var uri: Uri? = null

            suspendCoroutine<Unit> { cont ->
                openCallback = {
                    uri = it
                    cont.resume(Unit)
                }

                openDownloadTree.launch(null)
            }

            openCallback = null

            if (uri == null) return@input

            contentResolver.takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            val dir = DocumentFile.fromTreeUri(this@MainActivity, uri!!) ?: return@input
            val decName = fileName.replace(".enc2", "")
                .replace(".enc4", "")

            val enc = dir.findFile(fileName) ?: dir.createFile("application/octet-stream", fileName) ?: return@input
            val dec = dir.findFile(decName) ?: dir.createFile("application/zip", decName) ?: return@input

            val output = contentResolver.openOutputStream(enc.uri, "wa").toAsync()
            val input = { contentResolver.openInputStream(enc.uri).toAsync() }
            val decOutput = contentResolver.openOutputStream(dec.uri).toAsync()

            callback(
                DownloadFileInfo(
                    uri.toString(),
                    output,
                    input,
                    enc.length(),
                    decOutput
                )
            )
        }

        PlatformDecryptView.decryptCallback = input@{ callback ->
            var inputUri: Uri? = null
            var outputUri: Uri? = null

            suspendCoroutine<Unit> { cont ->
                openCallback = {
                    inputUri = it
                    cont.resume(Unit)
                }

                openDecryptInput.launch(arrayOf("application/octet-stream"))
            }

            openCallback = null

            if (inputUri == null) return@input
            val inputFile = DocumentFile.fromSingleUri(this@MainActivity, inputUri!!) ?: return@input

            suspendCoroutine<Unit> { cont ->
                openCallback = {
                    outputUri = it
                    cont.resume(Unit)
                }

                openDecryptOutput.launch(inputFile.name!!.replace(".enc2", "")
                    .replace(".enc4", ""))
            }

            val outputFile = DocumentFile.fromSingleUri(this@MainActivity, outputUri!!) ?: return@input

            val output = contentResolver.openOutputStream(outputFile.uri, "w").toAsync()
            val input = contentResolver.openInputStream(inputFile.uri).toAsync()
            callback(
                DecryptFileInfo(
                    inputFile.name!!,
                    inputFile.uri.toString(),
                    input,
                    inputFile.length(),
                    output
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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