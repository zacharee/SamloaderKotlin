package tk.zwander.entry

import moe.tlaster.precompose.PreComposeWindow
import tk.zwander.common.GradleConfig

fun main() {
    entry()
}

internal fun entry() {
    PreComposeWindow(GradleConfig.appName) {
        View()
    }
}
