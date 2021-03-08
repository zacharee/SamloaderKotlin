package tk.zwander.common.tools

import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.stream.readAll

/**
 * Delegate XML creation to the platform until there's a proper MPP library.
 */
expect object PlatformVersionFetch {
    suspend fun getLatestVersion(model: String, region: String, response: String): Pair<String, String>
}

/**
 * Handle fetching the latest version for a given model and region.
 */
object VersionFetch {
    /**
     * Get the latest firmware version for a given model and region.
     * @param model the device model.
     * @param region the device region.
     * @return a Pair(FirmwareString, AndroidVersion).
     */
    suspend fun getLatestVersion(model: String, region: String): Pair<String, String> {
        val client = HttpClient()
        val response = client.request(
            Http.Method.GET,
            "https://fota-cloud-dn.ospserver.net/firmware/${region}/${model}/version.xml"
        )

        return PlatformVersionFetch.getLatestVersion(model, region, response.content.readAll().decodeToString())
    }
}