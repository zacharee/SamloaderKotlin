package tk.zwander.common.util

import android.content.Intent
import androidx.core.content.FileProvider
import io.github.z4kn4fein.semver.toVersion
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import io.ktor.util.cio.use
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyTo
import org.kohsuke.github.GitHub
import tk.zwander.common.GradleConfig
import tk.zwander.samsungfirmwaredownloader.App
import java.io.File

actual object UpdateUtil {
    actual suspend fun checkForUpdate(): UpdateInfo? {
        return try {
            val github = GitHub.connectAnonymously()
            val repo = github.getRepository("zacharee/SamloaderKotlin")

            val latestVersion = repo.latestRelease.tagName
            val currentVersion = GradleConfig.versionName

            if (currentVersion.toVersion() >= latestVersion.toVersion()) {
                return null
            } else {
                return UpdateInfo(latestVersion)
            }
        } catch (e: Throwable) {
            CrossPlatformBugsnag.notify(e)
            null
        }
    }

    actual suspend fun installUpdate() {
        try {
            val github = GitHub.connectAnonymously()
            val repo = github.getRepository("zacharee/SamloaderKotlin")
            val assets = repo.latestRelease.listAssets().toList()

            val apk = assets.find { it.name.endsWith(".apk") } ?: return
            val downloadUrl = apk.browserDownloadUrl ?: return

            val destinationFile = File(App.instance.cacheDir, "updates/${apk.name}")
            destinationFile.delete()
            destinationFile.parentFile?.mkdirs()

            val request = globalHttpClient.prepareRequest {
                method = HttpMethod.Get
                url(downloadUrl)
            }

            request.execute { response ->
                val channel = response.bodyAsChannel()

                destinationFile.writeChannel().use { channel.copyTo(this) }
            }

            @Suppress("DEPRECATION")
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            intent.data = FileProvider.getUriForFile(
                App.instance,
                "tk.zwander.samsungfirmwaredownloader.fileprovider",
                destinationFile,
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance.startActivity(intent)
        } catch (e: Throwable) {
            CrossPlatformBugsnag.notify(e)
        }
    }
}