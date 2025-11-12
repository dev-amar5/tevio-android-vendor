package com.tevioapp.vendor.data.common

data class FileUploadResource<T>(
    val status: Status,
    val bytesWritten: Long = 0,
    val contentLength: Long = 0,
    val result: T? = null,
    val message: String? = null
) {
    override fun toString(): String {
        return "FileUploadResource(status=$status, bytesWritten=$bytesWritten, contentLength=$contentLength, result=$result, message=$message)"
    }

    /**
     * Returns upload progress as an integer percentage.
     * 0 if content length is zero to avoid division by zero.
     */
    fun getProgress(): Int {
        if (contentLength <= 0L) return 0
        val progress = (bytesWritten.toDouble() / contentLength.toDouble()) * 100
        return progress.coerceIn(0.0, 100.0).toInt()
    }

    /**
     * Returns upload progress as a string with percentage symbol.
     */
    fun getProgressDisplay(): String = "${getProgress()}%"

    enum class Status {
        UPLOADING, SUCCESS, ERROR
    }
}
