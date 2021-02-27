package tk.zwander.common.tools

import org.jdom2.input.SAXBuilder

actual object PlatformMain {
    actual fun getBinaryInfo(response: String): Main.BinaryFileInfo {
        val doc = SAXBuilder().build(response.byteInputStream())
        val root = doc.rootElement

        val status = root.getChild("FUSBody")
            .getChild("Results")
            .getChild("Status")
            .text.toInt()

        if (status != 200) {
            throw Exception("Bad return status: $status")
        }

        val size = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("BINARY_BYTE_SIZE")
            .getChild("Data")
            .text.toLong()

        val fileName = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("BINARY_NAME")
            .getChild("Data")
            .text

        val path = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("MODEL_PATH")
            .getChild("Data")
            .text

        val crc32 = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("BINARY_CRC")
            .getChild("Data")
            .text?.toLong()

        return Main.BinaryFileInfo(path, fileName, size, crc32)
    }
}