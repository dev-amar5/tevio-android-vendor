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
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.extensions.getResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import java.util.concurrent.TimeUnit

@HiltWorker
class GetDocumentsWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    private val sharedPref: SharedPref,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        return authRepo.apiGetDocumentsInfo().map { response ->
            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        }.onErrorReturn { error ->
            getResult(error)
        }
    }


    companion object {
        fun createJob(context: Context) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val request = OneTimeWorkRequest.Builder(GetDocumentsWorker::class.java)
                .setConstraints(constraints).setBackoffCriteria(
                    BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS
                ).build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                    "get_documents", ExistingWorkPolicy.REPLACE, request
                )
        }
    }
}
