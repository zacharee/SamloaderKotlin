package tk.zwander.common.view.components

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tk.zwander.common.data.*

/**
 * A Material theme with custom colors and such.
 */
@Composable
fun CustomMaterialTheme(block: @Composable()() -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = Color(primary.toLong(16)),
            primaryVariant = Color(primaryVariant.toLong(16)),
            secondary = Color(secondary.toLong(16)),
            secondaryVariant = Color(secondaryVariant.toLong(16)),
            background = Color(background.toLong(16))
        ),
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        )
    ) {
        block()
    }
}