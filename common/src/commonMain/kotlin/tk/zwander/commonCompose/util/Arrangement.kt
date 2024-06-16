package tk.zwander.commonCompose.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

object CustomArrangement {
    @Composable
    fun SpaceBetweenWithSpacing(spacing: Dp) : Arrangement.HorizontalOrVertical {
        return remember(spacing) {
            val spaceBetween = Arrangement.SpaceBetween
            object : Arrangement.HorizontalOrVertical {
                override val spacing: Dp = spacing

                override fun Density.arrange(
                    totalSize: Int,
                    sizes: IntArray,
                    outPositions: IntArray,
                ) {
                    return with (spaceBetween) {
                        arrange(totalSize, sizes, outPositions)
                    }
                }

                override fun Density.arrange(
                    totalSize: Int,
                    sizes: IntArray,
                    layoutDirection: LayoutDirection,
                    outPositions: IntArray,
                ) {
                    return with (spaceBetween) {
                        arrange(totalSize, sizes, layoutDirection, outPositions)
                    }
                }
            }
        }
    }
}
