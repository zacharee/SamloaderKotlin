package tk.zwander.commonCompose.monet

import kotlin.math.pow

fun pow(b: Double, p: Double): Double {
    return b.pow(p)
}

fun lerp(start: Double, stop: Double, amount: Double): Double {
    return start + (stop - start) * amount
}
