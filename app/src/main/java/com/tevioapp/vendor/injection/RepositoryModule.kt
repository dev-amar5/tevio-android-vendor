package com.tevioapp.vendor.injection

import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.repositary.auth.AuthRepoImpl
import com.tevioapp.vendor.repositary.notification.NotificationRepo
import com.tevioapp.vendor.repositary.notification.NotificationRepoImpl
import com.tevioapp.vendor.repositary.order.OrderRepo
import com.tevioapp.vendor.repositary.order.OrderRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun providesAuthRepository(authRepoImpl: AuthRepoImpl): AuthRepo = authRepoImpl

    @Singleton
    @Provides
    fun providesOrderRepository(orderRepoImpl: OrderRepoImpl): OrderRepo = orderRepoImpl

    @Singleton
    @Provides
    fun providesNotificationRepository(notificationRepoImpl: NotificationRepoImpl): NotificationRepo =
        notificationRepoImpl

}