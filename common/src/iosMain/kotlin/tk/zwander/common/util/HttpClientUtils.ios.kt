package tk.zwander.common.util

import com.linroid.kdown.sqlite.DriverFactory

actual val kdownDb: DriverFactory
    get() = DriverFactory()