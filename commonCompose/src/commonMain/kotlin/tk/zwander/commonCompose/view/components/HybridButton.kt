package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity

/**
 * A special Button that shows as a text button if there's enough room
 * and an image button otherwise.
 */
@Composable
fun HybridButton(
    icon: ImageBitmap? = null,
    vectorIcon: ImageVector? = null,
    description: String?,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    parentSize: Int
) {
    val fontScale = LocalDensity.current.fontScale
    val density = LocalDensity.current.density

    BoxWithConstraints {
        if (parentSize / (fontScale * density) >= 400) {
            OutlinedButton(modifier = modifier, onClick = onClick, enabled = enabled) {
                Text(text)
            }
        } else {
            IconButton(modifier = modifier, onClick = onClick, enabled = enabled) {
                when {
                    icon != null -> {
                        Icon(
                            bitmap = icon,
                            contentDescription = description,
                        )
                    }
                    vectorIcon != null -> {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = description,
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
