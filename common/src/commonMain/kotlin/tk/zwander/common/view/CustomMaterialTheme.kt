package tk.zwander.common.view

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import tk.zwander.common.data.primary
import tk.zwander.common.data.primaryVariant
import tk.zwander.common.data.secondary
import tk.zwander.common.data.secondaryVariant

@Composable
fun CustomMaterialTheme(block: @Composable()() -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = Color(primary.toLong(16)),
            primaryVariant = Color(primaryVariant.toLong(16)),
            secondary = Color(secondary.toLong(16)),
            secondaryVariant = Color(secondaryVariant.toLong(16))
        )
    ) {
        block()
    }
}