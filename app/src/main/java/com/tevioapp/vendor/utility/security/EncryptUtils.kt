package com.tevioapp.vendor.utility.security

import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object EncryptUtils {


    fun encrypt(token: String): String? {
        val RESULT_LENGTH = 16
        val cript: MessageDigest
        cript = try {
            MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        cript.reset()
        try {
            cript.update(token.toByteArray(charset("utf8")))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
        return BigInteger(1, cript.digest()).toString(RESULT_LENGTH)
    }

}