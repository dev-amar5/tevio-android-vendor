package com.tevioapp.vendor.injection

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.tevioapp.vendor.BuildConfig
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.data.local.SharedPrefImpl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.AuthUrl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.GoogleUrl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.OrdersUrl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.RestaurantsUrl
import com.tevioapp.vendor.injection.helper.CommonQualifiers.NotificationUrl
import com.tevioapp.vendor.injection.helper.HeaderInterceptor
import com.tevioapp.vendor.room.AppDatabase
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.socket.SocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    private val timeout = 60L

    @Provides
    @AuthUrl
    fun providesAuthUrl(): String = BuildConfig.URL_AUTH

    @Provides
    @RestaurantsUrl
    fun providesRestroUrl(): String = BuildConfig.URL_RESTAURANT

    @Provides
    @OrdersUrl
    fun providesOrdersUrl(): String = BuildConfig.URL_ORDERS

    @Provides
    @NotificationUrl
    fun providesNotificationUrl(): String = BuildConfig.URL_NOTIFICATION


    @Provides
    @Singleton
    fun providesOkHttp(
        headerInterceptor: HeaderInterceptor, loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(headerInterceptor)
        builder.addInterceptor(loggingInterceptor)
        builder.readTimeout(timeout, TimeUnit.SECONDS).connectTimeout(timeout, TimeUnit.SECONDS)
            .callTimeout(timeout, TimeUnit.SECONDS)
        return builder.build()
    }

    @Provides
    @Singleton
    @AuthUrl
    fun providesAuthRetrofit(@AuthUrl url: String, okHttpClient: OkHttpClient): Retrofit {
        return createRetrofit(url, okHttpClient)
    }

    @Provides
    @Singleton
    @RestaurantsUrl
    fun providesRestroRetrofit(@RestaurantsUrl url: String, okHttpClient: OkHttpClient): Retrofit {
        return createRetrofit(url, okHttpClient)
    }

    @Provides
    @Singleton
    @OrdersUrl
    fun providesOrdersRetrofit(@OrdersUrl url: String, okHttpClient: OkHttpClient): Retrofit {
        return createRetrofit(url, okHttpClient)
    }

    @Provides
    @Singleton
    @NotificationUrl
    fun providesNotificationRetrofit(@NotificationUrl url: String, okHttpClient: OkHttpClient): Retrofit {
        return createRetrofit(url, okHttpClient)
    }

    @Provides
    @Singleton
    @GoogleUrl
    fun providesGoogleRetrofit(loggingInterceptor: HttpLoggingInterceptor): Retrofit {
        val builder = OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor)
            readTimeout(timeout, TimeUnit.SECONDS).connectTimeout(timeout, TimeUnit.SECONDS)
                .callTimeout(timeout, TimeUnit.SECONDS)
        }
        return Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/")
            .client(builder.build()).addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    private fun createRetrofit(url: String, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(url).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Logger.d(
                message, "HttpLogging"
            )
        }.apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(application.packageName, MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSharedPref(sharedPrefImpl: SharedPrefImpl): SharedPref = sharedPrefImpl

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) =
        AppDatabase.getDatabase(appContext)

    @Provides
    @Singleton
    fun provideSocketClient(@NotificationUrl url: String, sharedPref: SharedPref): SocketClient {
        return SocketClient(
            baseUrl = url, sharedPref = sharedPref, enableLogging = BuildConfig.DEBUG
        )
    }

    private fun getCertificatePinner(application: Application): String? {
        return try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val inputStream = application.assets.open("your_server_certificate.crt")
            val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
            CertificatePinner.pin(certificate)
        } catch (e: Exception) {
            null
        }
    }

}
