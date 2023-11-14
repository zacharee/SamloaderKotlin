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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

@Composable
fun AboutInfo(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val copyrightAnnotated = buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    color = LocalContentColor.current,
                    fontSize = 16.sp
                )
            )
            append(MR.strings.version("${GradleConfig.versionName} © "))
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            )
            pushStringAnnotation("WebsiteLink", "https://zwander.dev")
            append(MR.strings.zacharyWander())
            pop()
        }

        val samloaderAnnotated = buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    color = LocalContentColor.current,
                    fontSize = 16.sp
                )
            )
            append(MR.strings.basedOn())
            append(" ")
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            )
            pushStringAnnotation("SamloaderLink", "https://github.com/nlscc/samloader")
            append(MR.strings.samloader())
            pop()
        }

        ClickableText(
            text = copyrightAnnotated,
            onClick = {
                copyrightAnnotated.getStringAnnotations("WebsiteLink", it, it)
                    .firstOrNull()?.let { item ->
                        UrlHandler.launchUrl(item.item)
                    }
            },
            style = LocalTextStyle.current.copy(LocalContentColor.current),
        )
        Spacer(Modifier.height(4.dp))
        ClickableText(
            text = samloaderAnnotated,
            onClick = {
                samloaderAnnotated.getStringAnnotations("SamloaderLink", it, it)
                    .firstOrNull()?.let { item ->
                        UrlHandler.launchUrl(item.item)
                    }
            },
            style = LocalTextStyle.current.copy(LocalContentColor.current),
        )
    }
}
