package com.tevioapp.vendor.utility.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tevioapp.vendor.data.common.FileUploadResource
import com.tevioapp.vendor.data.common.MediaFile
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.extensions.getResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single

@HiltWorker
class UploadMediaWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val imagePath = workerParams.inputData.getString("localPath")
        if (imagePath.isNullOrEmpty()) return Single.just(Result.failure())
        val mediaFile = MediaFile(mediaType = MediaFile.TYPE.IMAGE, localUrl = imagePath)
        return authRepo.apiUploadMedia(mediaFile)
            .filter { it.status != FileUploadResource.Status.UPLOADING } // Ignore progress
            .firstOrError() // take first emission
            .map { response ->
                if (response.status == FileUploadResource.Status.SUCCESS) {
                    Result.success(workDataOf("remotePath" to response.result))
                } else {
                    Result.retry()
                }
            }.onErrorReturn { error ->
                getResult(error)
            }


    }


    companion object {
        fun getWorkerRequest(localPath: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<UploadMediaWorker>().setInputData(
                workDataOf(
                    "localPath" to localPath
                )
            ).setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()
        }
    }
}
