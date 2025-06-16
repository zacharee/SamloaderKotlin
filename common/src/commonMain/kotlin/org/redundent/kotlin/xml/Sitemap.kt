package org.redundent.kotlin.xml

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

const val DEFAULT_URLSET_NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9"

class UrlSet internal constructor() : Node("urlset") {
	init {
		xmlns = DEFAULT_URLSET_NAMESPACE
	}

	fun url(
		loc: String,
		lastmod: LocalDateTime? = null,
		changefreq: ChangeFreq? = null,
		priority: Double? = null
	) {
		"url" {
			"loc"(loc)

			lastmod?.let {
				"lastmod"(formatDate(it))
			}

			changefreq?.let {
				"changefreq"(it.name)
			}

			priority?.let {
				"priority"(it.toString())
			}
		}
	}
}

class Sitemapindex internal constructor() : Node("sitemapindex") {
	init {
		xmlns = DEFAULT_URLSET_NAMESPACE
	}

	fun sitemap(
		loc: String,
		lastmod: LocalDateTime? = null
	) {
		"sitemap" {
			"loc"(loc)

			lastmod?.let {
				"lastmod"(formatDate(it))
			}
		}
	}
}

@Suppress("EnumEntryName", "ktlint:enum-entry-name-case")
enum class ChangeFreq {
	always,
	hourly,
	daily,
	weekly,
	monthly,
	yearly,
	never
}

private fun formatDate(date: LocalDateTime): String {
	return LocalDateTime.Format {
		year()
		char('-')
		monthNumber()
		char('-')
		dayOfMonth()
	}.format(date)
}

fun urlset(init: UrlSet.() -> Unit) = UrlSet().apply(init)

fun sitemapindex(init: Sitemapindex.() -> Unit) = Sitemapindex().apply(init)
