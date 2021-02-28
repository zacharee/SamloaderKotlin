package tk.zwander.common.view

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import tk.zwander.common.data.*

@Composable
fun CustomMaterialTheme(block: @Composable()() -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = Color(primary.toLong(16)),
            primaryVariant = Color(primaryVariant.toLong(16)),
            secondary = Color(secondary.toLong(16)),
            secondaryVariant = Color(secondaryVariant.toLong(16)),
            background = Color(background.toLong(16))
        )
    ) {
        block()
    }
}