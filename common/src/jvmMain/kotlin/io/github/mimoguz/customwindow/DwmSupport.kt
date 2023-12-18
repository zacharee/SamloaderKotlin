package io.github.mimoguz.customwindow

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT

internal interface DwmSupport : Library {
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