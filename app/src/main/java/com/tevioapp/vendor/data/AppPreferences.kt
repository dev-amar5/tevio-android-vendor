package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class AppPreferences(
    @SerializedName("data_sharing")
    var dataSharing: Boolean= false,
    @SerializedName("language")
    var language: String? = "en",
    @SerializedName("notification")
    var notification: Boolean= true,
    @SerializedName("order_updates")
    var orderUpdates: Boolean= true,
    @SerializedName("payment_method")
    var paymentMethod: Boolean= false,
    @SerializedName("promotions")
    var promotions: Boolean= true,
    @SerializedName("rewards_and_reminders")
    var rewardsAndReminders: Boolean = true,
    @SerializedName("theme")
    var theme: String?="light",
    @SerializedName("updates")
    var updates: Boolean = true
)