package tk.zwander.common.util

import korlibs.platform.Platform

data class OSVersion(
    val major: Int?,
    val minor: Int?,
    val patch: Int?,
)

expect val hostOsVersion: OSVersion

val isWindows11: Boolean
    get() = Platform.isWindows && (hostOsVersion.major ?: 0) >= 11
