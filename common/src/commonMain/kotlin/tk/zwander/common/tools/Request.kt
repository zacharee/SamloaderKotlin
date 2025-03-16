package tk.zwander.common.tools

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.whyoleg.cryptography.DelicateCryptographyApi
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.data.FetchResult
import tk.zwander.common.data.exception.VersionCheckException
import tk.zwander.common.data.exception.VersionException
import tk.zwander.common.data.exception.VersionMismatchException
import tk.zwander.common.exceptions.DownloadError
import tk.zwander.common.exceptions.NoBinaryFileError
import tk.zwander.common.util.CrossPlatformBugsnag
import tk.zwander.common.util.dataNode
import tk.zwander.common.util.firstElementByTagName
import tk.zwander.common.util.invoke
import tk.zwander.common.util.isAccessoryModel
import tk.zwander.common.util.textNode
import tk.zwander.samloaderkotlin.resources.MR
import kotlin.time.ExperimentalTime

/**
 * Handle some requests to Samsung's servers.
 */
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

    suspend fun performBinaryInformRetry(
        fw: String,
        model: String,
        region: String,
        imeiSerial: String,
        includeNonce: Boolean,
    ): Pair<String, Document> {
        val splitImeiSerial = imeiSerial.split("\n").flatMap { it.split(";") }

        var latestRequest = ""
        var latestResult: Document = Ksoup.parse("")
        var latestError: Throwable? = null

        splitImeiSerial.forEachIndexed { index, imei ->
            latestRequest = createBinaryInform(fw, model, region, FusClient.getNonce(), imei)

            if (index % 10 == 0) {
                delay(1000)
            }

            latestResult = try {
                val response =
                    FusClient.makeReq(FusClient.Request.BINARY_INFORM, latestRequest, includeNonce)

                Ksoup.parse(response)
            } catch (e: Throwable) {
                latestError = e
                e.printStackTrace()
                return@forEachIndexed
            }

            latestResult.let { result ->
                val status = result.firstElementByTagName("FUSBody")
                    ?.firstElementByTagName("Results")
                    ?.firstElementByTagName("Status")
                    ?.text()

                println("Status for IMEI $imei: $status")

                if (status != "408") {
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
    private fun createBinaryInform(
        fw: String,
        model: String,
        region: String,
        nonce: String,
        imeiSerial: String
    ): String {
        val split = fw.split("/")
        val (pda, csc, phone, data) = Array(4) { split.getOrNull(it) }
        val logicCheck = try {
            getLogicCheck(fw, nonce)
        } catch (e: Throwable) {
            e.printStackTrace()
            ""
        }

        val xml = xml("FUSMsg") {
            "FUSHdr" {
                textNode("ProtoVer", "1.0")
                textNode("SessionID", "0")
                textNode("MsgID", "1")
            }
            "FUSBody" {
                "Put" {
                    dataNode("ACCESS_MODE", "2")
                    dataNode("BINARY_NATURE", "1")
                    dataNode("CLIENT_PRODUCT", "Smart Switch")
                    dataNode("CLIENT_VERSION", "4.3.23123_1")
                    dataNode("DEVICE_IMEI_PUSH", imeiSerial.trim())

                    dataNode("DEVICE_FW_VERSION", fw.trim())
                    dataNode("DEVICE_LOCAL_CODE", region.trim())
                    dataNode("DEVICE_AID_CODE", region.trim())
                    dataNode("DEVICE_MODEL_NAME", model.trim())
                    dataNode("LOGIC_CHECK", logicCheck.trim())
                    dataNode("DEVICE_CONTENTS_DATA_VERSION", data?.trim() ?: "")
                    dataNode("DEVICE_CSC_CODE2_VERSION", csc?.trim() ?: "")
                    dataNode("DEVICE_PDA_CODE1_VERSION", pda?.trim() ?: "")
                    dataNode("DEVICE_PHONE_FONT_VERSION", phone?.trim() ?: "")

                    "CLIENT_LANGUAGE" {
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

                "Get" {
                    textNode("CmdID", "2")
                    "LATEST_FW_VERSION"()
                }
            }
        }

        return xml.toString(PrintOptions(singleLineTextElements = true))
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

        val xml = xml("FUSMsg") {
            "FUSHdr" {
                textNode("ProtoVer", "1.0")
            }
            "FUSBody" {
                "Put" {
                    dataNode("BINARY_FILE_NAME", fileName)
                    dataNode("LOGIC_CHECK", logicCheck)
                }
            }
        }

        return xml.toString(PrintOptions(singleLineTextElements = true))
    }

    suspend fun retrieveBinaryFileInfo(
        fw: String,
        model: String,
        region: String,
        imeiSerial: String,
        onFinish: suspend (String) -> Unit,
        onVersionException: (suspend (VersionException, BinaryFileInfo?) -> Unit)? = null,
        shouldReportError: suspend (Exception) -> Boolean = { true },
    ): BinaryFileInfo? {
        val result = getBinaryFile(
            fw, model, region, imeiSerial,
        )

        val (info, error, output, requestBody) = result

        if (error is VersionException && onVersionException != null) {
            onVersionException(error, info)
            return null
        } else if (error != null) {
            onFinish("${error.message ?: MR.strings.error()}\n\n${output}")
            if (result.isReportableCode() &&
                !output.contains("Incapsula") &&
                error !is CancellationException &&
                shouldReportError(error) &&
                !model.isAccessoryModel
            ) {
                CrossPlatformBugsnag.notify(DownloadError(requestBody, output, error))
            }
        }

        return info
    }

    /**
     * Retrieve the file information for a given firmware.
     * @param fw the firmware version string.
     * @param model the device model.
     * @param region the device region.
     * @return a BinaryFileInfo instance representing the file.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun getBinaryFile(
        fw: String,
        model: String,
        region: String,
        imeiSerial: String,
    ): FetchResult.GetBinaryFileResult {
        val (request, responseXml) = try {
            performBinaryInformRetry(fw.uppercase(), model, region, imeiSerial, false)
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
            val status = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Results")
                ?.firstElementByTagName("Status")
                ?.text()

            if (status == "F01") {
                return FetchResult.GetBinaryFileResult(
                    error = Exception(MR.strings.invalidFirmwareError()),
                    rawOutput = responseXml.toString(),
                    requestBody = request,
                    responseCode = status,
                )
            }

            if (status == "408") {
                return FetchResult.GetBinaryFileResult(
                    error = Exception(MR.strings.invalid_imei_or_serial()),
                    rawOutput = responseXml.toString(),
                    requestBody = request,
                    responseCode = status,
                )
            }

            if (status != "200") {
                return FetchResult.GetBinaryFileResult(
                    error = Exception(MR.strings.badReturnStatus(status.toString())),
                    rawOutput = responseXml.toString(),
                    requestBody = request,
                    responseCode = status,
                )
            }

            val noBinaryError = {
                FetchResult.GetBinaryFileResult(
                    error = NoBinaryFileError(model, region),
                    rawOutput = responseXml.toString(),
                    requestBody = request,
                    responseCode = status,
                )
            }

            val size = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Put")
                ?.firstElementByTagName("BINARY_BYTE_SIZE")
                ?.firstElementByTagName("Data")
                ?.text().run {
                    if (isNullOrBlank()) {
                        return noBinaryError()
                    } else {
                        toLong()
                    }
                }

            val fileName = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Put")
                ?.firstElementByTagName("BINARY_NAME")
                ?.firstElementByTagName("Data")
                ?.text() ?: return noBinaryError()

            fun getIndex(file: String?): Int? {
                if (file.isNullOrBlank()) return null

                val fileSplit = file.split("_")
                val modelSuffix = model.split("-").getOrElse(1) { model }

                return fileSplit.indexOfFirst {
                    it.startsWith(modelSuffix) ||
                            it.startsWith(model.replace("-", ""))
                }
            }

            suspend fun generateInfo(): BinaryFileInfo {
                val path = responseXml.firstElementByTagName("FUSBody")
                    ?.firstElementByTagName("Put")
                    ?.firstElementByTagName("MODEL_PATH")
                    ?.firstElementByTagName("Data")
                    ?.text()!!

                val crc32 = responseXml.firstElementByTagName("FUSBody")
                    ?.firstElementByTagName("Put")
                    ?.firstElementByTagName("BINARY_CRC")
                    ?.firstElementByTagName("Data")
                    ?.text()?.toLongOrNull()

                val v4Key = try {
                    responseXml.extractV4Key() ?: CryptUtils.getV4Key(fw, model, region, imeiSerial)
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
                responseXml.firstElementByTagName("FUSBody")
                    ?.firstElementByTagName("Put")
                    ?.firstElementByTagName(it)
                    ?.firstElementByTagName("Data")
                    ?.text().run { if (isNullOrBlank()) null else this }
            }

            if (dataFile.isNullOrBlank()) {
                return FetchResult.GetBinaryFileResult(
                    info = generateInfo(),
                    error = VersionCheckException(MR.strings.versionCheckError()),
                    requestBody = request,
                    responseCode = status,
                )
            }

            val dataIndex = getIndex(dataFile)

            val cscFile = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Put")
                ?.firstElementByTagName("DEVICE_CSC_HOME_FILE")
                ?.text().run {
                    if (isNullOrBlank()) {
                        responseXml.firstElementByTagName("FUSBody")
                            ?.firstElementByTagName("Put")
                            ?.firstElementByTagName("DEVICE_CSC_FILE")
                            ?.firstElementByTagName("Data")
                            ?.text()
                    } else {
                        this
                    }
                }

            val cscIndex = getIndex(cscFile)

            val cpFile = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Put")
                ?.firstElementByTagName("DEVICE_PHONE_FONT_FILE")
                ?.text()

            val cpIndex = getIndex(cpFile)

            val pdaFile = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Put")
                ?.firstElementByTagName("DEVICE_PDA_CODE1_FILE")
                ?.text()

            val pdaIndex = getIndex(pdaFile)

            dataFile.let { f ->
                val (fwVersion, fwCsc, fwCp, fwPda) = fw.split("/")
                val fwCscSuffix = getSuffix(fwCsc)
                val fwCpSuffix = getSuffix(fwCp)

                val split = f.split("_")
                val (version, versionSuffix) = split[dataIndex!!] to split.getOrNull(dataIndex + 1)

                val (servedCsc, cscSuffix) = cscFile?.split("_")
                    ?.takeIf { cscIndex != null }
                    ?.run { getOrNull(cscIndex!!) to getOrNull(cscIndex + 1) } ?: (null to null)
                val (servedCp, cpSuffix) = cpFile?.split("_").run {
                    this?.getOrNull(cpIndex ?: -1) to this?.getOrNull(
                        cpIndex?.plus(1) ?: -1
                    )
                }
                val servedPda = pdaFile?.split("_")?.getOrNull(pdaIndex ?: -1)

                val served =
                    "$version/${servedCsc ?: versionSuffix}/${servedCp ?: version}/${servedPda ?: version}"

                val cscMatch = fwCsc == (servedCsc ?: versionSuffix)
                val cpMatch = fwCp == (servedCp ?: version)
                val fwVersionMatch = fwVersion == version
                val fwPdaMatch = fwPda == servedPda

                val cscSuffixMatch = if (fwCscSuffix != null) fwCscSuffix == cscSuffix else true
                val cpSuffixMatch = if (fwCpSuffix != null) fwCpSuffix == cpSuffix else true

                if (served != fw || !cscMatch || !cpMatch || !fwVersionMatch ||
                    !fwPdaMatch || !cscSuffixMatch || !cpSuffixMatch
                ) {
                    return FetchResult.GetBinaryFileResult(
                        info = generateInfo(),
                        error = VersionMismatchException(MR.strings.versionMismatch(fw, served)),
                        requestBody = request,
                        responseCode = status,
                    )
                }
            }

            return FetchResult.GetBinaryFileResult(
                info = generateInfo(),
                requestBody = request,
                responseCode = status,
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

@OptIn(DelicateCryptographyApi::class)
fun Document.extractV4Key(): Pair<ByteArray, String>? {
    val fwVer = firstElementByTagName("FUSBody")
        ?.firstElementByTagName("Results")
        ?.firstElementByTagName("LATEST_FW_VERSION")
        ?.firstElementByTagName("Data")
        ?.text()

    val logicVal = firstElementByTagName("FUSBody")
        ?.firstElementByTagName("Put")
        .run {
            this?.firstElementByTagName("LOGIC_VALUE_FACTORY")
                ?.firstElementByTagName("Data")
                ?.text() ?: this?.firstElementByTagName("LOGIC_VALUE_HOME")
                ?.firstElementByTagName("Data")
                ?.text()
        }

    return if (fwVer != null && logicVal != null) {
        val decKey = Request.getLogicCheck(fwVer, logicVal)

        CryptUtils.md5Provider
            .hasher()
            .hashBlocking(decKey.toByteArray()) to decKey
    } else {
        null
    }
}
