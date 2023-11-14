package tk.zwander.common.util

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import tk.zwander.samsungfirmwaredownloader.App

actual operator fun StringResource.invoke(vararg args: Any): String {
    return if (args.isNotEmpty()) {
        format(*args)
    } else {
        this.desc()
    }.toString(App.instance!!)
}
