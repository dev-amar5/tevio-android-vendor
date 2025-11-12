package com.tevioapp.vendor.utility.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.extensions.getResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import org.json.JSONArray
import java.util.concurrent.TimeUnit

@HiltWorker
class DeleteMediaWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val url = workerParams.inputData.getStringArray("urls")
        if (url.isNullOrEmpty()) {
            return Single.just(Result.failure())
        }
        return authRepo.apiDeleteMedia(JSONArray().apply {
            url.forEach {
                put(it)
            }
        }).flatMap { response ->
            if (response.isSuccessful) {
                Single.just(Result.success())
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
    }

    companion object {
        fun createJob(context: Context, urls: Array<String>) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request = OneTimeWorkRequest.Builder(DeleteMediaWorker::class.java)
                .setConstraints(constraints).setBackoffCriteria(
                    BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS
                ).setInputData(
                    Data.Builder().putStringArray("urls", urls.map { it }.toTypedArray()).build()
                ).build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
