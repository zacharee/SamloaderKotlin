package tk.zwander.common.util

import io.ktor.client.*
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
 * Get the HTML from OdinRom to scrape for version history.
 * @param model the device model.
 * @param region the device region.
 */
suspend fun getFirmwareHistoryString(model: String, region: String): String? {
    val origUrl = "https://www.odinrom.com/samsung/${model}-${region}/"
    val client = HttpClient {
        followRedirects = true
        expectSuccess = false
    }

    val response = client.get {
        userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36")
        url(origUrl)
    }

    return if (response.status.isSuccess()) response.bodyAsText() else null
}
