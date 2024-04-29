package tk.zwander.common.util

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class SupporterInfo(
    val name: String,
    val link: String
)

class PatreonSupportersParser private constructor() {
    companion object {
        @Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")
        private var instance: PatreonSupportersParser? = null

        fun getInstance(): PatreonSupportersParser {
            return instance ?: PatreonSupportersParser().also {
                instance = it
            }
        }
    }

    suspend fun parseSupporters(): List<SupporterInfo> {
        val supportersString = StringBuilder()

        withContext(Dispatchers.IO) {
            try {
                val statement = globalHttpClient.get("https://raw.githubusercontent.com/zacharee/PatreonSupportersRetrieval/master/app/src/main/assets/supporters.json")

                supportersString.append(statement.bodyAsText())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Json.decodeFromString(ListSerializer(SupporterInfo.serializer()), supportersString.toString())
    }
}
