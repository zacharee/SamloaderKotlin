package tk.zwander.common.tools

import com.soywiz.krypto.AES
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.Base64
import kotlinx.io.core.toByteArray

object Auth {
    const val KEY_1 = "hqzdurufm2c8mf6bsjezu1qgveouv7c7"
    const val KEY_2 = "w13r4cvf4hctaujv"

    fun unpad(d: ByteArray): ByteArray {
        val lastByte = d.last().toInt()
        val padIndex = (d.size - (lastByte % d.size))

        return d.slice(0 until padIndex).toByteArray()
    }

    fun pad(d: ByteArray): ByteArray {
        val size = 16 - (d.size % 16)
        val array = ByteArray(size)

        for (i in 0 until size) {
            array[i] = size.toByte()
        }

        return d + array
    }

    fun aesEncrypt(input: ByteArray, key: ByteArray): ByteArray {
        val paddedInput = pad(input)
        val iv = key.slice(0 until 16).toByteArray()

        return AES.encryptAesCbc(paddedInput, key, iv, Padding.NoPadding)
    }

    fun aesDecrypt(input: ByteArray, key: ByteArray): ByteArray {
        val iv = key.slice(0 until 16).toByteArray()

        return unpad(AES.decryptAesCbc(input, key, iv, Padding.NoPadding))
    }

    fun getFKey(input: ByteArray): ByteArray {
        var key = ""

        for (i in 0 until 16) {
            key += KEY_1[input[i].toInt() % KEY_1.length]
        }

        key += KEY_2

        return key.toByteArray()
    }

    fun getAuth(nonce: String): String {
        val keyData = nonce.map { (it.toInt() % 16).toByte() }.toByteArray()
        val fKey = getFKey(keyData)

        return Base64.encode(aesEncrypt(nonce.toByteArray(), fKey))
    }

    fun decryptNonce(input: String): String {
        val d = Base64.decode(input)
        return aesDecrypt(d, KEY_1.toByteArray())
            .decodeToString()
    }

    val BA = KEY_1.toByteArray()
    val ENC = Base64.encode(KEY_1.toByteArray())
}