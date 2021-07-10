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

    val response = client.get<HttpResponse> {
        url(origUrl)
    }

    return if (response.status.isSuccess()) response.readText() else null
}