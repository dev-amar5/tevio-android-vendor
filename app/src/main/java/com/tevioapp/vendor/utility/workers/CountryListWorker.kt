package com.tevioapp.vendor.utility.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.room.AppDatabase
import com.tevioapp.vendor.utility.extensions.getResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@HiltWorker
class CountryListWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    private val appDatabase: AppDatabase,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val pair = BaseApp.instance.refreshCountryCode()
        return appDatabase.getCountryCodeDao().getCount().subscribeOn(Schedulers.io())
            .flatMap { count ->
                if (count == 0 || (pair.second != null && pair.first != pair.second)) {
                    // Fetch API and update DB
                    authRepo.apiCountryCodeList().flatMap { response ->
                        if (response.isSuccessful) {
                            appDatabase.getCountryCodeDao().insertAll(response.data.orEmpty())
                                .subscribeOn(Schedulers.io()).toSingleDefault(
                                    Result.success(
                                        workDataOf("updated" to true)
                                    )
                                )
                        } else {
                            Single.just(Result.retry())
                        }
                    }.onErrorReturn { error ->
                        try {
                            getResult(error)
                        } catch (e: Exception) {
                            Result.retry()
                        }
                    }
                } else {
                    Single.just(
                        Result.success(
                            workDataOf("updated" to false)
                        )
                    )
                }
            }
    }


    companion object {
        const val TAG = "country_code_list"
        fun createJob(context: Context) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request = OneTimeWorkRequest.Builder(CountryListWorker::class.java)
                .setConstraints(constraints).setBackoffCriteria(
                    BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS
                ).build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                TAG, ExistingWorkPolicy.REPLACE, request
            )
        }
    }
}
