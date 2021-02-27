package tk.zwander.common.util

/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

import com.soywiz.korio.lang.format
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.krypto.MD5
import kotlinx.io.core.Input

object MD5 {
    private const val TAG = "MD5"
    suspend fun checkMD5(md5: String, updateFile: AsyncInputStream?): Boolean {
        if (md5.isBlank() || updateFile == null) {
            println("MD5 string empty or updateFile null")
            return false
        }
        val calculatedDigest = calculateMD5(updateFile)
        if (calculatedDigest == null) {
            println("calculatedDigest null")
            return false
        }
        println("Calculated digest: $calculatedDigest")
        println("Provided digest: $md5")
        return calculatedDigest.equals(md5, ignoreCase = true)
            .also { updateFile.close() }
    }

    suspend fun calculateMD5(updateFile: AsyncInputStream): String? {
        val md5 = MD5.create()
        val buffer = ByteArray(8192)
        var read: Int
        return try {
            while (updateFile.read(buffer, 0, buffer.size).also { read = it } > 0) {
                md5.update(buffer, 0, read)
            }
            val output = md5.digest().hex.format("%32s").replace(' ', '0')
            output
        } catch (e: Exception) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                updateFile.close()
            } catch (e: Exception) {
                println("Exception on closing MD5 input stream")
                e.printStackTrace()
            }
        }
    }
}