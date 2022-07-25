package tk.zwander.commonCompose.util

import org.jetbrains.skia.Matrix33

actual class Matrix {
    private var actualMatrix = Matrix33()

    actual fun preRotate(degrees: Float, px: Float, py: Float): Boolean {
        if (degrees == 0f) {
            return false
        }

        val rotate = Matrix33.makeRotate(degrees, px, py)

        actualMatrix = actualMatrix.makeConcat(rotate)
        return true
    }

    actual fun preScale(sx: Float, sy: Float, px: Float, py: Float): Boolean {
        if (sx == 1f && sy == 1f) {
            return false
        }

        val scale = Matrix33.makeScale(sx, sy)
        val translate = Matrix33.makeTranslate(px - sx * px, py - sy * py)

        actualMatrix = actualMatrix.makeConcat(scale).makeConcat(translate)
        return true
    }

    actual fun mapPoints(pts: FloatArray) {
        val result = Matrix33(*pts).makeConcat(actualMatrix)

        result.mat.copyInto(pts)
    }
}
