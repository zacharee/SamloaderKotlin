package tk.zwander.common.tools

import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.stream.readAll

expect object PlatformVersionFetch {
    suspend fun getLatestVer(model: String, region: String, response: String): Pair<String, String>
}

object VersionFetch {
    suspend fun getLatestVer(model: String, region: String): Pair<String, String> {
        val client = HttpClient()
        val response = client.request(
            Http.Method.GET,
            "https://fota-cloud-dn.ospserver.net/firmware/${region}/${model}/version.xml"
        )

        return PlatformVersionFetch.getLatestVer(model, region, response.content.readAll().decodeToString().also {
            println(it)
        })
    }
}