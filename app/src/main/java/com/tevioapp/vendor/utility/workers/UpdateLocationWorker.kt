package com.tevioapp.vendor.utility.workers

import android.content.Context
import android.location.Location
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
import com.tevioapp.vendor.utility.util.DateTimeUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateLocationWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val payloadString =
            workerParams.inputData.getString("payload") ?: return Single.just(Result.failure())
        return authRepo.apiUpdateLocation(JSONObject(payloadString)).flatMap { response ->
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
        fun createJob(context: Context, location: Location) {
            val json = JSONObject().apply {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                if (location.hasSpeed()) {
                    put("speed", location.speed.toString())
                }
                if (location.hasBearing()) {
                    put("bearing", location.bearing.toString())
                }
                put("timestamp", DateTimeUtils.timestampToUtcIso(location.time))
            }


            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request = OneTimeWorkRequest.Builder(UpdateLocationWorker::class.java)
                .setConstraints(constraints).setBackoffCriteria(
                    BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS
                ).setInputData(
                    Data.Builder().putString("payload", json.toString()).build()
                ).build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

}
