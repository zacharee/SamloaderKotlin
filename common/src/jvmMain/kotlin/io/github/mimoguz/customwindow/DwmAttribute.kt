package io.github.mimoguz.customwindow

/**
 * Some DWM enum variants and constants.
 */
@Suppress("unused")
enum class DwmAttribute(
    /**
     * Corresponding DMW constant value
     */
    val value: Int
) {
    // If you only care about color customization, you will only need
    //  * DWMWA_BORDER_COLOR,
    //  * DWMWA_CAPTION_COLOR, and
    //  * DWMWA_TEXT_COLOR.
    // The rest can be removed.
    /**
     * Window border color. Accepts a COLORREF or a DWORD reference for pvAttribute,
     * 0x00_bb_gg_rr.
     */
    DWMWA_BORDER_COLOR(34),

    /**
     * Title bar color. Accepts a COLORREF or a DWORD reference for pvAttribute,
     * 0x00_bb_gg_rr.
     */
    DWMWA_CAPTION_COLOR(35),

    /**
     * Title text color. Accepts a COLORREF or a DWORD reference for pvAttribute,
     * 0x00_bb_gg_rr.
     */
    DWMWA_TEXT_COLOR(36),

    /**
     * A possible value for DWMWA_SYSTEMBACKDROP_TYPE: default.
     */
    DWMSBT_DISABLE(1),

    /**
     * A possible value for DWMWA_SYSTEMBACKDROP_TYPE: mica.
     */
    DWMSBT_MAINWINDOW(2),

    /**
     * A possible value for DWMWA_SYSTEMBACKDROP_TYPE: tabbed.
     */
    DWMSBT_TABBEDWINDOW(4),

    /**
     * A possible value for DWMWA_SYSTEMBACKDROP_TYPE: acrylic.
     */
    DWMSBT_TRANSIENTWINDOW(3),

    /**
     * The old way of enabling the mica effect. Takes a boolean reference for
     * pvAttribute.
     */
    DWMWA_MICA_EFFECT(1029),

    /**
     * The new way of enabling the mica effect. Takes a DWMSBT_* for pvAttribute
     */
    DWMWA_SYSTEMBACKDROP_TYPE(38),

    /**
     * Set dark (or light) mode. Accepts a BOOLRef for pvAttribute.
     */
    DWMWA_USE_IMMERSIVE_DARK_MODE(20)
}