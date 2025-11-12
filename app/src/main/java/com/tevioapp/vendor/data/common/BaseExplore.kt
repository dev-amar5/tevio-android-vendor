package com.tevioapp.vendor.data.common

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BaseExplore(
    @SerializedName("data")
    var data: JsonElement?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("type")
    var type: String = "",
    @SerializedName("title")
    var title: String = "",
){
    fun getDataString():String?{
        return data?.toString()
    }

}


