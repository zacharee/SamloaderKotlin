package tk.zwander.common.view

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tk.zwander.common.util.DPScale

@Composable
fun HybridButton(
    icon: ImageBitmap? = null,
    vectorIcon: ImageVector? = null,
    description: String?,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    parentSize: Dp
) {
    val fontScale = LocalDensity.current.fontScale
    val dpScale = DPScale.dpScale

    BoxWithConstraints {
        if (parentSize / fontScale * dpScale >= 800.dp) {
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