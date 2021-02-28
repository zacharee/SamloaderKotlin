package tk.zwander.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.imageResource

@Composable
fun FooterView() {
    Row(
        modifier = Modifier.fillMaxWidth()
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
                append("\u00A9 ")
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
                append("Code based on ")
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

            val githubAnnotated = buildAnnotatedString {
                pushStyle(
                    SpanStyle(
                        color = LocalContentColor.current,
                        fontSize = 16.sp
                    )
                )
                append("Check out the source on ")
                pushStyle(
                    SpanStyle(
                        color = MaterialTheme.colors.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
                pushStringAnnotation("GitHubLink", "https://github.com/zacharee/SamloaderKotlin")
                append("GitHub")
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
            Spacer(Modifier.height(4.dp))
            ClickableText(
                text = githubAnnotated,
                onClick = {
                    githubAnnotated.getStringAnnotations("GitHubLink", it, it)
                        .firstOrNull()?.let { item ->
                            UrlHandler.launchUrl(item.item)
                        }
                }
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.align(Alignment.Bottom)
        ) {
            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    UrlHandler.launchUrl("https://twitter.com/wander1236")
                },
            ) {
                Icon(
                    imageResource("twitter.png"), "Twitter",
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = {
                    UrlHandler.launchUrl("https://patreon.com/zacharywander")
                },
            ) {
                Icon(
                    imageResource("patreon.png"), "Patreon",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}