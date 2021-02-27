import tk.zwander.common.tools.Auth
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Test {
    fun unpad(d: ByteArray): ByteArray {
        val lastByte = d.last().toInt()
        val padIndex = (d.size - (lastByte % d.size))

        return d.slice(0 until padIndex).toByteArray()
    }


    @JvmStatic
    fun main(args: Array<String>) {
        val start = arrayOf(
            71.toByte(), 108, 0, -57, -118, -38, -111, -22, 109, -103, -38, 20, 0, 106, -14, 53, -48, 104, 31, 68, 109, 112, 40, -55, 8, -75, 109, 108, 11, -128, 79, 119
        ).toByteArray()
        val key = arrayOf(
            104.toByte(), 113, 122, 100, 117, 114, 117, 102, 109, 50, 99, 56, 109, 102, 54, 98, 115, 106, 101, 122, 117, 49, 113, 103, 118, 101, 111, 117, 118, 55, 99, 55
        ).toByteArray()

        val out = Auth.aesDecrypt(start, key)
        val testOut = aesDecrypt(start, key)

        println("out ${out.contentToString()}")
        println("testOut ${testOut.contentToString()}")
        println("eq ${out.contentEquals(testOut)}")

        val k = Auth.KEY_1.toByteArray()
        val kt = Auth.BA

        println("k ${k.contentToString()}")
        println("kt ${kt.contentToString()}")
        println("eq ${k.contentEquals(kt)}")
    }

    fun aesDecrypt(input: ByteArray, key: ByteArray): ByteArray {
        val iv = key.slice(0 until 16).toByteArray()
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))

        val result = cipher.doFinal(input)

        return unpad(result)
    }
}