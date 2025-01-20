package tk.zwander.common.tools

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.MD5
import io.github.andreypfau.kotlinx.crypto.CRC32
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.bytestring.toHexString
import tk.zwander.common.util.DEFAULT_CHUNK_SIZE
import tk.zwander.common.util.streamOperationWithProgress
import tk.zwander.common.util.trackOperationProgress
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Handle encryption and decryption stuff.
 */
object CryptUtils {
    /**
     * Decryption keys for the firmware and other data.
     */
    private const val KEY_1 = "vicopx7dqu06emacgpnpy8j8zwhduwlh"
    private const val KEY_2 = "9u7qab84rpc16gvk"

    @OptIn(DelicateCryptographyApi::class)
    val md5Provider = CryptographyProvider.Default.get(MD5)
    val aesCbcProvider = CryptographyProvider.Default.get(AES.CBC)
    @OptIn(DelicateCryptographyApi::class)
    val aesEcbProvider = CryptographyProvider.Default.get(AES.ECB)

    /**
     * Samsung uses its own padding for its AES
     * encryption, so decrypted bytes need to be manually
     * unpadded.
     *
     * @param d the data to unpad.
     * @return the unpadded data.
     */
    private fun unpad(d: ByteArray): ByteArray {
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
    private fun pad(d: ByteArray): ByteArray {
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
    @OptIn(DelicateCryptographyApi::class)
    private fun aesEncrypt(input: ByteArray, key: ByteArray): ByteArray {
        val paddedInput = pad(input)
        val iv = key.slice(0 until 16).toByteArray()

        return aesCbcProvider
            .keyDecoder()
            .decodeFromByteArrayBlocking(AES.Key.Format.RAW, key)
            .cipher(padding = false)
            .encryptWithIvBlocking(iv, paddedInput)
    }

    /**
     * Decrypt data using AES CBC with custom padding.
     * @param input the data to decrypt.
     * @param key the key to use for decryption.
     * @return the decrypted data.
     */
    @OptIn(DelicateCryptographyApi::class)
    private fun aesDecrypt(input: ByteArray, key: ByteArray): ByteArray {
        val iv = key.slice(0 until 16).toByteArray()

        return unpad(
            aesCbcProvider
                .keyDecoder()
                .decodeFromByteArrayBlocking(AES.Key.Format.RAW, key)
                .cipher(padding = false)
                .decryptWithIvBlocking(iv, input)
        )
    }

    /**
     * Generate a key given a specific input.
     * @param input the input seed.
     * @return the generated key.
     */
    private fun getFKey(input: ByteArray): ByteArray {
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
    @OptIn(ExperimentalEncodingApi::class)
    fun getAuth(nonce: String): String {
        val keyData = nonce.map { (it.code % 16).toByte() }.toByteArray()
        val fKey = getFKey(keyData)

        return Base64.Default.encode(aesEncrypt(nonce.toByteArray(), fKey))
    }

    /**
     * Decrypt a provided nonce string.
     * @param input the nonce to decrypt.
     * @return the decrypted nonce.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun decryptNonce(input: String): String {
        val d = Base64.Default.decode(input)
        return aesDecrypt(d, KEY_1.toByteArray())
            .decodeToString()
    }

    /**
     * Retrieve the decryption key for a .enc4 firmware file.
     * @param version the firmware string corresponding to the file.
     * @param model the device model corresponding to the file.
     * @param region the device region corresponding to the file.
     * @return the decryption key for this firmware.
     */
    suspend fun getV4Key(version: String, model: String, region: String, imeiSerial: String, tries: Int = 0, includeNonce: Boolean = true): Pair<ByteArray, String>? {
        val (_, responseXml) = Request.performBinaryInformRetry(version.uppercase(), model, region, imeiSerial, includeNonce)

        return try {
            responseXml.extractV4Key()
        } catch (e: Exception) {
            if (tries > 4) {
                throw e
            } else {
                FusClient.makeReq(FusClient.Request.GENERATE_NONCE)
                getV4Key(version, model, region, imeiSerial, tries + 1)
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
    fun getV2Key(version: String, model: String, region: String): Pair<ByteArray, String> {
        val decKey = "${region}:${model}:${version}"

        return md5Provider.hasher().hashBlocking(decKey.toByteArray()) to decKey
    }

    /**
     * Decrypt a provided file to a specified target, with a progress callback.
     * @param inf the encrypted file to decrypt.
     * @param outf where to store the decrypted firmware.
     * @param key the decryption key for this firmware.
     * @param length the size of the encrypted file.
     * @param progressCallback a callback to keep track of the progress.
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun decryptProgress(
        inf: Source,
        outf: Sink,
        key: ByteArray,
        length: Long,
        chunkSize: Int = DEFAULT_CHUNK_SIZE,
        progressCallback: suspend (current: Long, max: Long, bps: Long) -> Unit,
    ) {
        val cipher = aesEcbProvider.keyDecoder()
            .decodeFromByteArrayBlocking(AES.Key.Format.RAW, key)
            .cipher(padding = false)

        streamOperationWithProgress(
            input = inf,
            output = outf,
            size = length,
            chunkSize = chunkSize,
            progressCallback = progressCallback,
            operation = {
                cipher.decryptBlocking(it)
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
        enc: Source?,
        encSize: Long,
        expected: Long,
        progressCallback: suspend (current: Long, max: Long, bps: Long) -> Unit,
    ): Boolean {
        if (enc == null) {
            return false
        }

        val buffer = ByteArray(DEFAULT_CHUNK_SIZE)
        val crc = CRC32()

        trackOperationProgress(
            size = encSize,
            progressCallback = progressCallback,
            operation = {
                val len = enc.readAtMostTo(buffer, 0, buffer.size)

                if (len > 0) {
                    crc.update(buffer, 0, len)
                }
                len.toLong()
            },
        )

        withContext(Dispatchers.IO) {
            enc.close()
        }

        return crc.intDigest() == expected.toInt()
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
    suspend fun checkMD5(md5: String, updateFile: Source?): Boolean {
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
    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun calculateMD5(updateFile: Source): String? {
        val md5 = md5Provider.hasher().createHashFunction()
        val buffer = ByteArray(8192)
        var read: Int
        return try {
            while (updateFile.readAvailable(buffer, 0, buffer.size).also { read = it } > 0) {
                md5.update(buffer, 0, read)
            }
            val hex = md5.hash().toHexString(format = HexFormat.UpperCase)
            val output = hex.padStart(32, '0')
            output
        } catch (e: Exception) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                withContext(Dispatchers.IO) {
                    updateFile.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
