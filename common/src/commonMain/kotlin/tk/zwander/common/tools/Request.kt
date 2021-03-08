package tk.zwander.common.tools

import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.serialization.xml.buildXml
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.data.BinaryFileInfo

/**
 * Handle some requests to Samsung's servers.
 */
@OptIn(DangerousInternalIoApi::class)
object Request {
    /**
     * Generate a logic-check for a given input.
     * @param input the value that needs a logic-check.
     * @param nonce the nonce used to generate the logic-check.
     * @return the logic-check.
     */
    fun getLogicCheck(input: String, nonce: String): String {
        if (input.length < 16) {
            throw IllegalArgumentException("Input is too short")
        }

        return nonce.map { input[it.toInt() and 0xf] }.joinToString("")
    }

    /**
     * Generate the XML needed to perform a binary inform.
     * @param fw the firmware string.
     * @param model the device model.
     * @param region the device region.
     * @param nonce the session nonce.
     * @return the needed XML.
     */
    fun createBinaryInform(fw: String, model: String, region: String, nonce: String): String {
        val xml = buildXml("FUSMsg") {
            node("FUSHdr") {
                node("ProtoVer") {
                    text("1.0")
                }
            }
            node("FUSBody") {
                node("Put") {
                    node("ACCESS_MODE") {
                        node("Data") {
                            text("2")
                        }
                    }
                    node("BINARY_NATURE") {
                        node("Data") {
                            text("1")
                        }
                    }
                    node("CLIENT_PRODUCT") {
                        node("Data") {
                            text("Smart Switch")
                        }
                    }
                    node("DEVICE_FW_VERSION") {
                        node("Data") {
                            text(fw)
                        }
                    }
                    node("DEVICE_LOCAL_CODE") {
                        node("Data") {
                            text(region)
                        }
                    }
                    node("DEVICE_MODEL_NAME") {
                        node("Data") {
                            text(model)
                        }
                    }
                    node("LOGIC_CHECK") {
                        node("Data") {
                            text(getLogicCheck(fw, nonce))
                        }
                    }
                }
            }
        }

        return xml.outerXml
    }

    /**
     * Generate the XML needed to perform a binary init.
     * @param fileName the name of the firmware file.
     * @param nonce the session nonce.
     * @return the needed XML.
     */
    fun createBinaryInit(fileName: String, nonce: String): String {
        val xml = buildXml("FUSMsg") {
            node("FUSHdr") {
                node("ProtoVer") {
                    text("1.0")
                }
            }
            node("FUSBody") {
                node("Put") {
                    node("BINARY_FILE_NAME") {
                        node("Data") {
                            text(fileName)
                        }
                    }
                    node("LOGIC_CHECK") {
                        node("Data") {
                            val special = fileName.split(".").first()
                                .run { slice(this.length - (16 % this.length)..this.lastIndex) }
                            text(getLogicCheck(special, nonce))
                        }
                    }
                }
            }
        }
        
        return xml.outerXml
    }

    /**
     * Retrieve the file information for a given firmware.
     * @param client the FusClient used to request the data.
     * @param fw the firmware version string.
     * @param model the device model.
     * @param region the device region.
     * @return a BinaryFileInfo instance representing the file.
     */
    suspend fun getBinaryFile(client: FusClient, fw: String, model: String, region: String): BinaryFileInfo {
        val request = createBinaryInform(fw, model, region, client.nonce)
        val response = client.makeReq(FusClient.Request.BINARY_INFORM, request)
        
        val responseXml = Xml(response)
        
        val status = responseXml.child("FUSBody")
            ?.child("Results")
            ?.child("Status")
            ?.text?.toInt()!!
        
        if (status != 200) {
            throw Exception("Bad return status: $status")
        }
        
        val size = responseXml.child("FUSBody")
            ?.child("Put")
            ?.child("BINARY_BYTE_SIZE")
            ?.child("Data")
            ?.text?.toLong()!!

        val fileName = responseXml.child("FUSBody")
            ?.child("Put")
            ?.child("BINARY_NAME")
            ?.child("Data")
            ?.text!!

        val path = responseXml.child("FUSBody")
            ?.child("Put")
            ?.child("MODEL_PATH")
            ?.child("Data")
            ?.text!!

        val crc32 = responseXml.child("FUSBody")
            ?.child("Put")
            ?.child("BINARY_CRC")
            ?.child("Data")
            ?.text?.toLong()

        return BinaryFileInfo(path, fileName, size, crc32)
    }
}