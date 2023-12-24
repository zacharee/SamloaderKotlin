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

object Ole32 {
    init {
        Native.register("ole32")
    }

    external fun OleInitialize(pvReserved: Pointer?): Pointer?
    external fun CoTaskMemFree(pv: Pointer?)
}