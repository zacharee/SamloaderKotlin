package tk.zwander.common.tools

import org.jdom2.input.SAXBuilder

actual object PlatformCrypt  {
    actual fun getFwAndLogic(response: String): Pair<String, String> {
        val doc = SAXBuilder().build(response.byteInputStream())
        val root = doc.rootElement
        val fwVer = root.getChild("FUSBody")
            .getChild("Results")
            .getChild("LATEST_FW_VERSION")
            .getChild("Data")
            .text
        val logicVal = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("LOGIC_VALUE_FACTORY")
            .getChild("Data")
            .text

        return fwVer to logicVal
    }
}