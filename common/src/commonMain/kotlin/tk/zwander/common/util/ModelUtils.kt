package tk.zwander.common.util

val String?.isAccessoryModel: Boolean
    get() = this?.startsWith("SM-R") == true
