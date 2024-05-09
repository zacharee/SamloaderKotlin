package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@Composable
fun String.toRichHtmlString(): AnnotatedString {
    val state = rememberRichTextState()
    state.setHtml(this)
    return state.annotatedString
}
