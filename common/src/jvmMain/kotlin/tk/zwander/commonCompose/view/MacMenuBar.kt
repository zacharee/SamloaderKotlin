package tk.zwander.commonCompose.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import korlibs.memory.Platform
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR


@Composable
fun FrameWindowScope.MacMenuBar(
    mainWindowState: WindowState,
    applicationScope: ApplicationScope,
) {
    if (Platform.isMac) {
        MenuBar {
            Menu(
                text = MR.strings.window()
            ) {
                Item(
                    text = MR.strings.minimize(),
                    onClick = {
                        mainWindowState.isMinimized = true
                    },
                    shortcut = KeyShortcut(Key.M, meta = true)
                )

                Item(
                    text = MR.strings.zoom(),
                    onClick = {
                        mainWindowState.placement = WindowPlacement.Maximized
                    }
                )

                Item(
                    text = MR.strings.close(),
                    onClick = {
                        applicationScope.exitApplication()
                    },
                    shortcut = KeyShortcut(Key.W, meta = true)
                )
            }

            Menu(
                text = MR.strings.help()
            ) {
                Item(
                    text = MR.strings.github(),
                    onClick = {
                        UrlHandler.launchUrl("https://github.com/zacharee/SamloaderKotlin")
                    }
                )

                Item(
                    text = MR.strings.mastodon(),
                    onClick = {
                        UrlHandler.launchUrl("https://androiddev.social/@wander1236")
                    }
                )

                Item(
                    text = MR.strings.twitter(),
                    onClick = {
                        UrlHandler.launchUrl("https://twitter.com/wander1236")
                    }
                )

                Item(
                    text = MR.strings.patreon(),
                    onClick = {
                        UrlHandler.launchUrl("https://patreon.com/zacharywander")
                    }
                )
            }
        }
    }
}
