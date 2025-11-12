package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrainingVideo(
    @SerializedName("description") var description: String?,
    @SerializedName("display_order") var displayOrder: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("is_enabled") var isEnabled: Boolean?,
    @SerializedName("name") var name: String?,
    @SerializedName("thumbnail_url") var thumbnailUrl: String?,
    @SerializedName("total_time") var totalTime: String?,
    @SerializedName("video_stats") var videoStats: VideoStats?,
    @SerializedName("video_url") var videoUrl: String?
) : Parcelable


@Parcelize
data class VideoStats(
    @SerializedName("video_id") var videoId: String?,
    @SerializedName("is_completed") var isCompleted: Boolean,
    @SerializedName("watch_stats") var watchStats: List<Int>?,
    @SerializedName("watch_percentage") var watchPercentage: Double?
) : Parcelable


