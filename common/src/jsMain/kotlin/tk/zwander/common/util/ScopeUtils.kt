package tk.zwander.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun <T> runBlocking(action: suspend CoroutineScope.() -> T?): T? {
    var done = false
    var result: T? = null

    GlobalScope.launch {
        result = action()
        done = true
    }

    while (!done) {
        continue
    }

    return result
}