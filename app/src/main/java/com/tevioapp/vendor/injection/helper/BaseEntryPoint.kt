package com.tevioapp.vendor.injection.helper

import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.utility.util.DataProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BaseEntryPoint {
    fun getSharePref(): SharedPref
    fun getDataProvider(): DataProvider
}