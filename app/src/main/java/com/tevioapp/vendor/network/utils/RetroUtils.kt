package com.tevioapp.vendor.network.utils

import com.tevioapp.vendor.data.common.FileUploadResource
import io.reactivex.FlowableEmitter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RetroUtils {
    fun <T> getMultipartBody(
        filePath: String?, keyName: String, emitter: FlowableEmitter<FileUploadResource<T>>
    ): MultipartBody.Part? {
        if (filePath == null) return null
        val file = File(filePath)
        return MultipartBody.Part.createFormData(
            keyName, file.name, createCountingRequestBody(file, emitter)
        )
    }

    private fun <T> createCountingRequestBody(
        file: File, emitter: FlowableEmitter<FileUploadResource<T>>
    ): RequestBody {
        val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        return ProgressRequestBody(
            requestBody
        ) { bytesWritten, contentLength ->
            emitter.onNext(
                FileUploadResource(
                    status = FileUploadResource.Status.UPLOADING,
                    bytesWritten = bytesWritten,
                    contentLength = contentLength,
                )
            )
        }
    }

}
