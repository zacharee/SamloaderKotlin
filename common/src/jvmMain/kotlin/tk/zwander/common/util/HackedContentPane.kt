package tk.zwander.common.util

import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JPanel

internal class HackedContentPane : JPanel() {
    override fun paint(g: Graphics) {
        println("alpha ${background.alpha}")
        if (background.alpha != 255) {
            val gg = g.create()

            try {
                if (gg is Graphics2D) {
                    gg.setColor(background)
                    gg.composite = AlphaComposite.getInstance(AlphaComposite.SRC)
                    gg.fillRect(0, 0, width, height)
                }
            } finally {
                gg.dispose()
            }
        }

        super.paint(g)
    }
}