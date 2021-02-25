package tools

import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object VersionFetch {
    private val client = HttpClient.newBuilder()
        .build()

    fun getLatestVer(model: String, region: String): String {
        val request = HttpRequest.newBuilder(
            URI(
                "https://fota-cloud-dn.ospserver.net/firmware/${region}/${model}/version.xml"
            )
        ).GET().build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        val root = SAXBuilder().build(response.body().byteInputStream())

        val verCode = root.getContent(Filters.element("versioninfo"))[0]
            .getContent(Filters.element("firmware"))[0]
            .getContent(Filters.element("version"))[0]
            .getContent(Filters.element("latest"))[0].text
            ?: throw IllegalStateException("No version code")

        val vc = verCode.split("/").toMutableList()
        if (vc.size == 3) {
            vc.add(vc[0])
        }
        if (vc[2] == "") {
            vc[2] = vc[0]
        }

        return vc.joinToString("/")
    }
}