package tk.zwander.common.util

import com.soywiz.korio.lang.format
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.readText
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.data.changelog.Changelogs

expect object PlatformChangelogHandler {
    suspend fun parseDocUrl(body: String): String?
    suspend fun parseChangelogs(body: String): Map<String, Changelog>
}

object ChangelogHandler {
    private const val DOMAIN_URL = "https://doc.samsungmobile.com:443"
    private const val BASE_URL = "$DOMAIN_URL/%s/%s/doc.html"

    suspend fun getChangelog(device: String, region: String, firmware: String, useProxy: Boolean = false): Changelog? {
        return getChangelogs(device, region, useProxy)?.changelogs?.get(firmware)
    }

    @OptIn(InternalAPI::class)
    suspend fun getChangelogs(device: String, region: String, useProxy: Boolean = false): Changelogs? {
        val outerUrl = generateUrlForDeviceAndRegion(device, region, useProxy)
        val outerResponse = try {
            client.use {
                it.get<HttpResponse> {
                    url(outerUrl)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        val iframeUrl = if (outerResponse.status.isSuccess()) {
            PlatformChangelogHandler.parseDocUrl(outerResponse.readText())
                ?.replace("../../", generateProperUrl(useProxy, "$DOMAIN_URL/"))
        } else {
            println("No changelogs found for $device $region")
            return null
        }

        val iframeResponse = client.use {
            it.get<HttpResponse> {
                url(iframeUrl ?: return null)
            }
        }

        return if (iframeResponse.status.isSuccess()) {
            Changelogs(device, region, PlatformChangelogHandler.parseChangelogs(
                iframeResponse.readText()
            ))
        } else {
            println("Unable to load changelogs for $device $region")
            null
        }
    }

    private fun generateUrlForDeviceAndRegion(device: String, region: String, useProxy: Boolean): String {
        return generateProperUrl(useProxy, BASE_URL.format(device, region))
    }
}
