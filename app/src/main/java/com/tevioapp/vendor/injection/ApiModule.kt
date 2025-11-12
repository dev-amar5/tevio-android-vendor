package com.tevioapp.vendor.injection

import com.tevioapp.vendor.injection.helper.CommonQualifiers.AuthUrl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.GoogleUrl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.OrdersUrl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.NotificationUrl
import com.tevioapp.vendor.network.api.AuthApi
import com.tevioapp.vendor.network.api.GoogleApi
import com.tevioapp.vendor.network.api.NotificationApi
import com.tevioapp.vendor.network.api.OrderApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @Singleton
    fun providesAuthService(@AuthUrl retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun providesOrderService(@OrdersUrl retrofit: Retrofit): OrderApi =
        retrofit.create(OrderApi::class.java)

    @Provides
    @Singleton
    fun providesGoogleService(@GoogleUrl retrofit: Retrofit): GoogleApi =
        retrofit.create(GoogleApi::class.java)

    @Provides
    @Singleton
    fun providesNotificationService(@NotificationUrl retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

}