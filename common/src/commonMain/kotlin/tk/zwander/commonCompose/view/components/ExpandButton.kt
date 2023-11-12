package tk.zwander.commonCompose.view.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp

@Composable
internal fun ExpandButton(
    expanded: Boolean,
    text: String,
    onExpandChange: (Boolean) -> Unit
) {
    val expandText = buildAnnotatedString {
        append(text)
        append(" ")
        appendInlineContent("expandIcon", "[icon]")
    }
    val inlineContent = mapOf(
        "expandIcon" to InlineTextContent(
            placeholder = Placeholder(
                16.sp,
                16.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Crossfade(expanded) {
                if (it) {
                    Icon(Icons.Filled.KeyboardArrowUp, "")
                } else {
                    Icon(Icons.Filled.KeyboardArrowDown, "")
                }
            }
        }
    )

    TextButton(
        onClick = {
            onExpandChange(!expanded)
        }
    ) {
        Text(
            text = expandText,
            inlineContent = inlineContent,
        )
    }
}
