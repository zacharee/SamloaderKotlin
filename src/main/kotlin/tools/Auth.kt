package tools

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.provider.JCEBlockCipher
import java.security.Security
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Auth {
    const val KEY_1 = "hqzdurufm2c8mf6bsjezu1qgveouv7c7"
    const val KEY_2 = "w13r4cvf4hctaujv"

    init {
       Security.addProvider(BouncyCastleProvider())
    }

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

        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))

        val result = cipher.doFinal(paddedInput)

        return result
    }

    fun aesDecrypt(input: ByteArray, key: ByteArray): ByteArray {
        val iv = key.slice(0 until 16).toByteArray()
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))

        val result = cipher.doFinal(input)

        return unpad(result)
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

        return Base64.getEncoder().encodeToString(aesEncrypt(nonce.toByteArray(), fKey))
    }

    fun decryptNonce(input: String): String {
        val d = Base64.getDecoder().decode(input)
        return aesDecrypt(d, KEY_1.toByteArray())
            .decodeToString()
    }
}