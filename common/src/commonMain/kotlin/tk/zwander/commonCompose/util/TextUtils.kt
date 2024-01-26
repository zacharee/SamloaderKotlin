package tk.zwander.commonCompose.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class OffsetCorrectedIdentityTransformation(val value: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = text,
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return offset.coerceAtMost(value.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return offset.coerceAtMost(value.length)
                }
            },
        )
    }
}
