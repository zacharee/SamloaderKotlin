package tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object VersionFetch {
    private val client = HttpClient.newBuilder()
        .build()

    suspend fun getLatestVer(model: String, region: String): String {
        return withContext(Dispatchers.IO) {
            val request = HttpRequest.newBuilder(
                URI(
                    "https://fota-cloud-dn.ospserver.net/firmware/${region}/${model}/version.xml"
                )
            ).GET().build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            val root = SAXBuilder().build(response.body().byteInputStream())

            val error = root.getContent(Filters.element("Error"))

            if (error.isNotEmpty()) {
                val code = error[0].getContent(Filters.element("Code"))[0].text
                val message = error[0].getContent(Filters.element("Message"))[0].text

                throw IllegalStateException("Code: ${code}, Message: $message")
            }

            val verCode = root.getContent(Filters.element("versioninfo"))[0]
                .getContent(Filters.element("firmware"))[0]
                .getContent(Filters.element("version"))[0]
                .getContent(Filters.element("latest"))[0].text

            if (verCode.isNullOrBlank()) {
                throw IllegalStateException("No firmware found for $model, $region.")
            }

            val vc = verCode.split("/").toMutableList()

            if (vc.size == 3) {
                vc.add(vc[0])
            }
            if (vc[2] == "") {
                vc[2] = vc[0]
            }

            vc.joinToString("/")
        }
    }
}