package tk.zwander.common.util

import dev.icerock.moko.resources.StringResource

expect operator fun StringResource.invoke(vararg args: Any): String
