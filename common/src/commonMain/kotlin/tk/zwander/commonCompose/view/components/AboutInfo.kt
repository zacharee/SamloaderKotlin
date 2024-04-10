package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

@OptIn(ExperimentalTextApi::class)
@Composable
fun AboutInfo(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val contentColor = LocalContentColor.current

        val copyrightAnnotated = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = contentColor,
                    fontSize = 16.sp,
                ),
            ) {
                append(MR.strings.version("${GradleConfig.versionName} © "))

                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    withAnnotation(UrlAnnotation("https://zwander.dev")) {
                        append(MR.strings.zacharyWander())
                    }
                }
            }
        }

        val samloaderAnnotated = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = contentColor,
                    fontSize = 16.sp,
                ),
            ) {
                append(MR.strings.basedOn())
                append(" ")
                withAnnotation(UrlAnnotation("https://github.com/nlscc/samloader")) {
                    withStyle(
                        SpanStyle(
                            color = primaryColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(MR.strings.samloader())
                    }
                }
            }
        }

        ClickableText(
            text = copyrightAnnotated,
            onClick = {
                copyrightAnnotated.getUrlAnnotations(it, it)
                    .firstOrNull()?.let { item ->
                        UrlHandler.launchUrl(item.item.url)
                    }
            },
            style = LocalTextStyle.current.copy(LocalContentColor.current),
        )

        Spacer(Modifier.height(4.dp))

        ClickableText(
            text = samloaderAnnotated,
            onClick = {
                samloaderAnnotated.getUrlAnnotations(it, it)
                    .firstOrNull()?.let { item ->
                        UrlHandler.launchUrl(item.item.url)
                    }
            },
            style = LocalTextStyle.current.copy(LocalContentColor.current),
        )
    }
}
