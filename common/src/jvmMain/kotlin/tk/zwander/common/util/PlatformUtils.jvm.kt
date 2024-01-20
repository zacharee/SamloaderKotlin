package tk.zwander.common.util

actual val hostOsVersion: OSVersion
    get() {
        val version = oshi.SystemInfo().operatingSystem.versionInfo.version
        val split = version.split(".")

        return OSVersion(
            major = split.getOrNull(0)?.toIntOrNull(),
            minor = split.getOrNull(1)?.toIntOrNull(),
            patch = split.getOrNull(2)?.toIntOrNull(),
        )
    }