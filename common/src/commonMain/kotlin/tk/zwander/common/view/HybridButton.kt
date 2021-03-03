package tk.zwander.common.view

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tk.zwander.common.util.DPScale

@Composable
fun HybridButton(
    icon: ImageBitmap,
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
                Icon(bitmap = icon, contentDescription = description)
            }
        }
    }
}