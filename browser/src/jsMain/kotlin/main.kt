@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime
import androidx.compose.ui.native.ComposeLayer
import moe.tlaster.precompose.preComposeWindow
import tk.zwander.common.GradleConfig

var canvas = document.getElementById("ComposeTarget") as HTMLCanvasElement

fun canvasResize(width: Int = window.innerWidth, height: Int = window.innerHeight) {
    canvas.setAttribute("width", "$width")
    canvas.setAttribute("height", "$height")
}

fun composableResize(layer: ComposeLayer) {
    val clone = canvas.cloneNode(false) as HTMLCanvasElement
    canvas.replaceWith(clone)
    canvas = clone

    val scale = layer.layer.contentScale
    canvasResize()
    layer.layer.attachTo(clone)
    layer.layer.needRedraw()
    layer.setSize(
        (clone.width / scale).toInt(),
        (clone.height / scale).toInt()
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
        preComposeWindow(GradleConfig.appName) {
            window.addEventListener("resize", {
                composableResize(layer = layer)
            })
            MainView(Modifier.fillMaxSize())
        }
    }
}
