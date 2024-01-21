package tk.zwander.common.util.jna.windows

import com.sun.jna.Native
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

@Suppress("SpellCheckingInspection")
object DwmImpl : DwmApi by Native.load("dwmapi", DwmApi::class.java, W32APIOptions.DEFAULT_OPTIONS)

@Suppress("FunctionName")
interface DwmApi : StdCallLibrary {
    fun DwmSetWindowAttribute(hwnd: WinDef.HWND, attribute: Int, value: PointerType?, valueSize: Int): WinNT.HRESULT
}