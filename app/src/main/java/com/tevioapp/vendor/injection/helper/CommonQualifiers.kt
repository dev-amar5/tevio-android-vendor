package com.tevioapp.vendor.injection.helper

import javax.inject.Qualifier

class CommonQualifiers {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AuthUrl

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RestaurantsUrl

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class OrdersUrl

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GoogleUrl

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class NotificationUrl
}