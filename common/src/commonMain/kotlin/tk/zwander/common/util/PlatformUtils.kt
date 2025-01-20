package tk.zwander.common.util

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

data class OSVersion(
    val major: Int?,
    val minor: Int?,
    val patch: Int?,
)

expect val hostOsVersion: OSVersion

val isWindows11: Boolean
    get() = hostOs == OS.Windows && (hostOsVersion.major ?: 0) >= 11
