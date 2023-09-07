package com.kiliccambaz.expenseapp.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object HashUtils {

    fun hashPassword(password: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
            val builder = StringBuilder()

            for (byte in bytes) {
                builder.append(String.format("%02x", byte))
            }

            return builder.toString()
        } catch (e: NoSuchAlgorithmException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            throw RuntimeException("Hashing algorithm not available", e)
        }
    }

}