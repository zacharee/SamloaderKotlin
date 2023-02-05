package tk.zwander.commonCompose.view.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import tk.zwander.commonCompose.util.rememberIsOverScaledThreshold

/**
 * A special Button that shows as a text button if there's enough room
 * and an image button otherwise.
 */
@Composable
internal fun HybridButton(
    icon: ImageBitmap? = null,
    vectorIcon: Painter? = null,
    description: String?,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    parentSize: Int
) {
    Box(
        modifier = Modifier.animateContentSize()
    ) {
        Crossfade(
            targetState = rememberIsOverScaledThreshold(parentSize)
        ) {
            if (it) {
                OutlinedButton(modifier = modifier, onClick = onClick, enabled = enabled) {
                    Text(text)
                }
            } else {
                IconButton(
                    modifier = modifier,
                    onClick = onClick,
                    enabled = enabled,
                ) {
                    when {
                        icon != null -> {
                            Icon(
                                bitmap = icon,
                                contentDescription = description,
                            )
                        }
                        vectorIcon != null -> {
                            Icon(
                                painter = vectorIcon,
                                contentDescription = description,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        else -> {
                            throw IllegalArgumentException("Either icon or vectorIcon must not be null.")
                        }
                    }
                }
            }
        }
    }
}
