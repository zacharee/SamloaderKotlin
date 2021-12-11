package tk.zwander.common.tools

import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.serialization.xml.buildXml
import com.soywiz.krypto.MD5
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.data.FetchResult

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

        return nonce.map { input[it.code and 0xf] }.joinToString("")
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
        val (pda, csc, phone, data) = fw.split("/")

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
                    node("CLIENT_VERSION") {
                        node("Data") {
                            text("4.1.16014_12")
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
                    node("DEVICE_CONTENTS_DATA_VERSION") {
                        node("Data") {
                            text(data)
                        }
                    }
                    node("DEVICE_CSC_CODE2_VERSION") {
                        node("Data") {
                            text(csc)
                        }
                    }
                    node("DEVICE_PDA_CODE1_VERSION") {
                        node("Data") {
                            text(pda)
                        }
                    }
                    node("DEVICE_PHONE_FONT_VERSION") {
                        node("Data") {
                            text(phone)
                        }
                    }
                    node("DEVICE_PLATFORM") {
                        node("Data") {
                            text("Android")
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
    suspend fun getBinaryFile(client: FusClient, fw: String, model: String, region: String): FetchResult.GetBinaryFileResult {
        val request = createBinaryInform(fw, model, region, client.getNonce())
        val response = client.makeReq(FusClient.Request.BINARY_INFORM, request)

        val responseXml = Xml(response)

        try {
            val status = responseXml.child("FUSBody")
                ?.child("Results")
                ?.child("Status")
                ?.text?.toInt()!!

            if (status != 200) {
                return FetchResult.GetBinaryFileResult(
                    error = Exception("Bad return status: $status", Exception(response)),
                    rawOutput = responseXml.toString()
                )
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

            val v4Key = try {
                val fwVer = responseXml.child("FUSBody")
                    ?.child("Results")
                    ?.child("LATEST_FW_VERSION")
                    ?.child("Data")
                    ?.text!!

                val logicVal = responseXml.child("FUSBody")
                    ?.child("Put")
                    ?.child("LOGIC_VALUE_FACTORY")
                    ?.child("Data")
                    ?.text!!

                val decKey = getLogicCheck(fwVer, logicVal)

                MD5.digest(decKey.toByteArray()).bytes
            } catch (e: Exception) {
                null
            }

            return FetchResult.GetBinaryFileResult(
                info = BinaryFileInfo(path, fileName, size, crc32, v4Key)
            )
        } catch (e: Exception) {
            return FetchResult.GetBinaryFileResult(
                error = e,
                rawOutput = responseXml.toString()
            )
        }
    }
}
