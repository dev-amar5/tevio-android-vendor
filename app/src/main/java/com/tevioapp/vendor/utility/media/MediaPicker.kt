package com.tevioapp.vendor.utility.media

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tevioapp.vendor.R
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.permissions.QuickPermissionsOptions
import com.tevioapp.vendor.utility.permissions.runWithPermissions
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaPicker private constructor(
    private val appContext: Context, private val activity: FragmentActivity
) {
    private val provider: String = "${appContext.packageName}.provider"
    private var photoUri: Uri? = null
    private var videoUri: Uri? = null

    private lateinit var launcherCapture: ActivityResultLauncher<Uri>
    private lateinit var launcherVideoCapture: ActivityResultLauncher<Uri>
    private lateinit var launcherImagePick: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var launcherVideoPick: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var launcherCrop: ActivityResultLauncher<Intent>
    private lateinit var launcherFile: ActivityResultLauncher<Array<String>>
    private lateinit var launcherMultipleFile: ActivityResultLauncher<Array<String>>

    private var onResult: ((Result) -> Unit)? = null
    private var options: Options = Options(Type.NONE)

    companion object {
        fun getInstance(fragment: Fragment): MediaPicker {
            return MediaPicker(fragment.requireContext(), fragment.requireActivity()).apply {
                initializeResultLaunchers(fragment)
            }
        }

        fun getInstance(activity: FragmentActivity): MediaPicker {
            return MediaPicker(activity, activity).apply {
                initializeResultLaunchers(activity)
            }
        }
    }

    val defaultCropOptions: () -> UCrop.Options = {
        val options = UCrop.Options()
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        options.withAspectRatio(1f, 1f)
        options.withMaxResultSize(512, 512)
        options.setCropFrameStrokeWidth(5)
        options.setCropGridStrokeWidth(2)
        options.setCompressionQuality(20)
        options.setStatusBarColor(ContextCompat.getColor(appContext, R.color.white))
        options.setToolbarColor(ContextCompat.getColor(appContext, R.color.white))
        options.setToolbarWidgetColor(ContextCompat.getColor(appContext, R.color.black))
        options
    }

    private fun initializeResultLaunchers(owner: Any) {
        val registryOwner = when (owner) {
            is FragmentActivity -> owner
            is Fragment -> owner
            else -> throw IllegalArgumentException("Unsupported owner type")
        }

        launcherCrop =
            registryOwner.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                handleCropResult(result)
            }

        launcherCapture =
            registryOwner.registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
                if (result) {
                    if (options.uCropOptions != null) {
                        startCrop(appContext, photoUri)
                    } else {
                        uriToFile(
                            appContext,
                            photoUri,
                            onSuccess = { onResult?.invoke(Result(true, "Image Picked", it)) },
                            onError = { onResult?.invoke(Result(false, it)) })
                    }
                } else onResult?.invoke(Result(false, "Capture failed"))
            }

        launcherImagePick =
            registryOwner.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (options.uCropOptions != null) {
                    startCrop(appContext, uri)
                } else {
                    uriToFile(
                        appContext,
                        uri,
                        onSuccess = { onResult?.invoke(Result(true, "Image Picked", it)) },
                        onError = { onResult?.invoke(Result(false, it)) })
                }
            }

        launcherVideoCapture =
            registryOwner.registerForActivityResult(ActivityResultContracts.CaptureVideo()) { result ->
                if (result) {
                    uriToFile(
                        appContext,
                        videoUri,
                        onSuccess = { onResult?.invoke(Result(true, "Video captured", it)) },
                        onError = { onResult?.invoke(Result(false, it)) })
                } else onResult?.invoke(Result(false, "Video capture failed"))
            }

        launcherVideoPick =
            registryOwner.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uriToFile(
                    appContext,
                    uri,
                    onSuccess = { onResult?.invoke(Result(true, "Video Picked", it)) },
                    onError = { onResult?.invoke(Result(false, "Video pick failed: $it")) })
            }

        launcherFile =
            registryOwner.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uriToFile(
                    appContext,
                    uri,
                    onSuccess = { onResult?.invoke(Result(true, "File selected", it)) },
                    onError = { onResult?.invoke(Result(false, "File selection failed $it")) })
            }

        launcherMultipleFile =
            registryOwner.registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                val files = arrayListOf<File>()
                uris.forEach { uri ->
                    uriToFile(appContext, uri, onSuccess = { files.add(it) })
                }
                if (files.isNotEmpty()) {
                    onResult?.invoke(Result(true, "Files selected", files = files))
                } else {
                    onResult?.invoke(Result(false, "File selection failed"))
                }
            }
    }

    private fun resolveRequest() {
        when (options.type) {
            Type.PICK_IMAGE -> launcherImagePick.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )

            Type.PICK_VIDEO -> launcherVideoPick.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )

            Type.CAPTURE_IMAGE -> captureImage()
            Type.RECORD_VIDEO -> captureVideo()
            Type.FILE_PDF -> if (options.count == 1) launcherFile.launch(getArrayTypePdf())
            else launcherMultipleFile.launch(getArrayTypePdf())

            Type.FILE_DOCX -> if (options.count == 1) launcherFile.launch(getArrayTypeDocx())
            else launcherMultipleFile.launch(getArrayTypeDocx())

            else -> onResult?.invoke(Result(false, "Unsupported operation"))
        }
    }

    fun showSinglePicker(options: Options, onResult: (Result) -> Unit) {
        this.onResult = onResult
        this.options = options
        resolveRequest()
    }

    private fun captureImage() {
        activity.runWithPermissions(
            Manifest.permission.CAMERA, options = QuickPermissionsOptions(handleRationale = false)
        ) {
            val file = createTempFile()
            if (file != null) {
                photoUri = FileProvider.getUriForFile(appContext, provider, file)
                photoUri?.let { launcherCapture.launch(it) }
            } else {
                onResult?.invoke(Result(false, "Unable to create temp image file"))
            }
        }
    }

    private fun captureVideo() {
        activity.runWithPermissions(
            Manifest.permission.CAMERA, options = QuickPermissionsOptions(handleRationale = false)
        ) {
            val file = createTempFile()
            if (file != null) {
                videoUri = FileProvider.getUriForFile(appContext, provider, file)
                videoUri?.let { launcherVideoCapture.launch(it) }
            } else {
                onResult?.invoke(Result(false, "Unable to create temp video file"))
            }
        }
    }

    private fun getArrayTypePdf() = arrayOf("application/pdf")

    private fun getArrayTypeDocx() = arrayOf(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )

    private fun startCrop(context: Context, uri: Uri?) {
        if (uri == null) {
            onResult?.invoke(Result(false, "Cropping failed: Uri not found"))
            return
        }
        val name = SimpleDateFormat("ddMMyyyHHmmss", Locale.US).format(Date()) + ".png"
        val destinationUri = Uri.fromFile(File(context.cacheDir, name))
        val uCrop = UCrop.of(uri, destinationUri)
        uCrop.withOptions(options.uCropOptions ?: defaultCropOptions())
        launcherCrop.launch(uCrop.getIntent(context))
    }

    private fun handleCropResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            Logger.d("Cropping path = ${resultUri?.path}")
            resultUri?.path?.let {
                onResult?.invoke(Result(true, "Cropping Successful", File(it)))
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            onResult?.invoke(Result(false, "Cropping error: ${cropError?.message}"))
        }
    }

    private fun uriToFile(
        context: Context, uri: Uri?, onSuccess: (File) -> Unit, onError: (String) -> Unit = {}
    ) {
        if (uri == null) {
            onError("Uri is null")
            return
        }
        try {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val file = createTempFile() ?: return onError("Cannot create temp file!")
            if (mimeType.startsWith("image/")) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(input, null, options) // read bounds
                    input.close()

                    val maxSize = 1080
                    var sampleSize = 1
                    while (options.outWidth / sampleSize > maxSize || options.outHeight / sampleSize > maxSize) {
                        sampleSize *= 2
                    }
                    context.contentResolver.openInputStream(uri)?.use { input2 ->
                        val bitmap = BitmapFactory.decodeStream(
                            input2,
                            null,
                            BitmapFactory.Options().apply { inSampleSize = sampleSize })
                        if (bitmap != null) {
                            val ratio = minOf(
                                maxSize.toFloat() / bitmap.width,
                                maxSize.toFloat() / bitmap.height,
                                1f
                            )
                            val scaledBitmap = bitmap.scale(
                                (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt()
                            )
                            FileOutputStream(file).use { out ->
                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            onSuccess(file)
                            return
                        }
                    }
                }
                // fallback to raw copy if image decoding fails
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                onSuccess(file)
            } else {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                onSuccess(file)
            }

        } catch (e: Exception) {
            onError("uriToFile failed: ${e.message}")
        }
    }


    @Throws(IOException::class)
    private fun createTempFile(): File? {
        return when (options.type) {
            Type.PICK_IMAGE, Type.CAPTURE_IMAGE -> File.createTempFile(
                "img_", ".png", appContext.cacheDir
            )

            Type.PICK_VIDEO, Type.RECORD_VIDEO -> File.createTempFile(
                "video_", ".mp4", appContext.cacheDir
            )

            Type.FILE_PDF -> File.createTempFile("file_", ".pdf", appContext.cacheDir)
            Type.FILE_DOCX -> File.createTempFile("file_", ".docx", appContext.cacheDir)
            Type.NONE -> null
        }
    }

    data class Result(
        val successful: Boolean,
        val message: String?,
        val file: File? = null,
        val files: List<File>? = null
    ) {
        override fun toString(): String {
            val filesStr = files?.joinToString { it.path }
            return "Result : $successful Message: $message File: ${file?.path} Files: $filesStr"
        }
    }

    enum class Type {
        CAPTURE_IMAGE, RECORD_VIDEO, PICK_IMAGE, PICK_VIDEO, FILE_PDF, FILE_DOCX, NONE
    }
}

data class Options(
    val type: MediaPicker.Type,
    val uCropOptions: UCrop.Options? = null,
    val title: String? = null,
    val count: Int = 1
)
