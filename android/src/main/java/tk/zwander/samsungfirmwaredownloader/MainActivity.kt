package tk.zwander.samsungfirmwaredownloader

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import moe.tlaster.kfilepicker.FilePicker
import tk.zwander.commonCompose.MainView
import tk.zwander.common.data.*
import kotlin.time.ExperimentalTime

/**
 * The Activity to show the downloader UI.
 */
@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    /**
     * Set whenever the DownloaderService needs to select a file or folder.
     * Called once the user makes a selection.
     */
    private var openCallback: IOpenCallback? = null

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
        openCallback?.onOpen(it)
    }

    private val openDecryptInput = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        openCallback?.onOpen(it)
    }

    private val openDecryptOutput = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        openCallback?.onOpen(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FilePicker.init(activityResultRegistry, this, contentResolver)

        //Start the DownloaderService.
        DownloaderService.start(this, object : IMainActivity.Stub() {
            override fun openDecryptInput(callback: IOpenCallback) {
                openCallback = callback
                openDecryptInput.launch(arrayOf("application/octet-stream"))
            }

            override fun openDecryptOutput(fileName: String, callback: IOpenCallback) {
                openCallback = callback
                openDecryptOutput.launch(fileName)
            }

            override fun openDownloadTree(callback: IOpenCallback) {
                openCallback = callback
                openDownloadTree.launch(null)
            }
        })

        //Set up windowing stuff.
        supportActionBar?.hide()
        window.statusBarColor = Color.parseColor("#${primary}")
        window.navigationBarColor = Color.parseColor("#${background}")

        //Set the Compose content.
        setContent {
            MainView()
        }
    }

}