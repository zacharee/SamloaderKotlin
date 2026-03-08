package tk.zwander.common.util

import com.linroid.ketch.sqlite.DriverFactory

actual val ketchDb: DriverFactory
    get() = DriverFactory()