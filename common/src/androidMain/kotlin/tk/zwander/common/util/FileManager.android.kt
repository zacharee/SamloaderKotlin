package tk.zwander.common.util

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import dev.pranav.filepicker.FilePickerCallback
import dev.pranav.filepicker.FilePickerDialogFragment
import dev.pranav.filepicker.FilePickerOptions
import dev.pranav.filepicker.SelectionMode
import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.PlatformFile
import dev.zwander.kotlin.file.PlatformUriFile
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.suspendCancellableCoroutine
import tk.zwander.samsungfirmwaredownloader.App
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

actual object FileManager {
    private val openDownloadTreeContinuation: AtomicRef<Continuation<Uri?>?> = atomic(null)
    private val openFileContinuation: AtomicRef<Continuation<Uri?>?> = atomic(null)
    private val saveFileContinuation: AtomicRef<Continuation<Uri?>?> = atomic(null)
    private val openRealDirectoryContinuation: AtomicRef<Continuation<File?>?> = atomic(null)
    private val openRealFileContinuation: AtomicRef<Continuation<File?>?> = atomic(null)

    private val openDocumentTreeLauncher: AtomicRef<ActivityResultLauncher<Uri?>?> = atomic(null)
    private val openFileLauncher: AtomicRef<ActivityResultLauncher<Array<String>>?> = atomic(null)
    private val saveFileLauncher: AtomicRef<ActivityResultLauncher<String>?> = atomic(null)
    private val openRealDirectoryLauncher: AtomicRef<(() -> Unit)?> = atomic(null)
    private val openRealFileLauncher: AtomicRef<(() -> Unit)?> = atomic(null)

    fun init(activity: FragmentActivity) {
        openDocumentTreeLauncher.value = activity.registerForActivityResult(
            object : ActivityResultContract<Uri?, Uri?>() {
                override fun createIntent(context: Context, input: Uri?): Intent {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    if (input != null) {
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

        openRealDirectoryLauncher.value = {
            val options = FilePickerOptions().apply {
                selectionMode = SelectionMode.FOLDER
            }

            val callback = object : FilePickerCallback() {
                override fun onFileSelected(file: File) {
                    openRealDirectoryContinuation.value?.resume(file)
                }

                override fun onFileSelectionCancelled(): Boolean {
                    openRealDirectoryContinuation.value?.resume(null)
                    return true
                }
            }

            FilePickerDialogFragment(options, callback).show(activity.supportFragmentManager, "directoryPicker")
        }

        openRealFileLauncher.value = {
            val options = FilePickerOptions().apply {
                selectionMode = SelectionMode.FILE
            }

            val callback = object : FilePickerCallback() {
                override fun onFileSelected(file: File) {
                    openRealFileContinuation.value?.resume(file)
                }

                override fun onFileSelectionCancelled(): Boolean {
                    openRealFileContinuation.value?.resume(null)
                    return true
                }
            }

            FilePickerDialogFragment(options, callback).show(activity.supportFragmentManager, "filePicker")
        }
    }

    private fun shouldUseFileFramework(): Boolean {
        return App.instance.hasExternalStorage && BifrostSettings.Keys.useFileFramework.getValue()
    }

    actual suspend fun pickFile(): IPlatformFile? {
        return if (shouldUseFileFramework()) {
            suspendCancellableCoroutine {
                openRealFileContinuation.value = it
                openRealFileLauncher.value?.invoke()
            }?.let { PlatformFile(it) }
        } else {
            suspendCancellableCoroutine {
                openFileContinuation.value = it
                openFileLauncher.value?.launch(arrayOf("application/octet-stream"))
            }?.let { PlatformUriFile(App.instance, it, false) }
        }
    }

    actual suspend fun pickDirectory(): IPlatformFile? {
        return if (shouldUseFileFramework()) {
            suspendCancellableCoroutine {
                openRealDirectoryContinuation.value = it
                openRealDirectoryLauncher.value?.invoke()
            }?.let { PlatformFile(it) }
        } else {
            suspendCancellableCoroutine {
                openDownloadTreeContinuation.value = it
                openDocumentTreeLauncher.value?.launch(null)
            }?.also {
                App.instance.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }?.let { PlatformUriFile(App.instance, it, true) }
        }
    }

    actual suspend fun saveFile(name: String): IPlatformFile? {
        return if (shouldUseFileFramework()) {
            suspendCancellableCoroutine {
                openRealDirectoryContinuation.value = it
                openRealDirectoryLauncher.value?.invoke()
            }?.let { PlatformFile(it, name) }
        } else {
            suspendCancellableCoroutine {
                saveFileContinuation.value = it
                saveFileLauncher.value?.launch(
                    name.replace(".enc2", "")
                        .replace(".enc4", ""),
                )
            }?.let { PlatformUriFile(App.instance, it, false) }
        }
    }

    actual suspend fun getTempDirectory(): IPlatformFile? {
        return if (shouldUseFileFramework()) {
            null
        } else {
            PlatformFile(App.instance.cacheDir, "downloadTmp").also {
                it.mkdirs()
            }
        }
    }
}