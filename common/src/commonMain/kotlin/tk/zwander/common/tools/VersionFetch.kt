package tk.zwander.common.tools

import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.stream.readAll

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

        val responseString = response.content.readAll().decodeToString()
        val responseXml = Xml(responseString)

        if (responseXml.name == "Error") {
            val code = responseXml.child("Code")!!.text
            val message = responseXml.child("Message")!!.text

            throw IllegalStateException("Code: ${code}, Message: $message")
        }

        val latest = responseXml.child("firmware")
            ?.child("version")
            ?.child("latest")!!

        val vc = latest.text.split("/").toMutableList()

        if (vc.size == 3) {
            vc.add(vc[0])
        }
        if (vc[2] == "") {
            vc[2] = vc[0]
        }

        return vc.joinToString("/") to (latest.attribute("o") ?: "")
    }
}