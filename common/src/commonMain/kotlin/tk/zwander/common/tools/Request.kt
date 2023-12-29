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
import tk.zwander.common.util.CrossPlatformBugsnag
import tk.zwander.common.util.dataNode
import tk.zwander.common.util.invoke
import tk.zwander.common.util.textNode
import tk.zwander.samloaderkotlin.resources.MR

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

        return buildString {
            nonce.forEach { char ->
                append(input[char.code and 0xf])
            }
        }
    }

    suspend fun performBinaryInformRetry(client: FusClient, fw: String, model: String, region: String, imeiSerial: String, includeNonce: Boolean): Pair<String, Xml> {
        val splitImeiSerial = imeiSerial.split("\n")

        var latestRequest = ""
        var latestResult: Xml = Xml.Text("")
        var latestError: Throwable? = null

        splitImeiSerial.forEach { imei ->
            latestRequest = createBinaryInform(fw, model, region, client.getNonce(), imei)

            latestResult = try {
                Xml.parse(client.makeReq(FusClient.Request.BINARY_INFORM, latestRequest, includeNonce))
            } catch (e: Throwable) {
                latestError = e
                Xml.Text("")
                return@forEach
            }

            latestResult.let { result ->
                val status = result.child("FUSBody")
                    ?.child("Results")
                    ?.child("Status")
                    ?.text

                println("Status $status")

                if (status == "200") {
                    return (latestRequest to result)
                }
            }
        }

        latestError?.let { throw it }

        return (latestRequest to latestResult)
    }

    /**
     * Generate the XML needed to perform a binary inform.
     * @param fw the firmware string.
     * @param model the device model.
     * @param region the device region.
     * @param nonce the session nonce.
     * @return the needed XML.
     */
    fun createBinaryInform(fw: String, model: String, region: String, nonce: String, imeiSerial: String): String {
        val split = fw.split("/")
        val (pda, csc, phone, data) = Array(4) { split.getOrNull(it) }
        val logicCheck = try {
            getLogicCheck(fw, nonce)
        } catch (e: Throwable) {
            e.printStackTrace()
            ""
        }

        val xml = buildXml("FUSMsg") {
            node("FUSHdr") {
                textNode("ProtoVer", "1.0")
                textNode("SessionID", "0")
                textNode("MsgID", "1")
            }
            node("FUSBody") {
                node("Put") {
                    dataNode("ACCESS_MODE", "2")
                    dataNode("BINARY_NATURE", "1")
                    dataNode("CLIENT_PRODUCT", "Smart Switch")
                    dataNode("CLIENT_VERSION", "4.3.23123_1")
                    dataNode("DEVICE_IMEI_PUSH", imeiSerial)

                    dataNode("DEVICE_FW_VERSION", fw)
                    dataNode("DEVICE_LOCAL_CODE", region)
                    dataNode("DEVICE_AID_CODE", region)
                    dataNode("DEVICE_MODEL_NAME", model)
                    dataNode("LOGIC_CHECK", logicCheck)
                    dataNode("DEVICE_CONTENTS_DATA_VERSION", data ?: "")
                    dataNode("DEVICE_CSC_CODE2_VERSION", csc ?: "")
                    dataNode("DEVICE_PDA_CODE1_VERSION", pda ?: "")
                    dataNode("DEVICE_PHONE_FONT_VERSION", phone ?: "")

                    node("CLIENT_LANGUAGE") {
                        textNode("Type", "String")
                        textNode("Type", "ISO 3166-1-alpha-3")
                        textNode("Data", "1033")
                    }

                    // Some regions need extra properties specified.
                    // TODO: Make these settable in the UI?
                    val (cc, mcc, mnc) = when (region) {
                        "EUX" -> Triple("DE", "262", "01")
                        "EUY" -> Triple("RS", "220", "01")
                        else -> Triple(null, null, null)
                    }

                    cc?.let { dataNode("DEVICE_CC_CODE", it) }
                    mcc?.let { dataNode("MCC_NUM", it) }
                    mnc?.let { dataNode("MNC_NUM", it) }
                }

                node("Get") {
                    textNode("CmdID", "2")
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
        val logicCheck = run {
            val special = fileName.split(".").first()
                .run { slice(this.length - (16 % this.length)..this.lastIndex) }
            getLogicCheck(special, nonce)
        }

        val xml = buildXml("FUSMsg") {
            node("FUSHdr") {
                textNode("ProtoVer", "1.0")
            }
            node("FUSBody") {
                node("Put") {
                    dataNode("BINARY_FILE_NAME", fileName)
                    dataNode("LOGIC_CHECK", logicCheck)
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
    suspend fun getBinaryFile(
        client: FusClient,
        fw: String,
        model: String,
        region: String,
        imeiSerial: String,
    ): FetchResult.GetBinaryFileResult {
        val (request, responseXml) = try {
            performBinaryInformRetry(client, fw.uppercase(), model, region, imeiSerial, false)
        } catch (e: Exception) {
            CrossPlatformBugsnag.notify(e)

            return FetchResult.GetBinaryFileResult(
                error = e,
                rawOutput = mapOf(
                    "firmware" to fw,
                    "model" to model,
                    "region" to region,
                ).toString(),
                requestBody = "",
            )
        }

        try {
            val status = responseXml.child("FUSBody")
                ?.child("Results")
                ?.child("Status")
                ?.text

            if (status == "F01") {
                return FetchResult.GetBinaryFileResult(
                    error = Exception(MR.strings.invalidFirmwareError()),
                    rawOutput = responseXml.toString(),
                    requestBody = request,
                )
            }

            if (status != "200") {
                return FetchResult.GetBinaryFileResult(
                    error = Exception(MR.strings.badReturnStatus(status.toString())),
                    rawOutput = responseXml.toString(),
                    requestBody = request,
                )
            }

            val noBinaryError = {
                FetchResult.GetBinaryFileResult(
                    error = Exception(MR.strings.noBinaryFile(model, region)),
                    rawOutput = responseXml.toString(),
                    requestBody = request,
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
                                ?.text ?: this?.child("LOGIC_VALUE_HOME")
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
                    error = VersionCheckException(MR.strings.versionCheckError()),
                    requestBody = request,
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

                val (servedCsc, cscSuffix) = cscFile!!.split("_")
                    .run { get(cscIndex!!) to getOrNull(cscIndex + 1) }
                val (servedCp, cpSuffix) = cpFile?.split("_").run {
                    this?.getOrNull(cpIndex ?: -1) to this?.getOrNull(
                        cpIndex?.plus(1) ?: -1
                    )
                }
                val (servedPda, pdaSuffix) = pdaFile?.split("_").run {
                    this?.getOrNull(pdaIndex ?: -1) to this?.getOrNull(
                        pdaIndex?.plus(1) ?: -1
                    )
                }

                val served = "$version/$servedCsc/${servedCp ?: version}/${servedPda ?: version}"

                val versionSuffixMatch = fwVersionSuffix == versionSuffix
                val cscSuffixMatch = fwCscSuffix == cscSuffix
                val cpSuffixMatch = fwCpSuffix == (cpSuffix ?: versionSuffix)
                val pdaSuffixMatch = fwPdaSuffix == (pdaSuffix ?: versionSuffix)

                if (served != fw && !versionSuffixMatch && !cscSuffixMatch && !cpSuffixMatch && !pdaSuffixMatch) {
                    return FetchResult.GetBinaryFileResult(
                        info = generateInfo(),
                        error = VersionMismatchException(MR.strings.versionMismatch(fw, served)),
                        requestBody = request,
                    )
                }
            }

            return FetchResult.GetBinaryFileResult(
                info = generateInfo(),
                requestBody = request,
            )
        } catch (e: Exception) {
            return FetchResult.GetBinaryFileResult(
                error = e,
                rawOutput = responseXml.toString(),
                requestBody = request,
            )
        }
    }
}
