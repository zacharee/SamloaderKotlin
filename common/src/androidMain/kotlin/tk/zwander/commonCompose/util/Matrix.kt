package tk.zwander.commonCompose.util

import android.graphics.Matrix

actual class Matrix {
    private val actualMatrix = Matrix()

    actual fun preRotate(degrees: Float, px: Float, py: Float): Boolean {
        return actualMatrix.preRotate(degrees, px, py)
    }

    actual fun preScale(sx: Float, sy: Float, px: Float, py: Float): Boolean {
        return actualMatrix.preScale(sx, sy, px, py)
    }

    actual fun mapPoints(pts: FloatArray) {
        return actualMatrix.mapPoints(pts)
    }
}
