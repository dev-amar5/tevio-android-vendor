package com.tevioapp.vendor.data.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileInfo(
    val title: String, val fileName: String, val url: String
) : Parcelable
