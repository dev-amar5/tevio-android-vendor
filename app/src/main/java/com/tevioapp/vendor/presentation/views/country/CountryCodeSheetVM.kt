package com.tevioapp.vendor.presentation.views.country

import android.content.Context
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.room.AppDatabase
import com.tevioapp.vendor.utility.event.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class CountryCodeSheetVM @Inject constructor(
    @ApplicationContext private val context: Context, private val appDatabase: AppDatabase
) : BaseViewModel() {
    val obrCountryCode = SingleLiveEvent<List<CountryCode>>()

    fun getCountryList(text: String = "") {
        compositeDisposable.add(
            if (text.isNotBlank()) {
                appDatabase.getCountryCodeDao().getList(text)
            } else {
                appDatabase.getCountryCodeDao().getList()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    obrCountryCode.value = result
                })
    }

    fun getCountryCodeByIso(iso: String = BaseApp.instance.getCountryCode()) =
        SingleLiveEvent<CountryCode?>().apply {
            compositeDisposable.add(
                appDatabase.getCountryCodeDao().getCountryByIso(iso)
                    .delaySubscription(300, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
                        this.value = result
                    }, {
                        this.value = null
                    })
            )
        }
}

