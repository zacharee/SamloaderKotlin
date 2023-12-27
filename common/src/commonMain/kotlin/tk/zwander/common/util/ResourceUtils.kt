package tk.zwander.common.util

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.StringResource

expect operator fun StringResource.invoke(vararg args: Any): String
expect operator fun FileResource.invoke(): ByteArray?
