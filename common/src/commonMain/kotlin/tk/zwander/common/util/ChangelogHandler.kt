package tk.zwander.common.util

import com.soywiz.korio.lang.format
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.data.changelog.Changelogs

expect object PlatformChangelogHandler {
    suspend fun parseDocUrl(body: String): String
    suspend fun parseChangelogs(body: String): Map<String, Changelog>
}

object ChangelogHandler {
    private const val DOMAIN_URL = "https://doc.samsungmobile.com"
    private const val BASE_URL = "$DOMAIN_URL/%s/%s/doc.html"

    suspend fun getChangelog(device: String, region: String, firmware: String): Changelog? {
        return getChangelogs(device, region)?.changelogs?.get(firmware)
    }

    @OptIn(InternalAPI::class)
    suspend fun getChangelogs(device: String, region: String): Changelogs? {
        val outerUrl = generateUrlForDeviceAndRegion(device, region)
        val client = HttpClient {
            followRedirects = true
        }

        val outerResponse = client.get<HttpResponse> {
            url(outerUrl)
        }

        val iframeUrl = if (outerResponse.status.isSuccess()) {
            PlatformChangelogHandler.parseDocUrl(outerResponse.readText())
                .replace("../../", "$DOMAIN_URL/")
        } else return null

        val iframeResponse = client.get<HttpResponse> {
            url(iframeUrl)
        }

        return if (iframeResponse.status.isSuccess()) {
            Changelogs(device, region, PlatformChangelogHandler.parseChangelogs(
                iframeResponse.readText()
            ))
        } else null
    }

    private fun generateUrlForDeviceAndRegion(device: String, region: String): String {
        return BASE_URL.format(device, region)
    }
}