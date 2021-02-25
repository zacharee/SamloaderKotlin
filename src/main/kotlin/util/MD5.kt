package util

/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

import java.io.*
import java.lang.RuntimeException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object MD5 {
    private const val TAG = "MD5"
    fun checkMD5(md5: String, updateFile: File?): Boolean {
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
    }

    fun calculateMD5(updateFile: File): String? {
        val digest: MessageDigest = try {
            MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            println("Exception while getting digest")
            e.printStackTrace()
            return null
        }
        val `is`: InputStream = try {
            FileInputStream(updateFile)
        } catch (e: FileNotFoundException) {
            println("Exception while getting FileInputStream")
            e.printStackTrace()
            return null
        }
        val buffer = ByteArray(8192)
        var read: Int
        return try {
            while (`is`.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
            val md5sum = digest.digest()
            val bigInt = BigInteger(1, md5sum)
            var output = bigInt.toString(16)
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0')
            output
        } catch (e: IOException) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                println("Exception on closing MD5 input stream")
                e.printStackTrace()
            }
        }
    }
}