package com.tevioapp.vendor.data.common


data class MediaFile(
    var mediaType: TYPE,
    var localUrl: String? = null,
    var remoteUrl: String? = null,
    var mediaName: String = "",
    var resource: FileUploadResource<*>? = null
) {
    fun getMediaUrl(): String {
        if (localUrl.isNullOrEmpty().not()) {
            return localUrl.orEmpty()
        }
        return remoteUrl.orEmpty()
    }

    enum class TYPE {
        IMAGE, VIDEO, DOC, AUDIO
    }

    fun needRetry(): Boolean {
        return localUrl != null && resource?.status == FileUploadResource.Status.ERROR
    }

    fun isUploading(): Boolean {
        return localUrl != null && resource?.status == FileUploadResource.Status.UPLOADING
    }

    fun isRemoteMedia(): Boolean {
        return remoteUrl.orEmpty().isNotEmpty()
    }

    fun hasAnyMedia(): Boolean {
        return (localUrl.isNullOrEmpty() && remoteUrl.isNullOrEmpty()).not()
    }

    fun setRemoteUrlPath(path: String?) {
        remoteUrl = path
        remoteUrl?.let {
            mediaType = when {
                it.contains(".pdf", true) || it.contains(".doc", true) || it.contains(
                    ".docx", true
                ) || it.contains(".txt", true) -> TYPE.DOC

                it.contains(".jpg", true) || it.contains(".jpeg", true) || it.contains(
                    ".png", true
                ) -> TYPE.IMAGE

                it.contains(".mp4", true) || it.contains(".mkv", true) -> TYPE.VIDEO
                else -> mediaType // Retain existing type if extension doesn't match
            }
            mediaName = it.substringAfterLast("/")
        }
    }
}