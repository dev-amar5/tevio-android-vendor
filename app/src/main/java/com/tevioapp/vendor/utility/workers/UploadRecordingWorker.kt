package com.tevioapp.vendor.utility.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OverwritingInputMerger
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.setInputMerger
import androidx.work.workDataOf
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.extensions.getResult
import com.tevioapp.vendor.utility.notification.NotificationData
import com.tevioapp.vendor.utility.notification.NotificationProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Single
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

@HiltWorker
class UploadRecordingWorker @AssistedInject constructor(
    private val authRepo: AuthRepo,
    private val notificationProvider: NotificationProvider,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {
    private var notificationId: Int = 0
    override fun createWork(): Single<Result> {
        val notificationData = NotificationData(
            title = "Uploading...",
            message = "Uploading your recording",
            type = NotificationData.TYPE_UPLOAD_PROGRESS
        )
        notificationId = notificationProvider.showDefaultNotification(notificationData)
        showFakeProgress()
        val mediaPath = workerParams.inputData.getString("remotePath")
        val orderId = workerParams.inputData.getString("orderId").orEmpty()

        return authRepo.apiSaveRecording(JSONObject().apply {
            put("url", mediaPath)
            if (orderId.isNotEmpty()) {
                put("order_id", orderId)
            }
        }).map { response ->
            if (response.isSuccessful) {
                notificationData.title = "Upload Complete"
                notificationData.message = "Recording uploaded successfully"
                notificationProvider.removeNotification(notificationId)
                notificationProvider.showDefaultNotification(notificationData)
                Result.success()
            } else {
                notificationProvider.removeNotification(notificationId)
                Result.retry()
            }
        }.onErrorReturn { error ->
            notificationProvider.removeNotification(notificationId)
            getResult(error)
        }
    }

    private fun showFakeProgress() = runBlocking {
        val nm = context.getSystemService(android.app.NotificationManager::class.java)
        repeat(5) {
            delay(300)
            val n = NotificationCompat.Builder(context, "default")
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle("Uploading Recording").setContentText("Progress ${(it + 1) * 20}%")
                .setProgress(100, (it + 1) * 20, false).setOngoing(true).build()
            nm.notify(notificationId, n)
        }
    }

    companion object {
        fun createJob(context: Context, localPath: String, orderId: String) {
            val uploadRequest = UploadMediaWorker.getWorkerRequest(localPath)
            val updateRequest = OneTimeWorkRequestBuilder<UploadRecordingWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).setInputMerger(OverwritingInputMerger::class).setInputData(
                workDataOf(
                    "orderId" to orderId
                )
            ).build()
            WorkManager.getInstance(context).beginWith(uploadRequest).then(updateRequest).enqueue()
        }
    }
}
