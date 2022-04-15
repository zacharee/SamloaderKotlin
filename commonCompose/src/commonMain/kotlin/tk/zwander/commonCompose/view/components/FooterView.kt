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
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.UrlHandler
import tk.zwander.commonCompose.util.vectorResource

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
                    append("v${GradleConfig.versionName} \u00A9 ")
                    pushStyle(
                        SpanStyle(
                            color = MaterialTheme.colors.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    pushStringAnnotation("WebsiteLink", "https://zwander.dev")
                    append("Zachary Wander")
                    pop()
                }

                val samloaderAnnotated = buildAnnotatedString {
                    pushStyle(
                        SpanStyle(
                            color = LocalContentColor.current,
                            fontSize = 16.sp
                        )
                    )
                    append("Based on ")
                    pushStyle(
                        SpanStyle(
                            color = MaterialTheme.colors.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    pushStringAnnotation("SamloaderLink", "https://github.com/nlscc/samloader")
                    append("Samloader")
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
                            vectorResource("heart.xml"), "Supporters",
                            modifier = Modifier.padding(8.dp)
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
                            vectorResource("github.xml"), "GitHub",
                            modifier = Modifier.padding(8.dp)
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
                            vectorResource("twitter.xml"), "Twitter",
                            modifier = Modifier.padding(8.dp)
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
                            vectorResource("patreon.xml"), "Patreon",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                item {
                    IconButton(
                        onClick = {
                            showingSettings = true
                        }
                    ) {
                        Icon(
                            imageVector = vectorResource("settings.xml"),
                            contentDescription = "Settings",
                            modifier = Modifier.padding(8.dp)
                        )
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
            SettingsDialog {
                showingSettings = false
            }
        }
    }
}
