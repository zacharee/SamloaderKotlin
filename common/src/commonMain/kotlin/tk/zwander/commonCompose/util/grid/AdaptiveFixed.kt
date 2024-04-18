package tk.zwander.commonCompose.util.grid

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// TODO: Remove this once https://android-review.googlesource.com/c/platform/frameworks/support/+/3013813 is merged into Multiplatform.
class AdaptiveFixed(private val minSize: Dp) : StaggeredGridCells {
    init {
        require(minSize > 0.dp) { "invalid minSize" }
    }

    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int
    ): IntArray {
        val count = maxOf((availableSize + spacing) / (minSize.roundToPx() + spacing), 1)
        return calculateCellsCrossAxisSizeImpl(availableSize, count, spacing)
    }

    override fun hashCode(): Int {
        return minSize.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is AdaptiveFixed && minSize == other.minSize
    }
}

private fun calculateCellsCrossAxisSizeImpl(
    gridSize: Int,
    slotCount: Int,
    spacing: Int
): IntArray {
    val gridSizeWithoutSpacing = gridSize - spacing * (slotCount - 1)
    val slotSize = gridSizeWithoutSpacing / slotCount
    val remainingPixels = gridSizeWithoutSpacing % slotCount
    return IntArray(slotCount) {
        if (slotSize < 0) {
            0
        } else {
            slotSize + if (it < remainingPixels) 1 else 0
        }
    }
}
