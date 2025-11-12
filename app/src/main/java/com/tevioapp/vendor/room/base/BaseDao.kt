package com.tevioapp.vendor.room.base

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import io.reactivex.Completable

@Dao
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: T): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entityList: List<T>): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: T): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(entityList: List<T>): Completable

    @Delete
    fun delete(entity: T): Completable

    @Delete
    fun deleteAll(entityList: List<T>): Completable

}
