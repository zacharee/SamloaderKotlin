package tk.zwander.entry

import androidx.compose.ui.window.Window
import tk.zwander.common.GradleConfig

fun main() {
    entry()
}

internal fun entry() {
    Window(GradleConfig.appName) {
        View()
    }
}
