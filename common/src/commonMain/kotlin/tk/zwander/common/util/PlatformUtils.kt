package tk.zwander.common.util

import dev.zwander.kmp.platform.HostOS
import dev.zwander.kmp.platform.OSVersion

val isWindows11: Boolean
    get() = HostOS.current == HostOS.Windows && (OSVersion.current.major ?: 0) >= 11
