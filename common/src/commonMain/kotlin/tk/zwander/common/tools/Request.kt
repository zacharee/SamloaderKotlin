package tk.zwander.common.tools

import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import korlibs.crypto.MD5
import korlibs.io.serialization.xml.Xml
import korlibs.io.serialization.xml.buildXml
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.data.FetchResult
import tk.zwander.common.data.exception.VersionCheckException
import tk.zwander.common.data.exception.VersionMismatchException
import tk.zwander.samloaderkotlin.strings

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
            return ""
        }

        return nonce.toCharArray().joinToString("") { "${input[it.code and 0xf]}" }
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
        val split = fw.split("/")
        val (pda, csc, phone, data) = Array(4) { split.getOrNull(it) }

        val xml = buildXml("FUSMsg") {
            node("FUSHdr") {
                node("ProtoVer") {
                    text("1.0")
                }
                node("SessionID") {
                    text("0")
                }
                node("MsgID") {
                    text("1")
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
                    node("CLIENT_LANGUAGE") {
                        node("Type") {
                            text("String")
                        }
                        node("Type") {
                            text("ISO 3166-1-alpha-3")
                        }
                        node("Data") {
                            text("1033")
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
                            text(try {
                                getLogicCheck(fw, nonce)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                ""
                            })
                        }
                    }
                    node("DEVICE_CONTENTS_DATA_VERSION") {
                        node("Data") {
                            text(data ?: "")
                        }
                    }
                    node("DEVICE_CSC_CODE2_VERSION") {
                        node("Data") {
                            text(csc ?: "")
                        }
                    }
                    node("DEVICE_PDA_CODE1_VERSION") {
                        node("Data") {
                            text(pda ?: "")
                        }
                    }
                    node("DEVICE_PHONE_FONT_VERSION") {
                        node("Data") {
                            text(phone ?: "")
                        }
                    }
                    node("DEVICE_PLATFORM") {
                        node("Data") {
                            text("Android")
                        }
                    }
                }
                node("Get") {
                    node("CmdID") {
                        text("2")
                    }
                    node("LATEST_FW_VERSION")
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
        val request = try {
            createBinaryInform(fw.uppercase(), model, region, client.getNonce())
        } catch (e: Throwable) {
            return FetchResult.GetBinaryFileResult(
                error = Exception(strings.badReturnStatus(e.message.toString()), e)
            )
        }
        val response = client.makeReq(FusClient.Request.BINARY_INFORM, request, false)

        val responseXml = Xml.parse(response)

        try {
            val status = responseXml.child("FUSBody")
                ?.child("Results")
                ?.child("Status")
                ?.text

            if (status == "F01") {
                return FetchResult.GetBinaryFileResult(
                    error = Exception(strings.invalidFirmwareError()),
                    rawOutput = responseXml.toString()
                )
            }

            if (status != "200") {
                return FetchResult.GetBinaryFileResult(
                    error = Exception(strings.badReturnStatus(status.toString())),
                    rawOutput = responseXml.toString()
                )
            }

            val noBinaryError = {
                FetchResult.GetBinaryFileResult(
                    error = Exception(strings.noBinaryFile(model, region)),
                    rawOutput = responseXml.toString()
                )
            }

            val size = responseXml.child("FUSBody")
                ?.child("Put")
                ?.child("BINARY_BYTE_SIZE")
                ?.child("Data")
                ?.text.run {
                    if (isNullOrBlank()) {
                        return noBinaryError()
                    } else {
                        toLong()
                    }
                }

            val fileName = responseXml.child("FUSBody")
                ?.child("Put")
                ?.child("BINARY_NAME")
                ?.child("Data")
                ?.text ?: return noBinaryError()

            fun getIndex(file: String?): Int? {
                if (file.isNullOrBlank()) return null

                val fileSplit = file.split("_")
                val modelSuffix = model.split("-").getOrElse(1) { model }

                return fileSplit.indexOfFirst {
                    it.startsWith(modelSuffix) ||
                            it.startsWith(model.replace("-", ""))
                }
            }

            fun generateInfo(): BinaryFileInfo {
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
                        .run {
                            this?.child("LOGIC_VALUE_FACTORY")
                                ?.child("Data")
                                ?.text ?:
                            this?.child("LOGIC_VALUE_HOME")
                                ?.child("Data")
                                ?.text!!
                        }

                    val decKey = getLogicCheck(fwVer, logicVal)

                    MD5.digest(decKey.toByteArray()).bytes
                } catch (e: Exception) {
                    null
                }

                return BinaryFileInfo(path, fileName, size, crc32, v4Key)
            }

            fun getSuffix(str: String): String? {
                return str.split("_").getOrNull(1)
            }

            val dataKeys = arrayOf(
                "DEVICE_USER_DATA_FILE",
                "DEVICE_BOOT_FILE",
                "DEVICE_PDA_CODE1_FILE"
            )

            val dataFile = dataKeys.firstNotNullOfOrNull {
                responseXml.child("FUSBody")
                    ?.child("Put")
                    ?.child(it)
                    ?.child("Data")
                    ?.text.run { if (isNullOrBlank()) null else this }
            }

            if (dataFile.isNullOrBlank()) {
                return FetchResult.GetBinaryFileResult(
                    info = generateInfo(),
                    error = VersionCheckException(strings.versionCheckError())
                )
            }

            val dataIndex = getIndex(dataFile)

            val cscFile = responseXml.child("FUSBody")
                ?.child("Put")
                ?.child("DEVICE_CSC_HOME_FILE")
                ?.text.run {
                    if (isNullOrBlank()) {
                        responseXml.child("FUSBody")
                            ?.child("Put")
                            ?.child("DEVICE_CSC_FILE")
                            ?.child("Data")
                            ?.text
                    } else {
                        this
                    }
                }

            val cscIndex = getIndex(cscFile)

            val cpFile = responseXml.child("FUSBody")
                ?.child("Put")
                ?.child("DEVICE_PHONE_FONT_FILE")
                ?.text

            val cpIndex = getIndex(cpFile)

            val pdaFile = responseXml.child("FUSBody")
                ?.child("Put")
                ?.child("DEVICE_PDA_CODE1_FILE")
                ?.text

            val pdaIndex = getIndex(pdaFile)

            dataFile.let { f ->
                val (fwVersion, fwCsc, fwCp, fwPda) = fw.split("/")
                val fwVersionSuffix = getSuffix(fwVersion)
                val fwCscSuffix = getSuffix(fwCsc)
                val fwCpSuffix = getSuffix(fwCp)
                val fwPdaSuffix = getSuffix(fwPda)

                val split = f.split("_")
                val (version, versionSuffix) = split[dataIndex!!] to split.getOrNull(dataIndex + 1)

                val (servedCsc, cscSuffix) = cscFile!!.split("_").run { get(cscIndex!!) to getOrNull(cscIndex + 1) }
                val (servedCp, cpSuffix) = cpFile?.split("_").run { this?.getOrNull(cpIndex ?: -1) to this?.getOrNull(cpIndex?.plus(1) ?: - 1) }
                val (servedPda, pdaSuffix) = pdaFile?.split("_").run { this?.getOrNull(pdaIndex ?: -1) to this?.getOrNull(pdaIndex?.plus(1) ?: -1) }

                val served = "$version/$servedCsc/${servedCp ?: version}/${servedPda ?: version}"

                val versionSuffixMatch = fwVersionSuffix == versionSuffix
                val cscSuffixMatch = fwCscSuffix == cscSuffix
                val cpSuffixMatch = fwCpSuffix == (cpSuffix ?: versionSuffix)
                val pdaSuffixMatch = fwPdaSuffix == (pdaSuffix ?: versionSuffix)

                if (served != fw && !versionSuffixMatch && !cscSuffixMatch && !cpSuffixMatch && !pdaSuffixMatch) {
                    return FetchResult.GetBinaryFileResult(
                        info = generateInfo(),
                        error = VersionMismatchException(strings.versionMismatch(fw, served))
                    )
                }
            }

            return FetchResult.GetBinaryFileResult(
                info = generateInfo()
            )
        } catch (e: Exception) {
            return FetchResult.GetBinaryFileResult(
                error = e,
                rawOutput = responseXml.toString()
            )
        }
    }
}
