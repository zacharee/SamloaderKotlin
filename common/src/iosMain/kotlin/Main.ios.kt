@file:Suppress("FunctionName", "unused")

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime

@OptIn(InternalComposeUiApi::class, ExperimentalTime::class)
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        MainView(
            modifier = Modifier
                .imePadding()
                .systemBarsPadding(),
        )
    }
}
