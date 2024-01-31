package tk.zwander.common.util

import androidx.compose.runtime.compositionLocalOf

data class PhoneInfo(
    val tac: String?,
    val model: String,
)

val LocalPhoneInfo = compositionLocalOf<PhoneInfo?> { null }
