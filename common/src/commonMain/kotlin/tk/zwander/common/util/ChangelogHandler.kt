package tk.zwander.common.util

import androidx.compose.ui.text.intl.Locale
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.MalformedInputException
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.data.changelog.Changelogs

object ChangelogHandler {
    private const val DOMAIN_URL = "https://doc.samsungmobile.com:443"

    suspend fun getChangelog(device: String, region: String, firmware: String): Changelog? {
        return getChangelogs(device, region)?.changelogs?.get(firmware)
    }

    suspend fun getChangelogs(device: String, region: String): Changelogs? {
        try {
            val outerUrl = generateUrlForDeviceAndRegion(device, region)
            val outerResponse = try {
                globalHttpClient.get(outerUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

            val iframeUrl = if (outerResponse.status.isSuccess()) {
                parseDocUrl(outerResponse.bodyAsText())
                    ?.replace("../../", "$DOMAIN_URL/")
            } else {
                println("No changelogs found for $device $region")
                return null
            }

            val iframeResponse = globalHttpClient.get(iframeUrl ?: return null)

            return if (iframeResponse.status.isSuccess()) {
                Changelogs(device, region, parseChangelogs(
                    iframeResponse.bodyAsText()
                ))
            } else {
                println("Unable to load changelogs for $device $region")
                null
            }
        } catch (e: Throwable) {
            if (e !is MalformedInputException) {
                CrossPlatformBugsnag.notify(e)
            }
            return null
        }
    }

    private fun generateUrlForDeviceAndRegion(device: String, region: String): String {
        return "$DOMAIN_URL/${device}/${region}/doc.html"
    }

    private fun parseDocUrl(body: String): String? {
        val doc = Ksoup.parse(body)
        val selector = doc.selectFirst("#sel_lang_hidden")
        val currentLocale = Locale.current.toLanguageTag().uppercase()
        val localeOption = selector?.children()?.run {
            find {
                val value = it.attr("value")
                currentLocale.startsWith(value)
            } ?: find {
                it.attr("value") == "EN"
            } ?: first()
        }

        return localeOption?.text()
    }

    private fun parseChangelogs(body: String): Map<String, Changelog> {
        val doc = try {
            Ksoup.parse(body)
        } catch (e: NullPointerException) {
            return mapOf()
        }
        val container = doc.selectFirst(".container")

        val divs = container?.children()?.apply {
            removeIf { it.tagName() == "hr" }
        } ?: return mapOf()
        val changelogs = LinkedHashMap<String, Changelog>()

        for (i in 3 until divs.size step 2) {
            val row = divs[i].children()
            val log = divs.getOrNull(i + 1)

            //This is kind of messy, but Samsung doesn't have a proper API for retrieving
            //version info. Some firmware entries don't have a security patch field, so
            //this handles that case. Some entries are in other languages, so using text
            //searching doesn't work well. It's possible some entries are missing other
            //fields, but there aren't any examples of that yet.
            val (build, androidVer, relDate, secPatch, _) = when {
                row.count() == 4 -> {
                    Changelog(
                        row.getOrNull(0)?.text()?.split(":")?.getOrNull(1)?.trim(),
                        row.getOrNull(1)?.text()?.split(":")?.getOrNull(1)?.trim(),
                        row.getOrNull(2)?.text()?.split(":")?.getOrNull(1)?.trim(),
                        row.getOrNull(3)?.text()?.split(":")?.getOrNull(1)?.trim(),
                        null
                    )
                }
                row.count() == 3 -> {
                    Changelog(
                        row.getOrNull(0)?.text()?.split(":")?.getOrNull(1)?.trim(),
                        row.getOrNull(1)?.text()?.split(":")?.getOrNull(1)?.trim(),
                        row.getOrNull(2)?.text()?.split(":")?.getOrNull(1)?.trim(),
                        null, null
                    )
                }
                else -> {
                    Changelog(null, null, null, null, null)
                }
            }

            val logText = log?.children()?.getOrNull(0)?.childNodes()?.joinToString(
                separator = "",
                transform = {
                    it.outerHtml().lines().joinToString("\n") { line ->
                        if (line.startsWith(" ")) {
                            line.replaceFirst(" ", "")
                        } else {
                            line
                        }
                    }
                },
            )

            if (build != null) {
                changelogs[build] = Changelog(
                    build, androidVer, relDate, secPatch, logText,
                )
            }
        }

        return changelogs
    }
}
