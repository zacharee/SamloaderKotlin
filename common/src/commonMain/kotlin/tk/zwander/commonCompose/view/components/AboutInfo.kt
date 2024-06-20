package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

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
                    withLink(LinkAnnotation.Url("https://zwander.dev")) {
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
                withLink(LinkAnnotation.Url("https://github.com/nlscc/samloader")) {
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

        Text(
            text = copyrightAnnotated,
            style = LocalTextStyle.current.copy(LocalContentColor.current),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = samloaderAnnotated,
            style = LocalTextStyle.current.copy(LocalContentColor.current),
        )
    }
}
