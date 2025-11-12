package com.tevioapp.vendor.network.utils

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import java.io.IOException

class ProgressRequestBody(
    private val mDelegate: RequestBody,
    private val mListener: (bytesWritten: Long, contentLength: Long) -> Unit
) : RequestBody() {

    private var contentLength: Long = -1

    override fun contentType(): MediaType? = mDelegate.contentType()

    override fun contentLength(): Long {
        if (contentLength == -1L) {
            contentLength = try {
                mDelegate.contentLength()
            } catch (e: IOException) {
                0L
            }
        }
        return contentLength
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink).buffer()
        mDelegate.writeTo(countingSink)
        countingSink.flush()
    }

    inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten: Long = 0
        private var lastPercent: Int = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            val total = contentLength()
            val percent = if (total > 0) (bytesWritten * 100 / total).toInt() else 0
            if (percent != lastPercent) {
                lastPercent = percent
                mListener.invoke(bytesWritten, total)
            }
        }
    }
}
