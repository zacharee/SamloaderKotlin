package org.jsoup.parser

import org.jsoup.format

/**
 * A Parse Error records an error in the input HTML that occurs in either the tokenisation or the tree building phase.
 */
class ParseError {
    /**
     * Retrieves the offset of the error.
     * @return error offset within input
     */
    var position: Int
        private set

    /**
     * Get the formatted line:column cursor position where the error occurred.
     * @return line:number cursor position
     */
    //var cursorPos: String?
        //private set

    /**
     * Retrieve the error message.
     * @return the error message.
     */
    var errorMessage: String
        private set

    internal constructor(reader: CharacterReader, errorMsg: String) {
        position = reader.pos
        //cursorPos = reader.cursorPos()
        errorMessage = errorMsg
    }

    internal constructor(reader: CharacterReader?, errorFormat: String?, vararg args: Any?) {
        position = reader!!.pos
        //cursorPos = reader.cursorPos()
        errorMessage = String.format((errorFormat)!!, *args)
    }

    internal constructor(pos: Int, errorMsg: String) {
        position = pos
        //cursorPos = pos.toString()
        errorMessage = errorMsg
    }

    internal constructor(pos: Int, errorFormat: String?, vararg args: Any?) {
        position = pos
        //cursorPos = pos.toString()
        errorMessage = String.format((errorFormat)!!, *args)
    }

    override fun toString(): String {
        return "<$position>: $errorMessage"
    }
}
