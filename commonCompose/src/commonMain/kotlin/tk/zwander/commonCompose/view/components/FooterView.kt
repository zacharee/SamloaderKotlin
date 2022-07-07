package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soywiz.korio.util.OS
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.UrlHandler
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.samloaderkotlin.resources.MR
import tk.zwander.samloaderkotlin.strings

val options = arrayListOf<Pair<String, String>>().apply {
    if (OS.isJvm && !OS.isAndroid) {
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
    Box {
        var showingSupportersDialog by remember { mutableStateOf(false) }
        var showingSettings by remember { mutableStateOf(false) }

        Row(
            modifier = modifier.fillMaxWidth()
                .padding(8.dp)
        ) {
            Column {
                val copyrightAnnotated = buildAnnotatedString {
                    pushStyle(
                        SpanStyle(
                            color = LocalContentColor.current,
                            fontSize = 16.sp
                        )
                    )
                    append(strings.version("${GradleConfig.versionName} © "))
                    pushStyle(
                        SpanStyle(
                            color = MaterialTheme.colors.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    pushStringAnnotation("WebsiteLink", "https://zwander.dev")
                    append(strings.zacharyWander())
                    pop()
                }

                val samloaderAnnotated = buildAnnotatedString {
                    pushStyle(
                        SpanStyle(
                            color = LocalContentColor.current,
                            fontSize = 16.sp
                        )
                    )
                    append(strings.basedOn())
                    append(" ")
                    pushStyle(
                        SpanStyle(
                            color = MaterialTheme.colors.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    pushStringAnnotation("SamloaderLink", "https://github.com/nlscc/samloader")
                    append(strings.samloader())
                    pop()
                }

                ClickableText(
                    text = copyrightAnnotated,
                    onClick = {
                        copyrightAnnotated.getStringAnnotations("WebsiteLink", it, it)
                            .firstOrNull()?.let { item ->
                                UrlHandler.launchUrl(item.item)
                            }
                    }
                )
                Spacer(Modifier.height(4.dp))
                ClickableText(
                    text = samloaderAnnotated,
                    onClick = {
                        samloaderAnnotated.getStringAnnotations("SamloaderLink", it, it)
                            .firstOrNull()?.let { item ->
                                UrlHandler.launchUrl(item.item)
                            }
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            LazyRow(
                modifier = Modifier.align(Alignment.Bottom),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    IconButton(
                        onClick = {
                            showingSupportersDialog = true
                        }
                    ) {
                        Icon(
                            vectorResource(MR.assets.heart), strings.supporters(),
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
                            vectorResource(MR.assets.github), strings.github(),
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
                            vectorResource(MR.assets.twitter), strings.twitter(),
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
                            vectorResource(MR.assets.patreon), strings.patreon(),
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
                                painter = vectorResource(MR.assets.cog),
                                contentDescription = strings.settings(),
                                modifier = Modifier.padding(8.dp).size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showingSupportersDialog) {
            PatreonSupportersDialog {
                showingSupportersDialog = false
            }
        }

        if (showingSettings) {
            SettingsDialog(options) {
                showingSettings = false
            }
        }
    }
}
