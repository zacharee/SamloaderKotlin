package tk.zwander.common.view

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CustomMaterialTheme(block: @Composable()() -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = Color(0x4C, 0xAF, 0x50),
            primaryVariant = Color(0x52, 0xC7, 0x56),
            secondary = Color(0xFFBB86FC),
            secondaryVariant = Color(0xFF3700B3)
        )
    ) {
        block()
    }
}