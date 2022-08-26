package swiftsoup

class Parser(private val treeBuilder: TreeBuilder) {
    companion object {
        private const val DEFAULT_MAX_ERRORS = 0
    }

    private var settings = treeBuilder.defaultSettings()
}
