package tk.zwander.samsungfirmwaredownloader

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import tk.zwander.common.IDownloaderService
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformUriFile
import tk.zwander.common.util.Event
import tk.zwander.common.util.EventManager
import tk.zwander.common.util.LocalPhoneInfo
import tk.zwander.common.util.eventManager
import tk.zwander.common.util.rememberPhoneInfo
import tk.zwander.commonCompose.MainView
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.ExperimentalTime

/**
 * The Activity to show the downloader UI.
 */
@ExperimentalTime
class MainActivity : ComponentActivity(), CoroutineScope by MainScope(), EventManager.EventListener, ServiceConnection {
    private val openDownloadContinuation = atomic<Continuation<Uri?>?>(null)
    private val openDecryptInputContinuation = atomic<Continuation<Uri?>?>(null)
    private val openDecryptOutputContinuation = atomic<Continuation<Uri?>?>(null)

    private val downloaderService = atomic<IDownloaderService?>(null)

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
    }) {
        openDownloadContinuation.getAndSet(null)?.resume(it)
    }

    private val openDecryptInput = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        openDecryptInputContinuation.getAndSet(null)?.resume(it)
    }

    private val openDecryptOutput = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) {
        openDecryptOutputContinuation.getAndSet(null)?.resume(it)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkCallingOrSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        eventManager.addListener(this)

        //Set up windowing stuff.
        actionBar?.hide()

        //Set the Compose content.
        setContent {
            CompositionLocalProvider(
                LocalPhoneInfo provides rememberPhoneInfo(),
            ) {
                MainView(
                    modifier = Modifier
                        .imePadding()
                        .systemBarsPadding(),
                )
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        downloaderService.value = IDownloaderService.Stub.asInterface(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        downloaderService.value = null
    }

    override fun onResume() {
        super.onResume()

        //Start the DownloaderService.
        DownloaderService.bind(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        eventManager.removeListener(this)
    }

    override suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Download.GetInput -> {
                val inputUri: Uri? = suspendCoroutine { cont ->
                    openDownloadContinuation.value = cont
                    openDownloadTree.launch(null)
                }

                if (inputUri == null) {
                    event.callback(null)
                    return
                }

                contentResolver.takePersistableUriPermission(
                    inputUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val dir = DocumentFile.fromTreeUri(this@MainActivity, inputUri) ?: return
                val decName = event.fileName.replace(".enc2", "")
                    .replace(".enc4", "")

                val enc = dir.findFile(event.fileName)
                    ?: dir.createFile("application/octet-stream", event.fileName)
                    ?: run {
                        event.callback(null)
                        return
                    }
                val dec =
                    dir.findFile(decName) ?: dir.createFile("application/zip", decName) ?: return

                val decKey = event.decryptKeyFileName?.let {
                    dir.findFile(event.decryptKeyFileName) ?: dir.createFile("text/plain", event.decryptKeyFileName)
                }

                event.callback(
                    DownloadFileInfo(
                        PlatformUriFile(this@MainActivity, enc),
                        PlatformUriFile(this@MainActivity, dec),
                        decKey?.let { PlatformUriFile(this@MainActivity, it) },
                    )
                )
            }
            is Event.Decrypt.GetInput -> {
                val inputUri: Uri? = suspendCoroutine { cont ->
                    openDecryptInputContinuation.value = cont
                    openDecryptInput.launch(arrayOf("application/octet-stream"))
                }

                if (inputUri == null) {
                    event.callback(null)
                    return
                }

                val inputFile =
                    DocumentFile.fromSingleUri(this@MainActivity, inputUri) ?: run {
                        event.callback(null)
                        return
                    }

                if (inputFile.name == null) {
                    event.callback(null)
                    return
                }

                val outputUri: Uri? = suspendCoroutine { cont ->
                    openDecryptOutputContinuation.value = cont
                    openDecryptOutput.launch(
                        inputFile.name!!
                            .replace(".enc2", "")
                            .replace(".enc4", ""),
                    )
                }

                if (outputUri == null) {
                    event.callback(null)
                    return
                }

                val outputFile =
                    DocumentFile.fromSingleUri(this@MainActivity, outputUri) ?: run {
                        event.callback(null)
                        return
                    }

                event.callback(
                    DecryptFileInfo(
                        PlatformUriFile(this@MainActivity, inputFile),
                        PlatformUriFile(this@MainActivity, outputFile)
                    ),
                )
            }
            else -> {}
        }
    }
}
