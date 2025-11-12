package com.tevioapp.vendor.utility.log

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.tevioapp.vendor.utility.util.AppSettings

object Logger {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var isLoggingEnabled = AppSettings.isLoggingEnabled()

    private fun getTag(): String {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            val className = element.className
            if (className != Logger::class.java.name && !className.startsWith("java.lang.Thread") && !className.startsWith(
                    "dalvik.system.VMStack"
                )
            ) {
                return className.substringAfterLast('.') // only class name
            }
        }
        return "Unknown"
    }

    fun d(message: String?, tag: String = getTag()) {
        if (isLoggingEnabled) Log.d(tag, getPrettyString(message))
    }

    fun i(message: String?, tag: String = getTag()) {
        if (isLoggingEnabled) Log.i(tag, getPrettyString(message))
    }

    fun w(message: String?, tag: String = getTag()) {
        if (isLoggingEnabled) Log.w(tag, getPrettyString(message))
    }

    fun e(message: String?, throwable: Throwable? = null, tag: String = getTag()) {
        if (isLoggingEnabled) {
            Log.e(tag, message, throwable)
        }
    }


    private fun getPrettyString(message: String?): String {
        return if (message.isNullOrEmpty()) ""
        else if (isJson(message).not()) message
        else try {
            gson.toJson(JsonParser.parseString(message))
        } catch (e: Exception) {
            message
        }
    }

    private fun isJson(text: String): Boolean {
        val trimmed = text.trim()
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith(
            "]"
        ))
    }
}
