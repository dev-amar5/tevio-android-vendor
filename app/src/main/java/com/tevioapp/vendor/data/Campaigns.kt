package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.utility.currency.CurrencyUtils

data class Campaigns(
    @SerializedName("bonus_amount") var bonusAmount: Any?,
    @SerializedName("description") var description: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("is_new_rider_campaign") var isNewRiderCampaign: Boolean?,
    @SerializedName("name") var name: String?,
    @SerializedName("progress") var progress: Progress?,
    @SerializedName("referral_type") var referralType: Any?,
    @SerializedName("target") var target: String?,
    @SerializedName("time_info") var timeInfo: TimeInfo?,
    @SerializedName("type") var type: String?,
    @SerializedName("bonus_description") var bonusDescription: String?,
    @SerializedName("bonus_title") var bonusTitle: String?,

    ) {
    data class Progress(
        @SerializedName("current_value") var currentValue: Int?,
        @SerializedName("goal_value") var goalValue: Int?,
        @SerializedName("milestones") var milestones: List<Milestone>?
    ) {
        data class Milestone(
            @SerializedName("bonus_amount") var bonusAmount: Double?,
            @SerializedName("id") var id: String?,
            @SerializedName("target") var target: Int?,
            @SerializedName("unit") var unit: String?
        ) {
            fun getDisplayAmount(): String {
                return CurrencyUtils.formatAsCurrency(bonusAmount, "")
            }

            fun getDisplayLabel(): String {
                return buildString { append(target, "\n", unit) }
            }
        }


        fun getProgressPercent(): Float {
            val goal = goalValue ?: 0
            val current = currentValue ?: 0
            return if (goal > 0) {
                (current.toFloat() / goal.toFloat()) * 100
            } else 0F
        }
    }

    data class TimeInfo(
        @SerializedName("days_left") var daysLeft: Int?,
        @SerializedName("end_date") var endDate: String?,
        @SerializedName("start_date") var startDate: String?
    )
}