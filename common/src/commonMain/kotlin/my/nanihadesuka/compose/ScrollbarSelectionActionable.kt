package my.nanihadesuka.compose

/**
 * Scrollbar selection modes.
 */
enum class ScrollbarSelectionActionable {
    /**
     * Can select scrollbar always (when visible or hidden)
     */
    Always,

    /**
     * Can select scrollbar only when visible
     */
    WhenVisible,
}