package com.tevioapp.vendor.utility.extensions

import androidx.work.ListenableWorker.Result
import com.tevioapp.vendor.data.common.FileUploadResource
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.PagingResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.event.SingleLiveEvent
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.event.helper.Resource
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.rx.EventBus
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

fun <T> Flowable<FileUploadResource<T>>.mediaSubscription(liveData: SingleLiveEvent<FileUploadResource<T>>): Disposable {
    return subscribeOn(Schedulers.computation()).subscribe({
        Logger.d(it.toString(), tag = "ProgressRequestBody")
        liveData.postValue(it)
    }, { error ->
        liveData.postValue(
            FileUploadResource(
                status = FileUploadResource.Status.ERROR, message = error.parseException()
            )
        )
    })
}

fun <M> Single<ApiResponse<M>>.apiSubscription(
    liveData: SingleRequestEvent<M>, observable: Observable<Long>? = null
): Disposable {
    val source: Observable<ApiResponse<M>> = if (observable != null) {
        observable.flatMapSingle {
            this.doOnSubscribe {
                liveData.postValue(Resource.loading())
            }
        }
    } else {
        this.doOnSubscribe {
            liveData.postValue(Resource.loading())
        }.toObservable()
    }

    return source.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe({ response ->
            if (response.isSuccessful) {
                liveData.postValue(Resource.success(response.data, response.message.toString()))
            } else {
                liveData.postValue(Resource.warn(null, response.message.toString()))
            }
        }, { error ->
            liveData.postValue(
                Resource.error(
                    null, error.parseException(), extractErrorCode(error)
                )
            )
        })
}


fun <M> Observable<ApiResponse<M>>.apiSubscription(
    liveData: SingleRequestEvent<M>
): Disposable {
    return this.doOnSubscribe {
        liveData.postValue(Resource.loading())
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
        when {
            response.isSuccessful -> {
                liveData.postValue(Resource.success(response.data, response.message.orEmpty()))
            }

            else -> {
                liveData.postValue(Resource.warn(null, response.message.orEmpty()))
            }
        }
    }, { error ->
        liveData.postValue(
            Resource.error(
                null, error.parseException(), extractErrorCode(error)
            )
        )
    })
}

fun Observable<SimpleApiResponse>.simpleSubscription(
    liveData: SingleRequestEvent<Unit>
): Disposable {
    return this.doOnSubscribe {
        liveData.postValue(Resource.loading())
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
        when {
            response.isSuccessful -> {
                liveData.postValue(Resource.success(null, response.message.orEmpty()))
            }

            else -> {
                liveData.postValue(Resource.warn(null, response.message.orEmpty()))
            }
        }
    }, { error ->
        liveData.postValue(
            Resource.error(
                null, error.parseException(), extractErrorCode(error)
            )
        )
    })
}

fun <M> Single<PagingResponse<M>>.pagingSubscription(liveData: SingleRequestEvent<M>): Disposable {
    return this.doOnSubscribe {
        liveData.postValue(Resource.loading())
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
        if (it.isSuccessful) liveData.postValue(
            Resource.success(
                it.data, it.message.toString(), it.metaData
            )
        )
        else liveData.postValue(Resource.warn(null, it.message.toString()))
    }, { error ->
        liveData.postValue(Resource.error(null, error.parseException(), extractErrorCode(error)))
    })
}


fun Single<SimpleApiResponse>.simpleSubscription(liveData: SingleRequestEvent<Unit>): Disposable {
    return this.doOnSubscribe {
        liveData.postValue(Resource.loading())
    }.subscribeOn(Schedulers.io()).subscribe({
        liveData.postValue(
            Resource.success(
                null, it.message.toString()
            )
        )
    }, { error ->
        liveData.postValue(Resource.error(null, error.parseException(), extractErrorCode(error)))
    })
}


/**
 * get error text
 * @return error text
 */
fun Throwable?.parseException(): String {
    fun publishReLoginMessage() {
        EventBus.post(
            UnAuthorize(
                "Session Timeout", "The session have been expired ,Please login again"
            )
        )
    }

    val throwable = this ?: return ""
    return when (throwable) {
        is HttpException -> {
            val exception: HttpException = throwable
            when (exception.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    val error = getErrorText(exception)
                    publishReLoginMessage()
                    error
                }

                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    getErrorText(exception)
                }

                else -> {
                    getErrorText(throwable)
                }
            }
        }

        is IOException -> {
            return AppConstants.NO_INTERNET
        }

        is RuntimeException -> {
            return if (throwable.message.toString().contains("host", true)) AppConstants.NO_INTERNET
            else throwable.message.toString()
        }

        else -> {
            return throwable.message.toString()
        }
    }
}

/**
 * get error text
 * @param it
 * @return error text
 */
private fun getErrorText(it: HttpException): String {
    var message = "Unknown Error"
    try {
        val errorBody: ResponseBody? = it.response()?.errorBody()
        errorBody?.string()?.apply {
            val obj = JSONObject(this)
            if (obj.has("message")) {
                message = obj.getString("message")
            }
            if (obj.has("error")) {
                message = obj.getString("error")
            }
        }
    } catch (e: Exception) {
        message = it.message().toString()
    }
    return message
}

fun getResult(throwable: Throwable): Result {
    val exception = throwable as? HttpException
    return when (exception?.code()) {
        HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN -> Result.failure() // client-side issues, won't succeed on retry

        HttpURLConnection.HTTP_INTERNAL_ERROR, HttpURLConnection.HTTP_UNAVAILABLE, null -> Result.retry() // server errors or non-HTTP errors (network, timeout, etc.)

        else -> Result.retry()
    }
}

data class UnAuthorize(val title: String, val message: String)

/**
 * get error text
 * @param error
 * @return error code
 */
private fun extractErrorCode(error: Throwable): Int {
    return when (error) {
        is HttpException -> error.code()
        else -> -1
    }
}