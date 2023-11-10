package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import korlibs.memory.Platform
import tk.zwander.common.util.UrlHandler
import tk.zwander.commonCompose.util.rememberIsOverScaledThreshold
import tk.zwander.samloaderkotlin.resources.MR
import tk.zwander.samloaderkotlin.strings

val options = arrayListOf<Pair<String, String>>().apply {
    if (Platform.isJvm && !Platform.isAndroid) {
        add(strings.useNativeFilePicker() to "useNativeFileDialog")
    }
}

/**
 * The footer shown on all pages.
 */
@Composable
fun FooterView(
    modifier: Modifier = Modifier
) {
    var showingSupportersDialog by remember { mutableStateOf(false) }
    var showingSettings by remember { mutableStateOf(false) }
    var showingAboutDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val overThreshold = rememberIsOverScaledThreshold(constraints.maxWidth, 600)

        Row(
            modifier = modifier.fillMaxWidth()
                .padding(8.dp)
        ) {
            if (overThreshold) {
                AboutInfo()

                Spacer(Modifier.weight(1f))
            }

            LazyRow(
                modifier = Modifier.align(Alignment.Bottom).then(
                    if (overThreshold) {
                        Modifier
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ),
                horizontalArrangement = if (overThreshold) Arrangement.Start else Arrangement.SpaceEvenly
            ) {
                if (!overThreshold) {
                    item {
                        IconButton(
                            onClick = {
                                showingAboutDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = strings.about()
                            )
                        }
                    }
                }

                item {
                    IconButton(
                        onClick = {
                            showingSupportersDialog = true
                        }
                    ) {
                        Icon(
                            painterResource(MR.images.heart), strings.supporters(),
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }
                }

                item {
                    IconButton(
                        onClick = {
                            UrlHandler.launchUrl("https://github.com/zacharee/SamloaderKotlin")
                        }
                    ) {
                        Icon(
                            painterResource(MR.images.github), strings.github(),
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }
                }

                item {
                    IconButton(
                        onClick = {
                            UrlHandler.launchUrl("https://androiddev.social/@wander1236")
                        },
                    ) {
                        Icon(
                            painterResource(MR.images.mastodon), strings.mastodon(),
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }
                }

                item {
                    IconButton(
                        onClick = {
                            UrlHandler.launchUrl("https://twitter.com/wander1236")
                        },
                    ) {
                        Icon(
                            painterResource(MR.images.twitter), strings.twitter(),
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }
                }

                item {
                    IconButton(
                        onClick = {
                            UrlHandler.launchUrl("https://patreon.com/zacharywander")
                        },
                    ) {
                        Icon(
                            painterResource(MR.images.patreon), strings.patreon(),
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }
                }

                if (options.isNotEmpty()) {
                    item {
                        IconButton(
                            onClick = {
                                showingSettings = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(MR.images.cog),
                                contentDescription = strings.settings(),
                                modifier = Modifier.padding(8.dp).size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    PatreonSupportersDialog(showingSupportersDialog) {
        showingSupportersDialog = false
    }

    SettingsDialog(showingSettings, options) {
        showingSettings = false
    }

    AlertDialogDef(
        showing = showingAboutDialog,
        onDismissRequest = { showingAboutDialog = false },
        title = {
            Text(text = strings.about())
        },
        text = {
            AboutInfo()
        },
        buttons = {
            TextButton(onClick = { showingAboutDialog = false }) {
                Text(text = strings.ok())
            }
        }
    )
}
