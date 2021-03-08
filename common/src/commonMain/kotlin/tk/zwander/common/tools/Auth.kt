package tk.zwander.common.tools

import com.soywiz.krypto.AES
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.Base64
import kotlinx.io.core.toByteArray

/**
 * Handle all the encryption stuff.
 */
object Auth {
    /**
     * Decryption keys for the firmware and other data.
     */
    const val KEY_1 = "hqzdurufm2c8mf6bsjezu1qgveouv7c7"
    const val KEY_2 = "w13r4cvf4hctaujv"

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

        return AES.encryptAesCbc(paddedInput, key, iv, Padding.NoPadding)
    }

    /**
     * Decrypt data using AES CBC with custom padding.
     * @param input the data to decrypt.
     * @param key the key to use for decryption.
     * @return the decrypted data.
     */
    fun aesDecrypt(input: ByteArray, key: ByteArray): ByteArray {
        val iv = key.slice(0 until 16).toByteArray()

        return unpad(AES.decryptAesCbc(input, key, iv, Padding.NoPadding))
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
        val keyData = nonce.map { (it.toInt() % 16).toByte() }.toByteArray()
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

    val BA = KEY_1.toByteArray()
    val ENC = Base64.encode(KEY_1.toByteArray())
}