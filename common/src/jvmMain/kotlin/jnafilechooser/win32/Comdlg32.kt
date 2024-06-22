/* This file is part of JnaFileChooser.
 *
 * JnaFileChooser is free software: you can redistribute it and/or modify it
 * under the terms of the new BSD license.
 *
 * JnaFileChooser is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 */
package jnafilechooser.win32

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.WString

object Comdlg32 {
    init {
        Native.register("comdlg32")
    }

    external fun GetOpenFileNameW(params: OpenFileName?): Boolean
    external fun GetSaveFileNameW(params: OpenFileName?): Boolean
    external fun CommDlgExtendedError(): Int

    // flags for the OpenFileName structure
    const val OFN_READONLY = 0x00000001
    const val OFN_OVERWRITEPROMPT = 0x00000002
    const val OFN_HIDEREADONLY = 0x00000004
    const val OFN_NOCHANGEDIR = 0x00000008
    const val OFN_SHOWHELP = 0x00000010
    const val OFN_ENABLEHOOK = 0x00000020
    const val OFN_ENABLETEMPLATE = 0x00000040
    const val OFN_ENABLETEMPLATEHANDLE = 0x00000080
    const val OFN_NOVALIDATE = 0x00000100
    const val OFN_ALLOWMULTISELECT = 0x00000200
    const val OFN_EXTENSIONDIFFERENT = 0x00000400
    const val OFN_PATHMUSTEXIST = 0x00000800
    const val OFN_FILEMUSTEXIST = 0x00001000
    const val OFN_CREATEPROMPT = 0x00002000
    const val OFN_SHAREAWARE = 0x00004000
    const val OFN_NOREADONLYRETURN = 0x00008000
    const val OFN_NOTESTFILECREATE = 0x00010000
    const val OFN_NONETWORKBUTTON = 0x00020000
    const val OFN_NOLONGNAMES = 0x00040000
    const val OFN_EXPLORER = 0x00080000
    const val OFN_NODEREFERENCELINKS = 0x00100000
    const val OFN_LONGNAMES = 0x00200000
    const val OFN_ENABLEINCLUDENOTIFY = 0x00400000
    const val OFN_ENABLESIZING = 0x00800000
    const val OFN_DONTADDTORECENT = 0x02000000
    const val OFN_FORCESHOWHIDDEN = 0x10000000

    // error codes from cderr.h which may be returned by
    // CommDlgExtendedError for the GetOpenFileName and
    // GetSaveFileName functions.
    const val CDERR_DIALOGFAILURE = 0xFFFF
    const val CDERR_FINDRESFAILURE = 0x0006
    const val CDERR_INITIALIZATION = 0x0002
    const val CDERR_LOADRESFAILURE = 0x0007
    const val CDERR_LOADSTRFAILURE = 0x0005
    const val CDERR_LOCKRESFAILURE = 0x0008
    const val CDERR_MEMALLOCFAILURE = 0x0009
    const val CDERR_MEMLOCKFAILURE = 0x000A
    const val CDERR_NOHINSTANCE = 0x0004
    const val CDERR_NOHOOK = 0x000B
    const val CDERR_NOTEMPLATE = 0x0003
    const val CDERR_STRUCTSIZE = 0x0001
    const val FNERR_SUBCLASSFAILURE = 0x3001
    const val FNERR_INVALIDFILENAME = 0x3002
    const val FNERR_BUFFERTOOSMALL = 0x3003

    @Structure.FieldOrder(
        "lStructSize",
        "hwndOwner",
        "hInstance",
        "lpstrFilter",
        "lpstrCustomFilter",
        "nMaxCustFilter",
        "nFilterIndex",
        "lpstrFile",
        "nMaxFile",
        "lpstrDialogTitle",
        "nMaxDialogTitle",
        "lpstrInitialDir",
        "lpstrTitle",
        "Flags",
        "nFileOffset",
        "nFileExtension",
        "lpstrDefExt",
        "lCustData",
        "lpfnHook",
        "lpTemplateName",
    )
    class OpenFileName : Structure() {
        @JvmField
        var lStructSize: Int = size()
        @JvmField
        var hwndOwner: Pointer? = null
        @JvmField
        var hInstance: Pointer? = null
        @JvmField
        var lpstrFilter: WString? = null
        @JvmField
        var lpstrCustomFilter: WString? = null
        @JvmField
        var nMaxCustFilter = 0
        @JvmField
        var nFilterIndex = 0
        @JvmField
        var lpstrFile: Pointer? = null
        @JvmField
        var nMaxFile = 0
        @JvmField
        var lpstrDialogTitle: String? = null
        @JvmField
        var nMaxDialogTitle = 0
        @JvmField
        var lpstrInitialDir: WString? = null
        @JvmField
        var lpstrTitle: WString? = null
        @JvmField
        var Flags = 0
        @JvmField
        var nFileOffset: Short = 0
        @JvmField
        var nFileExtension: Short = 0
        @JvmField
        var lpstrDefExt: WString? = null
        @JvmField
        var lCustData: Pointer? = null
        @JvmField
        var lpfnHook: Pointer? = null
        @JvmField
        var lpTemplateName: Pointer? = null

    }
}