package com.tevioapp.vendor.utility.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OverwritingInputMerger
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.setInputMerger
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.extensions.getResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import org.json.JSONObject

@HiltWorker
class UpdateProfileImageWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val imagePath = workerParams.inputData.getString("remotePath")
        return authRepo.apiUpdateProfilePic(JSONObject().apply {
            put("profile_pic", imagePath)
        }).map { response ->
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
        fun createJob(context: Context, localPath: String) {
            val uploadRequest = UploadMediaWorker.getWorkerRequest(localPath)
            val updateRequest =
                OneTimeWorkRequestBuilder<UpdateProfileImageWorker>().setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).setInputMerger(OverwritingInputMerger::class).build()
            WorkManager.getInstance(context).beginWith(uploadRequest).then(updateRequest).enqueue()
        }
    }
}
