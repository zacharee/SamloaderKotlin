package tk.zwander.common.tools

import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import korlibs.crypto.AES
import korlibs.crypto.CipherPadding
import korlibs.crypto.MD5
import korlibs.crypto.encoding.Base64
import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncOutputStream
import korlibs.io.util.checksum.CRC32
import kotlinx.coroutines.*
import tk.zwander.common.util.Averager
import tk.zwander.common.util.firstElementByTagName
import tk.zwander.common.util.streamOperationWithProgress
import kotlin.time.*

/**
 * Handle encryption and decryption stuff.
 */
@DangerousInternalIoApi
@ExperimentalTime
object CryptUtils {
    /**
     * Decryption keys for the firmware and other data.
     */
    const val KEY_1 = "vicopx7dqu06emacgpnpy8j8zwhduwlh"
    const val KEY_2 = "9u7qab84rpc16gvk"

    /**
     * Samsung uses its own padding for its AES
     * encryption, so decrypted bytes need to be manually
     * unpadded.
     *
     * @param d the data to unpad.
     * @return the unpadded data.
     */
    fun unpad(d: ByteArray): ByteArray {
        val lastByte = d.last().toInt()
        val padIndex = (d.size - (lastByte % d.size))

        return d.slice(0 until padIndex).toByteArray()
    }

    /**
     * Manually pad data to be encrypted.
     *
     * @param d the data to pad.
     * @return the padded data.
     */
    fun pad(d: ByteArray): ByteArray {
        val size = 16 - (d.size % 16)
        val array = ByteArray(size)

        for (i in 0 until size) {
            array[i] = size.toByte()
        }

        return d + array
    }

    /**
     * Encrypt data using AES CBC with custom padding.
     * @param input the data to encrypt.
     * @param key the key to use for encryption.
     * @return the encrypted data.
     */
    fun aesEncrypt(input: ByteArray, key: ByteArray): ByteArray {
        val paddedInput = pad(input)
        val iv = key.slice(0 until 16).toByteArray()

        return AES.encryptAesCbc(paddedInput, key, iv, CipherPadding.NoPadding)
    }

    /**
     * Decrypt data using AES CBC with custom padding.
     * @param input the data to decrypt.
     * @param key the key to use for decryption.
     * @return the decrypted data.
     */
    fun aesDecrypt(input: ByteArray, key: ByteArray): ByteArray {
        val iv = key.slice(0 until 16).toByteArray()

        return unpad(AES.decryptAesCbc(input, key, iv, CipherPadding.NoPadding))
    }

    /**
     * Generate a key given a specific input.
     * @param input the input seed.
     * @return the generated key.
     */
    fun getFKey(input: ByteArray): ByteArray {
        var key = ""

        for (i in 0 until 16) {
            key += KEY_1[input[i].toInt() % KEY_1.length]
        }

        key += KEY_2

        return key.toByteArray()
    }

    /**
     * Generate an auth token with a given nonce.
     * @param nonce the nonce seed.
     * @return an auth token based on the nonce.
     */
    fun getAuth(nonce: String): String {
        val keyData = nonce.map { (it.code % 16).toByte() }.toByteArray()
        val fKey = getFKey(keyData)

        return Base64.encode(aesEncrypt(nonce.toByteArray(), fKey))
    }

    /**
     * Decrypt a provided nonce string.
     * @param input the nonce to decrypt.
     * @return the decrypted nonce.
     */
    fun decryptNonce(input: String): String {
        val d = Base64.decode(input)
        return aesDecrypt(d, KEY_1.toByteArray())
            .decodeToString()
    }

    /**
     * Retrieve the decryption key for a .enc4 firmware file.
     * @param client the [FusClient] instance to use.
     * @param version the firmware string corresponding to the file.
     * @param model the device model corresponding to the file.
     * @param region the device region corresponding to the file.
     * @return the decryption key for this firmware.
     */
    suspend fun getV4Key(client: FusClient, version: String, model: String, region: String, imeiSerial: String, tries: Int = 0): ByteArray {
        val (_, responseXml) = Request.performBinaryInformRetry(client, version.uppercase(), model, region, imeiSerial, true)

        return try {
            val fwVer = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Results")
                ?.firstElementByTagName("LATEST_FW_VERSION")
                ?.firstElementByTagName("Data")
                ?.text()!!

            val logicVal = responseXml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Put")
                .run {
                    this?.firstElementByTagName("LOGIC_VALUE_FACTORY")
                        ?.firstElementByTagName("Data")
                        ?.text() ?:
                    this?.firstElementByTagName("LOGIC_VALUE_HOME")
                        ?.firstElementByTagName("Data")
                        ?.text()!!
                }

            val decKey = Request.getLogicCheck(fwVer, logicVal)

            return MD5.digest(decKey.toByteArray()).bytes
        } catch (e: Exception) {
            if (tries > 4) {
                throw e
            } else {
                client.makeReq(FusClient.Request.GENERATE_NONCE)
                getV4Key(client, version, model, region, imeiSerial, tries + 1)
            }
        }
    }

    /**
     * Create the decryption key for a .enc2 firmware file.
     * @param version the firmware string corresponding to the file.
     * @param model the device model corresponding to the file.
     * @param region the device region corresponding to the file.
     * @return the decryption key for this firmware.
     */
    suspend fun getV2Key(version: String, model: String, region: String): ByteArray {
        val decKey = "${region}:${model}:${version}"
        return MD5.digest(decKey.toByteArray()).bytes
    }

    /**
     * Decrypt a provided file to a specified target, with a progress callback.
     * @param inf the encrypted file to decrypt.
     * @param outf where to store the decrypted firmware.
     * @param key the decryption key for this firmware.
     * @param length the size of the encrypted file.
     * @param progressCallback a callback to keep track of the progress.
     */
    suspend fun decryptProgress(
        inf: AsyncInputStream,
        outf: AsyncOutputStream,
        key: ByteArray,
        length: Long,
        chunkSize: Int = 0x300000,
        progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit
    ) {
        streamOperationWithProgress(
            input = inf,
            output = outf,
            size = length,
            chunkSize = chunkSize,
            progressCallback = progressCallback,
            operation = {
                AES.decryptAesEcb(it, key, CipherPadding.NoPadding)
            },
        )
    }

    /**
     * Check the CRC32 of a given encrypted firmware file.
     * @param enc the encrypted file to verify.
     * @param encSize the size of the encrypted file.
     * @param expected the expected CRC32.
     * @param progressCallback a callback to keep track of the progress.
     * @return true if the file's CRC32 matches the expected value.
     */
    suspend fun checkCrc32(
        enc: AsyncInputStream,
        encSize: Long,
        expected: Long,
        progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit
    ): Boolean {
        var crcVal = CRC32.initialValue

        coroutineScope {
            withContext(Dispatchers.Default) {
                val buffer = ByteArray(0x300000)

                var len: Int
                var count = 0L

                val averager = Averager()

                while (isActive) {
                    val nano = measureTime {
                        len = enc.read(buffer, 0, buffer.size)
                        count += len

                        if (len > 0) {
                            crcVal = CRC32.update(crcVal, buffer, 0, len)
                        }
                    }.inWholeNanoseconds

                    if (len <= 0) break

                    val lenF = len
                    val totalLenF = count

                    async {
                        averager.update(nano, lenF.toLong())
                        val (totalTime, totalRead, _) = averager.sum()

                        progressCallback(
                            totalLenF,
                            encSize,
                            (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong()
                        )
                    }
                }
            }
        }

        enc.close()

        return crcVal == expected.toInt()
    }

    /*
     * The MD5 checking methods are from CyanogenMod.
     *
     *
     * Copyright (C) 2012 The CyanogenMod Project
     *
     * * Licensed under the GNU GPLv2 license
     *
     * The text of the license can be found in the LICENSE file
     * or at https://www.gnu.org/licenses/gpl-2.0.txt
     */

    /**
     * Check an MD5 hash given an input stream and an expected value.
     * @param md5 the expected value.
     * @param updateFile the file to check.
     * @return true if the hashes match.
     */
    suspend fun checkMD5(md5: String, updateFile: AsyncInputStream?): Boolean {
        if (md5.isBlank() || updateFile == null) {
            return false
        }

        val calculatedDigest = calculateMD5(updateFile) ?: return false
        return calculatedDigest.equals(md5, ignoreCase = true)
            .also { updateFile.close() }
    }

    /**
     * Calculate an MD5 hash for a given input stream.
     * @param updateFile the file used to calculate.
     * @return the MD5 hash.
     */
    suspend fun calculateMD5(updateFile: AsyncInputStream): String? {
        val md5 = MD5.create()
        val buffer = ByteArray(8192)
        var read: Int
        return try {
            while (updateFile.read(buffer, 0, buffer.size).also { read = it } > 0) {
                md5.update(buffer, 0, read)
            }
            val hex = md5.digest().hex
            val output = hex.padStart(32, '0');
            output
        } catch (e: Exception) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                updateFile.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
