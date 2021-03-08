package tk.zwander.common.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder

actual object PlatformVersionFetch {
    actual suspend fun getLatestVersion(model: String, region: String, response: String): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            val root = SAXBuilder().build(response.byteInputStream())

            val error = root.getContent(Filters.element("Error"))

            if (error.isNotEmpty()) {
                val code = error[0].getContent(Filters.element("Code"))[0].text
                val message = error[0].getContent(Filters.element("Message"))[0].text

                throw IllegalStateException("Code: ${code}, Message: $message")
            }

            val (osVer, verCode) = root.getContent(Filters.element("versioninfo"))[0]
                .getContent(Filters.element("firmware"))[0]
                .getContent(Filters.element("version"))[0]
                .getContent(Filters.element("latest"))[0]
                .run {
                    getAttribute("o")?.value to text
                }

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

            vc.joinToString("/") to (osVer ?: "")
        }
    }
}
