package com.tevioapp.vendor.data.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class TextIconState(
    val id: String? = null,
    val text: String? = null,
    val icon: Int? = null,
    val isSelected: Boolean = false,
    val type: String? = null
) : Parcelable


