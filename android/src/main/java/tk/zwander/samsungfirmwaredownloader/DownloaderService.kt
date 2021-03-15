package tk.zwander.samsungfirmwaredownloader

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformUriFile
import tk.zwander.common.util.*
import tk.zwander.common.view.pages.PlatformDecryptView
import tk.zwander.common.view.pages.PlatformDownloadView
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

/**
 * A Service to manage downloading and decrypting on Android. This is used mostly to just keep the
 * app running when it's in the background. It will run as long as the app is running.
 *
 * TODO: Maybe this should be completely separate from the Activity, so the app can be cleared
 * TODO: from Recents and still function?
 */
@OptIn(ExperimentalTime::class)
class DownloaderService : Service() {
    companion object {
        const val EXTRA_ACTIVITY_CALLBACK = "activity_callback"

        /**
         * Start the Service.
         * @param context a Context object.
         * @param callback the MainActivity callback.
         */
        fun start(context: Context, callback: IMainActivity) {
            val startIntent = Intent(context, DownloaderService::class.java)
            startIntent.putBinder(EXTRA_ACTIVITY_CALLBACK, callback.asBinder())

            ContextCompat.startForegroundService(context, startIntent)
        }

        /**
         * Stop the Service.
         * @param context a Context object.
         */
        fun stop(context: Context) {
            val stopIntent = Intent(context, DownloaderService::class.java)

            context.stopService(stopIntent)
        }
    }

    /**
     * Used to communicate with MainActivity.
     */
    private var activityCallback: IMainActivity? = null
    private var runningJobs = 0
        set(value) {
            field = value

            if (value == 0) {
                nm.notify(100, makeForegroundNotification(null))
            }

            if (value == 0 && !activityRunning) {
                nm.notify(
                    100,
                    NotificationCompat.Builder(this, "notification")
                        .setContentTitle(getString(R.string.notification_finished_channel_name))
                        .setContentText(getString(R.string.notification_finished_channel_text))
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentIntent(
                            PendingIntent.getActivity(
                                this, 101,
                                Intent(this, MainActivity::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                        .build()
                )

                stopSelf()
            }
        }
    private var activityRunning = false

    private val nm by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
        override fun onActivityResumed(activity: Activity?) {}
        override fun onActivityPaused(activity: Activity?) {}
        override fun onActivityStopped(activity: Activity?) {}
        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

        override fun onActivityStarted(activity: Activity?) {
            if (activity is MainActivity) {
                activityRunning = true
            }
        }

        override fun onActivityDestroyed(activity: Activity?) {
            if (activity is MainActivity) {
                activityRunning = false
                if (runningJobs == 0) {
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Retrieve the callback.
        activityCallback = IMainActivity.Stub.asInterface(
            intent.getBinder(EXTRA_ACTIVITY_CALLBACK)
        )

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)

        //Create the notification channel if applicable.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            nm.createNotificationChannel(
                NotificationChannel(
                    "progress", getString(R.string.notification_progress_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    "notification", getString(R.string.notification_finished_channel_text),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        //Create the foreground notification.
        val foregroundNotification = makeForegroundNotification(null)

        //Start in the foreground.
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

            if (inputUri == null) {
                callback(null)
                return@input
            }

            contentResolver.takePersistableUriPermission(
                inputUri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            val dir = DocumentFile.fromTreeUri(this@DownloaderService, inputUri!!) ?: return@input
            val decName = fileName.replace(".enc2", "")
                .replace(".enc4", "")

            val enc = dir.findFile(fileName)
                ?: dir.createFile("application/octet-stream", fileName)
                ?: run {
                    callback(null)
                    return@input
                }
            val dec =
                dir.findFile(decName) ?: dir.createFile("application/zip", decName) ?: return@input

            callback(
                DownloadFileInfo(
                    PlatformUriFile(this@DownloaderService, enc),
                    PlatformUriFile(this@DownloaderService, dec)
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

            if (inputUri == null) {
                callback(null)
                return@input
            }
            val inputFile =
                DocumentFile.fromSingleUri(this@DownloaderService, inputUri!!) ?: run {
                    callback(null)
                    return@input
                }

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
                DocumentFile.fromSingleUri(this@DownloaderService, outputUri!!) ?: run {
                    callback(null)
                    return@input
                }

            callback(
                DecryptFileInfo(
                    PlatformUriFile(this@DownloaderService, inputFile),
                    PlatformUriFile(this@DownloaderService, outputFile)
                )
            )
        }

        PlatformDownloadView.downloadStartCallback = ::onJobStarted
        PlatformDownloadView.downloadStopCallback = ::onJobFinished
        PlatformDecryptView.decryptStartCallback = ::onJobStarted
        PlatformDecryptView.decryptStopCallback = ::onJobFinished

        PlatformDownloadView.downloadProgressCallback = ::onProgress
        PlatformDecryptView.decryptProgressCallback = ::onProgress
    }

    override fun onDestroy() {
        stopForeground(false)
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return object : IDownloaderService.Stub() {
            override fun nuffin() {}
        }
    }

    private var lastUpdate = 0L

    private fun onProgress(status: String, current: Long, max: Long) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdate > 500) {
            nm.notify(100, makeForegroundNotification(Triple(status, current, max)))
            lastUpdate = currentTime
        }
    }

    private fun onJobStarted() {
        runningJobs++
    }

    private fun onJobFinished() {
        runningJobs = max(0, runningJobs - 1)
    }

    private fun makeForegroundNotification(progress: Triple<String, Long, Long>? = null): Notification {
        return NotificationCompat.Builder(this, "progress")
            .setContentTitle(getString(R.string.app_name))
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentText(getString(R.string.notification_progress_text))
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 101,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .apply {
                if (progress != null) {
                    setStyle(NotificationCompat.DecoratedCustomViewStyle())

                    val percent =
                        ((progress.second.toDouble() / progress.third.toDouble() * 100.0 * 100.0).roundToInt() / 100.0)

                    setCustomBigContentView(
                        RemoteViews(packageName, R.layout.progress)
                            .apply {
                                setTextViewText(R.id.title, progress.first)
                                setProgressBar(R.id.progress, 100, percent.roundToInt(), false)
                                setTextViewText(R.id.progress_text, "$percent%")
                            }
                    )
                }
            }
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .build()
    }
}