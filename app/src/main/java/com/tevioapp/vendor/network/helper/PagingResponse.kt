package com.tevioapp.vendor.network.helper

import com.google.gson.annotations.SerializedName

class PagingResponse<T> : ApiResponse<T>() {
    @SerializedName("metaData")
    var metaData: MetaData? = null
}

data class MetaData(
    @SerializedName("limit") val pageSize: Int,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") var totalPages: Int,
    @SerializedName("total_count") var totalCount: Int,
){
    val isLastPage: Boolean
        get() = currentPage == totalPages
}
