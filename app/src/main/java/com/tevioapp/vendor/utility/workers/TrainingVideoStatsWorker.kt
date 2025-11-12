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
import com.google.gson.Gson
import com.tevioapp.vendor.data.VideoStats
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.extensions.fromJsonToList
import com.tevioapp.vendor.utility.extensions.getResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import java.util.concurrent.TimeUnit

@HiltWorker
class TrainingVideoStatsWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val list= inputData.getString("input").fromJsonToList<VideoStats>()
        return authRepo.apiUpdateTrainingVideoStats(list)
            .map { response ->
                if (response.isSuccessful) {
                    Result.success()
                } else {
                    Result.retry()
                }
            }
            .onErrorReturn { error ->
                getResult(error)
            }
    }


    companion object {
        fun createJob(context: Context, list: List<VideoStats>) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequest.Builder(TrainingVideoStatsWorker::class.java)
                .setConstraints(constraints)
                .setInputData(Data.Builder().putString("input", Gson().toJson(list)).build())
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
