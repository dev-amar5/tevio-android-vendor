package com.tevioapp.vendor.presentation.common.base

import android.view.View
import androidx.lifecycle.ViewModel
import com.tevioapp.vendor.utility.event.SingleLiveEvent
import com.tevioapp.vendor.utility.extensions.UnAuthorize
import com.tevioapp.vendor.utility.extensions.preventDoubleClick
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.rx.EventBus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


open class BaseViewModel : ViewModel() {
    var compositeDisposable = CompositeDisposable()
    val onClick by lazy { SingleLiveEvent<View>() }
    val obrMessage by lazy { SingleLiveEvent<String?>() }
    val obrUnAuthorize by lazy { SingleLiveEvent<UnAuthorize>() }

    init {
        compositeDisposable.add(
            EventBus.subscribe<UnAuthorize>().subscribeOn(Schedulers.io()).subscribe {
                obrUnAuthorize.postValue(it)
            })
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }

    fun Disposable.addToCompositeDisposable() {
        compositeDisposable.add(this)
    }

    open fun onClick(view: View) {
        view.preventDoubleClick()
        onClick.value = view
    }

    open fun showMessage(message: String?) {
        Logger.e(message)
        obrMessage.postValue(message)
    }

    fun obrMinutesChange(): SingleLiveEvent<Long> {
        fun getMinutes() = ((System.currentTimeMillis() / 1000) % 3600) / 60
        return SingleLiveEvent<Long>().apply {
            var currentTime = getMinutes()
            compositeDisposable.add(
                Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).filter {
                    this.hasActiveObservers()
                }.subscribe { _ ->
                    val s = getMinutes()
                    if (currentTime != s) {
                        this.value = currentTime
                        currentTime = s
                    }
                })
        }
    }

}