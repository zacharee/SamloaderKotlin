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

object Shell32 {
    init {
        Native.register("shell32")
    }

    external fun SHBrowseForFolder(params: BrowseInfo?): Pointer?
    external fun SHGetPathFromIDListW(pidl: Pointer?, path: Pointer?): Boolean

    // flags for the BrowseInfo structure
    const val BIF_RETURNONLYFSDIRS = 0x00000001
    const val BIF_DONTGOBELOWDOMAIN = 0x00000002
    const val BIF_NEWDIALOGSTYLE = 0x00000040
    const val BIF_EDITBOX = 0x00000010
    const val BIF_USENEWUI = BIF_EDITBOX or BIF_NEWDIALOGSTYLE
    const val BIF_NONEWFOLDERBUTTON = 0x00000200
    const val BIF_BROWSEINCLUDEFILES = 0x00004000
    const val BIF_SHAREABLE = 0x00008000
    const val BIF_BROWSEFILEJUNCTIONS = 0x00010000

    // http://msdn.microsoft.com/en-us/library/bb773205.aspx
    @Structure.FieldOrder(
        "hwndOwner",
        "pidlRoot",
        "pszDisplayName",
        "lpszTitle",
        "ulFlags",
        "lpfn",
        "lParam",
        "iImage",
    )
    class BrowseInfo : Structure() {
        @JvmField
        var hwndOwner: Pointer? = null
        @JvmField
        var pidlRoot: Pointer? = null
        @JvmField
        var pszDisplayName: String? = null
        @JvmField
        var lpszTitle: String? = null
        @JvmField
        var ulFlags = 0
        @JvmField
        var lpfn: Pointer? = null
        @JvmField
        var lParam: Pointer? = null
        @JvmField
        var iImage = 0
    }
}