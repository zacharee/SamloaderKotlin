package org.jsoup

import com.soywiz.korio.lang.format

fun String.Companion.format(value: String, vararg args: Any?): String {
    return value.format(args.map { it.toString() }.toTypedArray())
}

val Char.charCount: Int
    get() = if (code >= 0x10000) 2 else 1
