package tk.zwander.common.util

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
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
        private var instance: PatreonSupportersParser? = null

        fun getInstance(): PatreonSupportersParser {
            return instance ?: PatreonSupportersParser().also {
                instance = it
            }
        }
    }

    suspend fun parseSupporters(): List<SupporterInfo> {
        val supportersString = StringBuilder()

        withContext(Dispatchers.Default) {
            try {
                val statement = client.use {
                    it.get<HttpStatement> {
                        url("https://raw.githubusercontent.com/zacharee/PatreonSupportersRetrieval/master/app/src/main/assets/supporters.json")
                    }
                }

                supportersString.append(statement.execute().readText())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Json.decodeFromString(ListSerializer(SupporterInfo.serializer()), supportersString.toString())
    }
}
