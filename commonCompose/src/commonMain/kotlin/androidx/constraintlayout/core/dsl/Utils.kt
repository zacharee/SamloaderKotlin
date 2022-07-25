package androidx.constraintlayout.core.dsl

fun Array<out String?>.stringRepresentation(): String {
    val ret = StringBuilder("[")

    forEachIndexed { index, s ->
        ret.append(if (index == 0) "'" else ",'")
        ret.append(s)
        ret.append("'")
    }

    ret.append("]")
    return ret.toString()
}
