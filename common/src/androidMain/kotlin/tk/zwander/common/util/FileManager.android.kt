package tk.zwander.common.util

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.PlatformUriFile
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import tk.zwander.samsungfirmwaredownloader.App
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual object FileManager {
    private val openDownloadTreeContinuation: AtomicRef<Continuation<Uri?>?> = atomic(null)
    private val openFileContinuation: AtomicRef<Continuation<Uri?>?> = atomic(null)
    private val saveFileContinuation: AtomicRef<Continuation<Uri?>?> = atomic(null)

    private val openDownloadTreeLauncher: AtomicRef<ActivityResultLauncher<Uri?>?> = atomic(null)
    private val openFileLauncher: AtomicRef<ActivityResultLauncher<Array<String>>?> = atomic(null)
    private val saveFileLauncher: AtomicRef<ActivityResultLauncher<String>?> = atomic(null)

    fun init(activity: ComponentActivity) {
        openDownloadTreeLauncher.value = activity.registerForActivityResult(
            object : ActivityResultContract<Uri?, Uri?>() {
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
            openDownloadTreeContinuation.getAndSet(null)?.resume(it)
        }
        openFileLauncher.value =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
                openFileContinuation.getAndSet(null)?.resume(it)
            }
        saveFileLauncher.value =
            activity.registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) {
                saveFileContinuation.getAndSet(null)?.resume(it)
            }
    }

    actual suspend fun pickFile(): IPlatformFile? {
        return suspendCoroutine {
            openFileContinuation.value = it
            openFileLauncher.value?.launch(arrayOf("application/octet-stream"))
        }?.let { PlatformUriFile(App.instance, it, false) }
    }

    actual suspend fun pickDirectory(): IPlatformFile? {
        return suspendCoroutine {
            openDownloadTreeContinuation.value = it
            openDownloadTreeLauncher.value?.launch(null)
        }?.also {
            App.instance.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }?.let { PlatformUriFile(App.instance, it, true) }
    }

    actual suspend fun saveFile(name: String): IPlatformFile? {
        return suspendCoroutine {
            saveFileContinuation.value = it
            saveFileLauncher.value?.launch(
                name.replace(".enc2", "")
                    .replace(".enc4", ""),
            )
        }?.let { PlatformUriFile(App.instance, it, false) }
    }
}