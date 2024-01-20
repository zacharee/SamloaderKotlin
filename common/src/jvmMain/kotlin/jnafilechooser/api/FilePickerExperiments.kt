@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package jnafilechooser.api

//import com.jthemedetecor.OsThemeDetector
//import com.mayakapps.compose.windowstyler.windows.hwnd
//import com.mayakapps.compose.windowstyler.windows.jna.Dwm
//import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentFlag
//import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentState
//import com.mayakapps.compose.windowstyler.windows.jna.enums.DwmWindowAttribute
//import com.mayakapps.compose.windowstyler.windows.WindowsBackdropApis
//import com.mayakapps.compose.windowstyler.windows.toBgr
//import com.sun.jna.platform.win32.Advapi32Util
//import com.sun.jna.platform.win32.User32
//import com.sun.jna.platform.win32.WinDef.HWND
//import com.sun.jna.platform.win32.WinReg
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import tk.zwander.commonCompose.monet.ColorScheme
//import java.awt.Window

data object FilePickerExperiments {
//    fun applyAcrylicToFilePicker(parent: Window?) {
//        GlobalScope.launch {
//            var activeWindow: HWND?
//
//            do {
//                activeWindow = User32.INSTANCE.FindWindowEx(parent?.hwnd, null, "SunAw", null)
//                activeWindow = User32.INSTANCE.FindWindowEx(activeWindow, null, "#32770", null)
//
////                activeWindow = User32.INSTANCE.GetForegroundWindow()?.takeIf {
////                    val name = CharArray(6)
////                    User32.INSTANCE.GetClassName(it, name, 7)
////
////                    name.concatToString() == "#32770" && User32.INSTANCE.GetAncestor(parent?.hwnd, User32.GA_ROOT) == parent?.hwnd
////                }
//            } while (activeWindow == null)
//
//            val colorScheme = ColorScheme(
//                seed = java.awt.Color(
//                    Advapi32Util.registryGetIntValue(
//                        WinReg.HKEY_CURRENT_USER,
//                        "Software\\Microsoft\\Windows\\DWM",
//                        "AccentColor",
//                    )
//                ).rgb,
//                OsThemeDetector.getDetector().isDark,
//            ).toComposeColorScheme()
//
//            Dwm.setWindowAttribute(
//                activeWindow,
//                DwmWindowAttribute.DWMWA_BORDER_COLOR,
//                colorScheme.background.toBgr()
//            )
//            Dwm.setWindowAttribute(
//                activeWindow,
//                DwmWindowAttribute.DWMWA_CAPTION_COLOR,
//                colorScheme.background.toBgr()
//            )
//            Dwm.setWindowAttribute(
//                activeWindow,
//                DwmWindowAttribute.DWMWA_TEXT_COLOR,
//                colorScheme.onBackground.toBgr()
//            )
//
//            val apis = WindowsBackdropApis(activeWindow)
//
////            apis.setSystemBackdrop(DwmSystemBackdrop.DWMSBT_TRANSIENTWINDOW)
////            apis.setMicaEffectEnabled(true)
//            apis.setAccentPolicy(
//                AccentState.ACCENT_ENABLE_ACRYLICBLURBEHIND,
//                setOf(AccentFlag.DRAW_ALL_BORDERS)
//            )
//        }
//    }
}