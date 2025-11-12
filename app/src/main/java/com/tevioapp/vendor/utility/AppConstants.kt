package com.tevioapp.vendor.utility


object AppConstants {

    const val FORMAT_DATE_SMALL = "EEE, MMM d" //Thur, Apr 5
    const val FORMAT_DATE_SMALL_WITH_YEAR = "MMM d yyyy" //Apr 5 2025
    const val FORMAT_DATE_MEDIUM = "EEE, MMM d yyyy"//Thur, Apr 5 2025
    const val FORMAT_DATE_TIME_SHORT = "MMM dd yyyy - h:mm a"//Apr 5 2025 - 4:00AM
    const val FORMAT_TIME = "h:mm a"//4:00AM

    const val MIN_LOCATION_ACCURACY = 100f //meters
    const val MAP_ZOOM_LARGE: Float = 19f
    const val MAP_ZOOM_SMALL: Float = 13f
    const val MAP_MAX_ZOOM_OUT: Float = 10f
    const val NO_INTERNET = "Slow or No Internet Access"

    const val DEFAULT_PHONE_MIN_LENGTH = 5
    const val DEFAULT_PHONE_MAX_LENGTH = 15
    const val DEVICE_TYPE_ANDROID = "android"
    const val URL_PRIVACY_POLICY = "https://google.com"
}
