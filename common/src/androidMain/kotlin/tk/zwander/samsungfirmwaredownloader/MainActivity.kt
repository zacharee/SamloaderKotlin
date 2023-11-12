package tk.zwander.samsungfirmwaredownloader

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformUriFile
import tk.zwander.common.util.Event
import tk.zwander.common.util.EventManager
import tk.zwander.common.util.eventManager
import tk.zwander.commonCompose.MainView
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.ExperimentalTime

/**
 * The Activity to show the downloader UI.
 */
@ExperimentalTime
class MainActivity : ComponentActivity(), CoroutineScope by MainScope(), EventManager.EventListener {
    /**
     * Set whenever the DownloaderService needs to select a file or folder.
     * Called once the user makes a selection.
     */
    private var openCallback: Continuation<Uri?>? = null

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
        openCallback?.resume(it)
    }

    private val openDecryptInput = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        openCallback?.resume(it)
    }

    private val openDecryptOutput = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) {
        openCallback?.resume(it)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkCallingOrSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        eventManager.addListener(this)

        //Start the DownloaderService.
        DownloaderService.start(this)

        //Set up windowing stuff.
        actionBar?.hide()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        //Set the Compose content.
        setContent {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isSystemInDarkTheme()
                isAppearanceLightNavigationBars = isAppearanceLightStatusBars
            }

            MainView(
                Modifier
                    .imePadding()
                    .systemBarsPadding()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventManager.removeListener(this)
    }

    override suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Download.GetInput -> {
                event.callbackScope.launch {
                    val inputUri: Uri? = suspendCoroutine { cont ->
                        openCallback = cont

                        openDownloadTree.launch(null)
                    }

                    if (inputUri == null) {
                        event.callback(this, null)
                        return@launch
                    }

                    contentResolver.takePersistableUriPermission(
                        inputUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                    val dir = DocumentFile.fromTreeUri(this@MainActivity, inputUri) ?: return@launch
                    val decName = event.fileName.replace(".enc2", "")
                        .replace(".enc4", "")

                    val enc = dir.findFile(event.fileName)
                        ?: dir.createFile("application/octet-stream", event.fileName)
                        ?: run {
                            event.callback(this, null)
                            return@launch
                        }
                    val dec =
                        dir.findFile(decName) ?: dir.createFile("application/zip", decName) ?: return@launch

                    event.callback(
                        this,
                        DownloadFileInfo(
                            PlatformUriFile(this@MainActivity, enc),
                            PlatformUriFile(this@MainActivity, dec)
                        )
                    )
                }
            }
            is Event.Decrypt.GetInput -> {
                event.callbackScope.launch {
                    val inputUri: Uri? = suspendCoroutine { cont ->
                        openCallback = cont
                        openDecryptInput.launch(arrayOf("application/octet-stream"))
                    }

                    if (inputUri == null) {
                        event.callback(this, null)
                        return@launch
                    }

                    val inputFile =
                        DocumentFile.fromSingleUri(this@MainActivity, inputUri) ?: run {
                            event.callback(this, null)
                            return@launch
                        }

                    val outputUri: Uri? = suspendCoroutine { cont ->
                        openCallback = cont
                        openDecryptOutput.launch(
                            inputFile.name!!
                                .replace(".enc2", "")
                                .replace(".enc4", ""),
                        )
                    }

                    if (outputUri == null) {
                        event.callback(this, null)
                        return@launch
                    }

                    val outputFile =
                        DocumentFile.fromSingleUri(this@MainActivity, outputUri) ?: run {
                            event.callback(this, null)
                            return@launch
                        }

                    event.callback(
                        this,
                        DecryptFileInfo(
                            PlatformUriFile(this@MainActivity, inputFile),
                            PlatformUriFile(this@MainActivity, outputFile)
                        ),
                    )
                }
            }
            else -> {}
        }
    }
}
