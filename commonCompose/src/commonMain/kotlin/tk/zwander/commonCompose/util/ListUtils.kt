package tk.zwander.commonCompose.util

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

fun LazyListState.disableScrolling(scope: CoroutineScope) {
    scope.launch {
        scroll(scrollPriority = MutatePriority.PreventUserInput) {
            // Await indefinitely, blocking scrolls
            awaitCancellation()
        }
    }
}

fun LazyListState.reenableScrolling(scope: CoroutineScope) {
    scope.launch {
        scroll(scrollPriority = MutatePriority.PreventUserInput) {
            // Do nothing, just cancel the previous indefinite "scroll"
        }
    }
}
