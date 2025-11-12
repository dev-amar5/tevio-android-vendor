package com.tevioapp.vendor.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tevioapp.vendor.presentation.views.country.CountryCode
import com.tevioapp.vendor.room.dao.CountryCodeDao


@Database(
    entities = [CountryCode::class], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCountryCodeDao(): CountryCodeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(appContext: Context) =
            Room.databaseBuilder(appContext, AppDatabase::class.java, "tevio-delivery.db")
                .fallbackToDestructiveMigration().build()


    }

}