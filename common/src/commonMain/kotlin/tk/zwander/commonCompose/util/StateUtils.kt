package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

// TODO: Keep an eye on BasicTextField2 API so we can remove this: https://proandroiddev.com/basictextfield2-a-textfield-of-dreams-1-2-0103fd7cc0ec.
@Composable
fun <T> MutableStateFlow<T>.collectAsImmediateMutableState(): MutableState<T> = collectAsMutableState(Dispatchers.Main.immediate)
