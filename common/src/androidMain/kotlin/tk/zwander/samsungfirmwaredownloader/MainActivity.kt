package tk.zwander.samsungfirmwaredownloader

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import tk.zwander.common.IDownloaderService
import tk.zwander.common.util.FileManager
import tk.zwander.common.util.LocalPhoneInfo
import tk.zwander.common.util.rememberPhoneInfo
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime

/**
 * The Activity to show the downloader UI.
 */
@ExperimentalTime
class MainActivity : ComponentActivity(), CoroutineScope by MainScope(), ServiceConnection {
    private val downloaderService = atomic<IDownloaderService?>(null)

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FileKit.init(this)
        FileManager.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkCallingOrSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
}
