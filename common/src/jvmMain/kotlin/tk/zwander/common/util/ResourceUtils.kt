package tk.zwander.common.util

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format

actual operator fun StringResource.invoke(vararg args: Any): String {
    return if (args.isNotEmpty()) {
        format(*args)
    } else {
        this.desc()
    }.localized()
}

actual operator fun FileResource.invoke(): ByteArray? {
    return resourcesClassLoader.getResourceAsStream(filePath)?.use { it.readBytes() }
}
