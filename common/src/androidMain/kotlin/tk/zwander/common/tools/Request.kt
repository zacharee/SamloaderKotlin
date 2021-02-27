package tk.zwander.common.tools

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter

actual object PlatformRequest {
    actual fun binaryInform(fw: String, model: String, region: String, nonce: String): String {
        val fusMsg = Element("FUSMsg")
        val doc = Document(fusMsg)

        val fusHdr = Element("FUSHdr")
        fusMsg.addContent(fusHdr)

        val protoVer = Element("ProtoVer")
        protoVer.text = "1.0"
        fusHdr.addContent(protoVer)

        val fusBody = Element("FUSBody")
        fusMsg.addContent(fusBody)

        val fPut = Element("Put").apply {
            addContent(Element("ACCESS_MODE").apply {
                addContent(Element("Data").apply {
                    text = "2"
                })
            })
            addContent(Element("BINARY_NATURE").apply {
                addContent(Element("Data").apply {
                    text = "1"
                })
            })
            addContent(Element("CLIENT_PRODUCT").apply {
                addContent(Element("Data").apply {
                    text = "Smart Switch"
                })
            })
            addContent(Element("DEVICE_FW_VERSION").apply {
                addContent(Element("Data").apply {
                    text = fw
                })
            })
            addContent(Element("DEVICE_LOCAL_CODE").apply {
                addContent(Element("Data").apply {
                    text = region
                })
            })
            addContent(Element("DEVICE_MODEL_NAME").apply {
                addContent(Element("Data").apply {
                    text = model
                })
            })
            addContent(Element("LOGIC_CHECK").apply {
                addContent(Element("Data").apply {
                    text = Request.getLogicCheck(fw, nonce)
                })
            })
        }
        fusBody.addContent(fPut)

        val outputter = XMLOutputter()
        outputter.format = Format.getPrettyFormat()

        return outputter.outputString(doc)
    }

    actual fun binaryInit(fileName: String, nonce: String): String {
        val fusMsg = Element("FUSMsg")
        val doc = Document(fusMsg)

        val fusHdr = Element("FUSHdr")
        fusMsg.addContent(fusHdr)

        val protoVer = Element("ProtoVer")
        protoVer.text = "1.0"
        fusHdr.addContent(protoVer)

        val fusBody = Element("FUSBody")
        fusMsg.addContent(fusBody)

        val fPut = Element("Put").apply {
            addContent(Element("BINARY_FILE_NAME").apply {
                addContent(Element("Data").apply {
                    text = fileName
                })
            })
            addContent(Element("LOGIC_CHECK").apply {
                addContent(Element("Data").apply {
                    val special = fileName.split(".").first()
                        .run { slice(this.length - (16 % this.length)..this.lastIndex) }
                    text = Request.getLogicCheck(special, nonce)
                })
            })
        }
        fusBody.addContent(fPut)

        val outputter = XMLOutputter()
        outputter.format = Format.getCompactFormat()

        return outputter.outputString(doc)
    }
}