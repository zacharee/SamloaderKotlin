import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.builders.InputAttrsBuilder
import org.jetbrains.compose.web.css.CSSNumeric
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement

@Composable
fun Container(
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    type: String? = null,
    content: @Composable()() -> Unit
) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes(type.run { if (this.isNullOrBlank()) "container" else "container-$this" })
        }
    ) {
        content()
    }
}

@Composable
fun Row(
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: @Composable()() -> Unit
) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("row")
        }
    ) {
        content()
    }
}

@Composable
fun Column(
    xs: Int? = null,
    sm: Int? = null,
    md: Int? = null,
    lg: Int? = null,
    xl: Int? = null,
    xxl: Int? = null,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: @Composable()() -> Unit
) {
    Div(
        attrs = {
            attrs?.invoke(this)

            val classList = arrayListOf("col").apply {
                xs?.let { add("col-$it") }
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
    value: String = "",
    attrs: InputAttrsBuilder<String>.() -> Unit = {}
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

@Composable
fun Spacer(
    width: CSSNumeric? = null,
    height: CSSNumeric? = null
) {
    Div(
        attrs = {
            style {
                width?.let { width(it) }
                height?.let { height(it) }
            }
        }
    )
}
