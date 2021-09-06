import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div

@Composable
fun Container(content: @Composable()() -> Unit) {
    Div(
        attrs = {
            classes("container")
        }
    ) {
        content()
    }
}

@Composable
fun Row(content: @Composable()() -> Unit) {
    Div(
        attrs = {
            classes("row")
        }
    ) {
        content()
    }
}

@Composable
fun Column(
    sm: Int? = null,
    md: Int? = null,
    lg: Int? = null,
    xl: Int? = null,
    xxl: Int? = null,
    content: @Composable()() -> Unit
) {
    Div(
        attrs = {
            val classList = arrayListOf("col").apply {
                sm?.let { add("col-sm-$it") }
                md?.let { add("col-md-$it") }
                lg?.let { add("col-lg-$it") }
                xl?.let { add("col-xl-$it") }
                xxl?.let { add("col-xxl-$it") }
            }

            classes(*classList.toTypedArray())
        }
    ) {
        content()
    }
}
