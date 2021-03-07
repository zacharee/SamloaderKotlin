package tk.zwander.samsungfirmwaredownloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.documentfile.provider.DocumentFile
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.util.*
import tk.zwander.common.view.pages.PlatformDecryptView
import tk.zwander.common.view.pages.PlatformDownloadView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DownloaderService : Service() {
    companion object {
        const val EXTRA_ACTIVITY_CALLBACK = "activity_callback"

        fun start(context: Context, callback: IMainActivity) {
            val startIntent = Intent(context, DownloaderService::class.java)
            startIntent.putBinder(EXTRA_ACTIVITY_CALLBACK, callback.asBinder())

            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stop(context: Context) {
            val stopIntent = Intent(context, DownloaderService::class.java)

            context.stopService(stopIntent)
        }
    }

    private var activityCallback: IMainActivity? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        activityCallback = IMainActivity.Stub.asInterface(
            intent.getBinder(EXTRA_ACTIVITY_CALLBACK)
        )
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(NotificationChannel("progress", getString(R.string.notification_progress_channel_name),
                    NotificationManager.IMPORTANCE_LOW))
        }

        val foregroundNotification = NotificationCompat.Builder(this, "progress")
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_progress_text))
            .setSmallIcon(IconCompat.createWithBitmap(imageFromResource("download.png")))
            .build()

        startForeground(100, foregroundNotification)

        //TODO: This is an absolute mess and hopefully there will be a way to
        //TODO: fix it in the future.
        PlatformDownloadView.getInputCallback = input@{ fileName, callback ->
            var inputUri: Uri? = null

            suspendCoroutine<Unit> { cont ->
                activityCallback?.openDownloadTree(object : IOpenCallback.Stub() {
                    override fun onOpen(uri: Uri?) {
                        inputUri = uri
                        cont.resume(Unit)
                    }
                })
            }

            if (inputUri == null) return@input

            contentResolver.takePersistableUriPermission(
                inputUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            val dir = DocumentFile.fromTreeUri(this@DownloaderService, inputUri!!) ?: return@input
            val decName = fileName.replace(".enc2", "")
                .replace(".enc4", "")

            val enc = dir.findFile(fileName) ?: dir.createFile("application/octet-stream", fileName)
            ?: return@input
            val dec =
                dir.findFile(decName) ?: dir.createFile("application/zip", decName) ?: return@input

            val output = contentResolver.openOutputStream(enc.uri, "wa").inputAsync()
            val input = { contentResolver.openInputStream(enc.uri).inputAsync() }
            val decOutput = contentResolver.openOutputStream(dec.uri).inputAsync()

            callback(
                DownloadFileInfo(
                    inputUri.toString(),
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
                activityCallback?.openDecryptInput(object : IOpenCallback.Stub() {
                    override fun onOpen(uri: Uri?) {
                        inputUri = uri
                        cont.resume(Unit)
                    }
                })
            }

            if (inputUri == null) return@input
            val inputFile =
                DocumentFile.fromSingleUri(this@DownloaderService, inputUri!!) ?: return@input

            suspendCoroutine<Unit> { cont ->
                activityCallback?.openDecryptOutput(
                    inputFile.name!!
                        .replace(".enc2", "")
                        .replace(".enc4", ""),
                    object : IOpenCallback.Stub() {
                        override fun onOpen(uri: Uri?) {
                            outputUri = uri
                            cont.resume(Unit)
                        }
                    })
            }

            val outputFile =
                DocumentFile.fromSingleUri(this@DownloaderService, outputUri!!) ?: return@input

            val output = contentResolver.openOutputStream(outputFile.uri, "w").inputAsync()
            val input = contentResolver.openInputStream(inputFile.uri).inputAsync()
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

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return object : IDownloaderService.Stub() {
            override fun nuffin() {}
        }
    }
}