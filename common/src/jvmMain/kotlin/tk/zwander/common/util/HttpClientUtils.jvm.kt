package tk.zwander.common.util

import com.linroid.kdown.sqlite.DriverFactory
import net.harawata.appdirs.AppDirsFactory
import tk.zwander.common.GradleConfig
import java.io.File

actual val kdownDb: DriverFactory
    get() = DriverFactory(
        dbPath = File(
            AppDirsFactory.getInstance().getUserDataDir(GradleConfig.appName, null, "Zachary Wander"),
            "kdown.db",
        ).absolutePath,
    )