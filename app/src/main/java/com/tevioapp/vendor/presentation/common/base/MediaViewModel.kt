package com.tevioapp.vendor.presentation.common.base

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tevioapp.vendor.data.common.FileUploadResource
import com.tevioapp.vendor.data.common.MediaFile
import com.tevioapp.vendor.databinding.ItemProgressBinding
import com.tevioapp.vendor.databinding.ItemUploadImageBgBinding
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.event.SingleLiveEvent
import com.tevioapp.vendor.utility.extensions.getContext
import com.tevioapp.vendor.utility.extensions.mediaSubscription
import com.tevioapp.vendor.utility.extensions.preventDoubleClick
import com.tevioapp.vendor.utility.extensions.setBlur
import com.tevioapp.vendor.utility.media.MediaPicker
import com.tevioapp.vendor.utility.media.Options
import com.tevioapp.vendor.utility.popups.OptionPopupWindow
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * ViewModel responsible for handling media selection and upload logic.
 * - Manages API calls for file uploads.
 * - Updates UI via LiveData (progress, success, error).
 * - Supports attaching upload views dynamically in a container layout.
 *
 * @property context Application context (injected by Hilt).
 * @property authRepo Repository that performs authenticated API calls, including media upload.
 */
@HiltViewModel
class MediaViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {
    /** RxJava disposable bag to manage API subscriptions. */
    private var compositeDisposable = CompositeDisposable()

    /**
     * Uploads a media file to the server.
     *
     * @param media The [MediaFile] to upload.
     * @param observer LiveEvent to emit upload states ([FileUploadResource]).
     * @return A [Disposable] representing the upload subscription.
     */
    private fun uploadMedia(
        media: MediaFile, observer: SingleLiveEvent<FileUploadResource<String>>
    ): Disposable {
        val disposable = authRepo.apiUploadMedia(media).mediaSubscription(observer)
        compositeDisposable.add(disposable)
        return disposable
    }


    /**
     * Attaches a new upload progress view into the container and starts uploading the file.
     *
     * @param lifecycleOwner Lifecycle for observing upload states.
     * @param container Container of views.
     * @param label Label to show as the file name.
     * @param mediaFile The [MediaFile] to be uploaded.
     * @param onSuccess Callback invoked when upload completes successfully.
     */
    fun attach(
        lifecycleOwner: LifecycleOwner,
        container: LinearLayout,
        label: String,
        mediaFile: MediaFile,
        onSuccess: (MediaFile) -> Unit
    ) {
        val context = lifecycleOwner.getContext() ?: return
        val binding = ItemProgressBinding.inflate(LayoutInflater.from(context), container, false)
        binding.tvFileName.text = label
        val observer = SingleLiveEvent<FileUploadResource<String>>()
        observer.observe(lifecycleOwner) { resource ->
            when (resource.status) {
                FileUploadResource.Status.UPLOADING -> {
                    binding.tvMessage.isGone = true
                    binding.ivInfo.isGone = true
                    binding.ivRetry.isGone = true
                    binding.progressBar.isVisible = true
                    binding.tvPercent.isVisible = true
                    val percent = resource.getProgress()
                    binding.tvPercent.text = resource.getProgressDisplay()
                    binding.progressBar.isIndeterminate = percent !in 1..99
                    if (percent in 1..99) binding.progressBar.setProgress(percent, true)
                }

                FileUploadResource.Status.SUCCESS -> {
                    onSuccess.invoke(mediaFile.apply {
                        localUrl = null
                        remoteUrl = resource.result
                    })
                    container.removeView(binding.root)
                }

                FileUploadResource.Status.ERROR -> {
                    binding.tvMessage.isVisible = true
                    binding.ivRetry.isVisible = true
                    binding.ivInfo.isVisible = true
                    binding.progressBar.isGone = true
                    binding.tvPercent.isGone = true
                    binding.tvPercent.text = ""
                    binding.progressBar.progress = 0
                    binding.tvMessage.text = resource.message
                }
            }
        }

        var disposable = uploadMedia(mediaFile, observer)

        binding.ivRetry.setOnClickListener {
            compositeDisposable.remove(disposable)
            disposable.dispose()
            disposable = uploadMedia(mediaFile, observer)
        }

        binding.ivCross.setOnClickListener {
            compositeDisposable.remove(disposable)
            disposable.dispose()
            container.removeView(binding.root)
        }

        container.addView(binding.root)
    }


    /**
     * Attaches image picker UI with upload functionality to a given binding.
     *
     * @param lifecycleOwner Lifecycle for observing LiveData.
     * @param binding ViewBinding for the image upload layout.
     * @param label Text label shown on the add button.
     * @param mediaPicker Helper class for picking or capturing images.
     * @param options Options for media picking (defaults to camera capture + crop).
     * @param obrMediaFile LiveData holding the selected [MediaFile].
     */
    fun attachImagePicker(
        lifecycleOwner: LifecycleOwner,
        binding: ItemUploadImageBgBinding,
        label: String,
        mediaPicker: MediaPicker,
        option: List<Pair<String, Options>>,
        obrMediaFile: MutableLiveData<MediaFile>
    ) {
        fun uploadMediaFile(result: MediaPicker.Result) {
            if (result.successful && result.file != null) {
                val mediaFile = obrMediaFile.value ?: MediaFile(mediaType = MediaFile.TYPE.IMAGE)
                mediaFile.localUrl = result.file.path
                obrMediaFile.value = mediaFile
                val observer = SingleLiveEvent<FileUploadResource<String>>()
                observer.observe(lifecycleOwner) { resource ->
                    when (resource.status) {
                        FileUploadResource.Status.UPLOADING -> {
                            binding.vProgress.apply {
                                if (root.isGone) {
                                    root.isVisible = true
                                    binding.main.setBlur(true)
                                }
                                val percent = resource.getProgress()
                                tvPercent.text = resource.getProgressDisplay()
                                if (percent in 1..99) {
                                    progressBar.isIndeterminate = false
                                    progressBar.setProgress(percent, true)
                                } else {
                                    progressBar.isIndeterminate = true
                                }
                            }
                        }

                        FileUploadResource.Status.SUCCESS -> {
                            binding.vProgress.apply {
                                root.isVisible = false
                                binding.main.setBlur(false)
                                progressBar.progress = 0
                            }
                            mediaFile.apply {
                                remoteUrl = resource.result
                                localUrl = null
                            }
                            obrMediaFile.value = mediaFile
                        }

                        FileUploadResource.Status.ERROR -> {
                            binding.main.setBlur(false)
                            binding.vProgress.apply {
                                root.isVisible = false
                                progressBar.progress = 0
                            }
                            lifecycleOwner.getContext()?.let { context ->
                                Toast.makeText(
                                    context, "Upload failed: ${result.message}", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                uploadMedia(mediaFile, observer)
            }
        }
        obrMediaFile.observe(lifecycleOwner) {
            binding.media = it
        }
        binding.tvAdd.text = label

        binding.ivCross.setOnClickListener {
            obrMediaFile.value = null
        }

        binding.vCamera.setOnClickListener {
            it.preventDoubleClick()
            if (option.isNotEmpty()) {
                if (option.size > 1) {
                    OptionPopupWindow(binding.root.context, option, getTitle = { bean ->
                        bean.first
                    }, isSelected = { _ -> false }, onOptionClick = { bean, _ ->
                        mediaPicker.showSinglePicker(bean.second) { result ->
                            uploadMediaFile(result)
                        }
                    }).showBelowCenter(it)
                } else {
                    mediaPicker.showSinglePicker(option[0].second) { result ->
                        uploadMediaFile(result)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}
