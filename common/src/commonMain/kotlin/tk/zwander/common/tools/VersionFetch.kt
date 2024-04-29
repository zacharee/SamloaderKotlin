package tk.zwander.common.tools

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import tk.zwander.common.data.FetchResult
import tk.zwander.common.util.globalHttpClient
import tk.zwander.common.util.firstElementByTagName

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
    suspend fun getLatestVersion(model: String, region: String): FetchResult.VersionFetchResult {
        try {
            val response = globalHttpClient.get(
                urlString = "https://fota-cloud-dn.ospserver.net:443/firmware/${region}/${model}/version.xml",
            ) {
                userAgent("Kies2.0_FUS")
            }

            val responseXml = Ksoup.parse(response.bodyAsText())

            if (responseXml.tagName() == "Error") {
                val code = responseXml.firstElementByTagName("Code")!!.text()
                val message = responseXml.firstElementByTagName("Message")!!.text()

                return FetchResult.VersionFetchResult(
                    error = IllegalStateException("Code: ${code}, Message: $message"),
                    rawOutput = responseXml.toString()
                )
            }

            try {
                val latest = responseXml.firstElementByTagName("firmware")
                    ?.firstElementByTagName("version")
                    ?.firstElementByTagName("latest")!!

                val vc = latest.text().split("/").toMutableList()

                if (vc.size == 3) {
                    vc.add(vc[0])
                }
                if (vc[2] == "") {
                    vc[2] = vc[0]
                }

                return FetchResult.VersionFetchResult(
                    versionCode = vc.joinToString("/"),
                    androidVersion = latest.attribute("o")?.value ?: "",
                    rawOutput = responseXml.toString()
                )
            } catch (e: Exception) {
                return FetchResult.VersionFetchResult(
                    error = e,
                    rawOutput = responseXml.toString()
                )
            }
        } catch (e: Exception) {
            return FetchResult.VersionFetchResult(
                error = e
            )
        }
    }
}
