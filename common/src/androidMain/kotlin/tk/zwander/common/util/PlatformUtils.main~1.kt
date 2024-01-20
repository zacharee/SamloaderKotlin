package tk.zwander.common.util

import android.os.Build

actual val hostOsVersion: OSVersion
    get() {
        val release = Build.VERSION.RELEASE
        val split = release.split(".")

        return OSVersion(
            major = split.getOrNull(0)?.toIntOrNull(),
            minor = split.getOrNull(1)?.toIntOrNull(),
            patch = split.getOrNull(2)?.toIntOrNull(),
        )
    }
