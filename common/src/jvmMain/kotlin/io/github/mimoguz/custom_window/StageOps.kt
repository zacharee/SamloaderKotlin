package io.github.mimoguz.custom_window

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.W32Errors
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import java.util.*
import kotlin.math.max


/**
 * A small collection of utility methods to customize a window.
 * Targets Windows 11+, won't show any effect on unsupported OSes.
 */
object StageOps {
    /**
     * A wrapper for DwmSetWindowAttribute.
     *
     * @param handle    WindowHandle for the window. Can be obtained by using findWindowHandle method. Can be null.
     * @param attribute dwAttribute
     * @param value     pvAttribute
     * @return True if it was successful, false if it wasn't.
     */
    fun dwmSetBooleanValue(handle: WindowHandle?, attribute: DwmAttribute, value: Boolean): Boolean {
        return if (handle == null) {
            false
        } else isOk(
            DwmSupport.INSTANCE.DwmSetWindowAttribute(
                handle.value,
                attribute.value,
                WinDef.BOOLByReference(WinDef.BOOL(value)),
                WinDef.BOOL.SIZE
            )
        )
    }

    /**
     * A wrapper for DwmSetWindowAttribute.
     *
     * @param handle    WindowHandle for the window. Can be obtained by using findWindowHandle method. Can be null.
     * @param attribute dwAttribute
     * @param value     pvAttribute
     * @return True if it was successful, false if it wasn't.
     */
    fun dwmSetIntValue(handle: WindowHandle?, attribute: DwmAttribute, value: Int): Boolean {
        return if (handle == null) {
            false
        } else isOk(
            DwmSupport.INSTANCE.DwmSetWindowAttribute(
                handle.value,
                attribute.value,
                WinDef.DWORDByReference(WinDef.DWORD(value.toLong())),
                WinDef.DWORD.SIZE
            )
        )
    }

    /**
     * Try find the window handle.
     *
     * @param stage JavaFX Stage to search.
     * @return WindowHandle if it can find, null otherwise.
     */
    fun findWindowHandle(stage: ComposeWindow): WindowHandle? {
        if (Platform.getOSType() != Platform.WINDOWS) {
            return null
        }
        val searchString: String = "stage_" + UUID.randomUUID()
        val title: String = stage.title
        stage.title = searchString
        val hwnd = User32.INSTANCE.FindWindow(null, searchString)
        stage.title = title
        return hwnd?.let { WindowHandle(it) }
    }

    /**
     * Sets the border color of a window.
     *
     * @param handle WindowHandle for the window. Can be obtained by using findWindowHandle method. Can be null.
     * @param color  Border color
     * @return True if it was successful, false if it wasn't.
     */
    fun setBorderColor(handle: WindowHandle?, color: Color): Boolean {
        return dwmSetIntValue(handle, DwmAttribute.DWMWA_BORDER_COLOR, RGB(color))
    }

    /**
     * Sets the title bar background color of a window.
     *
     * @param handle WindowHandle for the window. Can be obtained by using findWindowHandle method. Can be null.
     * @param color  Caption color
     * @return True if it was successful, false if it wasn't.
     */
    fun setCaptionColor(handle: WindowHandle?, color: Color): Boolean {
        return dwmSetIntValue(handle, DwmAttribute.DWMWA_CAPTION_COLOR, RGB(color))
    }

    /**
     * Sets the title text color of a window.
     *
     * @param handle WindowHandle for the window. Can be obtained by using findWindowHandle method. Can be null.
     * @param color  Caption color
     * @return True if it was successful, false if it wasn't.
     */
    fun setTextColor(handle: WindowHandle?, color: Color): Boolean {
        return dwmSetIntValue(handle, DwmAttribute.DWMWA_TEXT_COLOR, RGB(color))
    }

    private fun floatingTo8Bit(n: Float): Int {
        return 255f.coerceAtMost(max(n * 255f, 0f)).toInt()
    }

    private fun isOk(result: WinNT.HRESULT): Boolean {
        return WinNT.HRESULT.compare(result, W32Errors.S_OK) == 0
    }

    private fun RGB(color: Color): Int {
        return (floatingTo8Bit(color.blue) shl 16
                or (floatingTo8Bit(color.green) shl 8)
                or floatingTo8Bit(color.red))
    }

    /**
     * A wrapper for HWND type.
     */
    data class WindowHandle(val value: WinDef.HWND)
    private interface DwmSupport : Library {
        fun DwmSetWindowAttribute(
            hwnd: WinDef.HWND?,
            dwAttribute: Int,
            pvAttribute: PointerType?,
            cbAttribute: Int
        ): WinNT.HRESULT

        companion object {
            val INSTANCE = Native.load("dwmapi", DwmSupport::class.java)
        }
    }
}
