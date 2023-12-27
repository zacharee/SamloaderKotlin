package tk.zwander.common.util

import androidx.compose.runtime.Composable

data class PhoneInfo(
    val tac: String?,
    val model: String,
)

@Composable
expect fun rememberPhoneInfo(): PhoneInfo?
