package tk.zwander.commonCompose.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.kmp.platform.HostOS
import tk.zwander.common.util.UrlHandler
import tk.zwander.samloaderkotlin.resources.MR

@Composable
fun FrameWindowScope.MacMenuBar(
    mainWindowState: WindowState,
    applicationScope: ApplicationScope,
) {
    if (HostOS.current == HostOS.MacOS) {
        MenuBar {
            Menu(
                text = stringResource(MR.strings.window),
            ) {
                Item(
                    text = stringResource(MR.strings.minimize),
                    onClick = {
                        mainWindowState.isMinimized = true
                    },
                    shortcut = KeyShortcut(Key.M, meta = true),
                )

                Item(
                    text = stringResource(MR.strings.zoom),
                    onClick = {
                        mainWindowState.placement = WindowPlacement.Maximized
                    },
                )

                Item(
                    text = stringResource(MR.strings.close),
                    onClick = {
                        applicationScope.exitApplication()
                    },
                    shortcut = KeyShortcut(Key.W, meta = true),
                )
            }

            Menu(
                text = stringResource(MR.strings.help),
            ) {
                Item(
                    text = stringResource(MR.strings.github),
                    onClick = {
                        UrlHandler.launchUrl("https://github.com/zacharee/SamloaderKotlin")
                    },
                )

                Item(
                    text = stringResource(MR.strings.mastodon),
                    onClick = {
                        UrlHandler.launchUrl("https://androiddev.social/@wander1236")
                    },
                )

                Item(
                    text = stringResource(MR.strings.patreon),
                    onClick = {
                        UrlHandler.launchUrl("https://patreon.com/zacharywander")
                    },
                )
            }
        }
    }
}
