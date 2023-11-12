package tk.zwander.commonCompose.util

expect class Matrix() {
    fun preRotate(degrees: Float, px: Float, py: Float): Boolean
    fun preScale(sx: Float, sy: Float, px: Float, py: Float): Boolean
    fun mapPoints(pts: FloatArray)
}
