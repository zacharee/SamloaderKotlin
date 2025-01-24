package tk.zwander.common.util

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Make a firmware string given the PDA and CSC versions.
 * @param pda the PDA version.
 * @param csc the CSC version.
 * @return a firmware string in the format PDA/CSC/PDA/PDA.
 */
fun makeFirmwareString(pda: String, csc: String): String {
    return "$pda/$csc/$pda/$pda"
}

/**
 * Get the HTML from Samfrew to scrape for version history.
 * @param model the device model.
 * @param region the device region.
 */
suspend fun getFirmwareHistoryString(model: String, region: String): String? {
    val origUrl = "https://samfrew.com/firmware/model/${model}/region/${region}/upload/Desc/0/1000"

    val response = globalHttpClient.get {
        userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36")
        url(origUrl)
    }

    return if (response.status.isSuccess()) response.bodyAsText() else null
}

suspend fun getFirmwareHistoryStringFromSamsung(model: String, region: String): String? {
    return globalHttpClient.get(
        urlString = "https://fota-cloud-dn.ospserver.net:443/firmware/${region}/${model}/version.xml",
    ) {
        userAgent("Kies2.0_FUS")
    }.run { if (status.isSuccess()) bodyAsText() else null }
}
