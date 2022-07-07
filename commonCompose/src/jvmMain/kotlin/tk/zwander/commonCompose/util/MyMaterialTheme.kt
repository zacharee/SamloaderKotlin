package tk.zwander.commonCompose.util

import mdlaf.themes.MaterialOceanicTheme
import tk.zwander.common.data.background
import tk.zwander.common.data.primary
import tk.zwander.common.data.surface
import java.awt.Color
import javax.swing.plaf.ColorUIResource

class MyMaterialTheme : MaterialOceanicTheme() {
    override fun installDefaultColor() {
        super.installDefaultColor()

        selectionForegroundTable = selectionForegroundList
    }

    override fun installColor() {
        super.installColor()

        backgroundPrimary = ColorUIResource(Color.decode("#${background.substring(2)}"))
        highlightBackgroundPrimary = ColorUIResource(Color.decode("#${primary.substring(2)}"))

        buttonBackgroundColor = ColorUIResource(Color.decode("#${surface.substring(2)}"))
        buttonBackgroundColorMouseHover = ColorUIResource(Color.decode("#${primary.substring(2)}"))
        buttonDefaultBackgroundColor = buttonBackgroundColor
        buttonDefaultBackgroundColorMouseHover = buttonBackgroundColorMouseHover

        textColor = ColorUIResource(Color.decode("#DEDEDE"))
        disableTextColor = ColorUIResource(Color.decode("#AAAAAA"))

        disabledBackgroudnTextField = backgroundPrimary
        backgroundTable = backgroundPrimary
        backgroundTableHeader = backgroundPrimary
        gridColorTable = backgroundPrimary
    }
}