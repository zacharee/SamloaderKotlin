import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.builders.InputAttrsBuilder
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLButtonElement

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

@Composable
fun BootstrapTextInput(
    value: String = "", attrs: InputAttrsBuilder<String>.() -> Unit = {}
) {
    TextInput(
        value = value,
        attrs = {
            attrs()
            classes("form-control")
        }
    )
}

@Composable
fun BootstrapButton(
    attrs: AttrBuilderContext<HTMLButtonElement>? = null,
    type: String = "primary",
    content: ContentBuilder<HTMLButtonElement>? = null,
) {
    Button(
        attrs = {
            attrs?.invoke(this)
            classes("btn", "btn-$type")
        },
        content = content
    )
}
