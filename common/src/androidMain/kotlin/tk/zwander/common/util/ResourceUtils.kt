package tk.zwander.common.util

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import tk.zwander.samsungfirmwaredownloader.App

actual operator fun StringResource.invoke(vararg args: Any): String {
    return if (args.isNotEmpty()) {
        format(*args)
    } else {
        this.desc()
    }.toString(App.instance)
}

actual operator fun FileResource.invoke(): ByteArray? {
    return App.instance.resources.openRawResource(rawResId).use { it.readBytes() }
}
