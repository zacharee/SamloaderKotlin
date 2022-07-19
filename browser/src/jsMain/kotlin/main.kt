@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement
import tk.zwander.common.GradleConfig
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime
import androidx.compose.ui.native.ComposeLayer
import androidx.compose.ui.window.ComposeWindow

val canvas = document.getElementById("ComposeTarget") as HTMLCanvasElement

fun canvasResize(width: Int = window.innerWidth, height: Int = window.innerHeight) {
    canvas.setAttribute("width", "$width")
    canvas.setAttribute("height", "$height")
}

fun composableResize(layer: ComposeLayer) {
    val scale = layer.layer.contentScale
    canvasResize()
    layer.layer.attachTo(canvas)
    layer.layer.needRedraw()
    layer.setSize(
        (canvas.width / scale).toInt(),
        (canvas.height / scale).toInt()
    )
}

@OptIn(ExperimentalTime::class)
fun main() {
    js("""
            window.originalFetch = window.fetch;
            window.fetch = function (resource, init) {
                init = Object.assign({}, init);
                init.credentials = init.credentials !== undefined ? init.credentials : 'include';
                return window.originalFetch(resource, init);
            };
        """)

    onWasmReady {
        canvasResize()
        ComposeWindow().apply {
            setTitle(GradleConfig.appName)

            setContent {
                window.addEventListener("resize", {
                    composableResize(layer = layer)
                })
                MainView(Modifier.fillMaxSize())
            }
        }
    }
}
