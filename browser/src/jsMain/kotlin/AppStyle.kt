import org.jetbrains.compose.web.css.*

object AppStyle : StyleSheet() {
    init {
        ".form-control, .form-control:focus, .form-control[readonly]" style {
            property("background-color", "#222")
        }

        ".form-control" style {
            color(rgb(0xad, 0xb5, 0xbd))
        }

        ".form-control:focus" style {
            color(rgb(0xe9, 0xec, 0xef))
        }
    }

    val main by style {
        property("margin-left", "auto")
        property("margin-right", "auto")
        property("margin-top", "2em")
        property("margin-bottom", "2em")

        property("box-shadow", "0.1em 0.1em 0.1em black")

        maxWidth(90.vw)
        minHeight(90.vh)

        padding(1.em)

        backgroundColor(rgb(0x44, 0x44, 0x44))
        borderRadius(1.em)
    }
}
