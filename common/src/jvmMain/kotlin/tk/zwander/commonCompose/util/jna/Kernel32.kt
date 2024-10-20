package tk.zwander.commonCompose.util.jna

import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.Wincon
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

@Suppress("FunctionName")
interface Kernel32 : StdCallLibrary, WinNT, Wincon {
    companion object {
        private const val IMAGE_FILE_MACHINE_ARM64 = 0xAA64L

        private val INSTANCE =
            Native.load("kernel32", Kernel32::class.java, W32APIOptions.DEFAULT_OPTIONS)

        fun isEmulatedX86(): Boolean {
            try {
                val processHandle = com.sun.jna.platform.win32.Kernel32.INSTANCE.GetCurrentProcess()
                val processMachine = WinDef.USHORTByReference()
                val nativeMachine = WinDef.USHORTByReference()

                INSTANCE.IsWow64Process2(processHandle, processMachine, nativeMachine)

                return nativeMachine.value == WinDef.USHORT(IMAGE_FILE_MACHINE_ARM64)
            } catch (e: Throwable) {
                println("Unable to check for emulated x86.")
                return false
            }
        }
    }

    fun IsWow64Process2(
        hProcess: WinNT.HANDLE,
        pProcessMachine: WinDef.USHORTByReference,
        pNativeMachine: WinDef.USHORTByReference,
    )
}