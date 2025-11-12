package com.tevioapp.vendor.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.tevioapp.vendor.presentation.views.country.CountryCode
import com.tevioapp.vendor.room.base.BaseDao
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface CountryCodeDao : BaseDao<CountryCode> {
    @Query("SELECT * FROM tb_country WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    fun getList(query: String): Single<List<CountryCode>>

    @Query("SELECT * FROM tb_country")
    fun getList(): Single<List<CountryCode>>

    @Query("SELECT * FROM tb_country WHERE code = :iso")
    fun getCountryByIso(iso: String): Maybe<CountryCode>

    @Query("SELECT COUNT(*) FROM tb_country")
    fun getCount(): Single<Int>
}
