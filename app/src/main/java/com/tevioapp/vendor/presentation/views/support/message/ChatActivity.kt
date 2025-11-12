package com.tevioapp.vendor.presentation.views.support.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.ChatMessage
import com.tevioapp.vendor.data.ThreadDetail
import com.tevioapp.vendor.data.common.MediaFile
import com.tevioapp.vendor.databinding.ActivityChatBinding
import com.tevioapp.vendor.databinding.HolderChatMediaLeftBinding
import com.tevioapp.vendor.databinding.HolderChatMediaRightBinding
import com.tevioapp.vendor.databinding.HolderChatTextLeftBinding
import com.tevioapp.vendor.databinding.HolderChatTextRightBinding
import com.tevioapp.vendor.databinding.HolderEmptyItemBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.MediaViewModel
import com.tevioapp.vendor.presentation.common.base.adapter.QuickAdapter
import com.tevioapp.vendor.presentation.common.base.dialog.FullScreenImageDialog
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.animateVisibility
import com.tevioapp.vendor.utility.extensions.withDelay
import com.tevioapp.vendor.utility.keyboard.KeyboardDetector
import com.tevioapp.vendor.utility.media.MediaPicker
import com.tevioapp.vendor.utility.media.Options
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class ChatActivity : BaseActivity<ActivityChatBinding>() {
    private val viewModel: ChatActivityVM by viewModels()
    private val viewModelMedia: MediaViewModel by viewModels()
    private lateinit var threadId: String
    private lateinit var adapterChat: QuickAdapter<ChatMessage>
    private lateinit var mediaPicker: MediaPicker
    private var textMessage: String = ""
    private var threadDetail: ThreadDetail? = null


    override fun getLayoutResource(): Int = R.layout.activity_chat

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(savedInstanceState: Bundle?) {
        initView()
        setHeaderView()
        setAdapter()
        setListeners()
        registerObservers()
        viewModel.initSocket()
    }

    private fun initView() {
        viewModel.compositeDisposable.add(
            KeyboardDetector(this).getObserver().subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.isOpen) {
                        scrollToBottom()
                    }
                })
        mediaPicker = MediaPicker.getInstance(this)
        threadId = intent.getStringExtra("threadId").orEmpty()
        if (threadId.isEmpty()) {
            finish()
        }
    }

    private fun setAdapter() {
        val myUserId = sharePref.getMyUserId().orEmpty()
        adapterChat = QuickAdapter(context = this@ChatActivity, getBinding = { parent, viewType ->
            when (viewType) {
                -VIEW_TYPE_TEXT -> HolderChatTextLeftBinding.inflate(
                    layoutInflater, parent, false
                )

                VIEW_TYPE_TEXT -> HolderChatTextRightBinding.inflate(
                    layoutInflater, parent, false
                )

                -VIEW_TYPE_MEDIA -> HolderChatMediaLeftBinding.inflate(
                    layoutInflater, parent, false
                )

                VIEW_TYPE_MEDIA -> HolderChatMediaRightBinding.inflate(
                    layoutInflater, parent, false
                )

                else -> HolderEmptyItemBinding.inflate(layoutInflater, parent, false)
            }
        }, getViewType = { bean, _ ->
            when (bean.type) {
                Enums.CHAT_MESSAGE_IMAGE -> VIEW_TYPE_MEDIA
                Enums.CHAT_MESSAGE_TEXT -> VIEW_TYPE_TEXT
                else -> VIEW_TYPE_UNKNOWN
            } * if (bean.senderId == myUserId) 1 else -1
        }, onItemClick = { _, view, bean, _ ->
            when (view.id) {
                R.id.iv_chat -> {
                    FullScreenImageDialog(this, bean.url.orEmpty()).show()
                }
            }
        })
        binding.rvOne.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = adapterChat
            itemAnimator = null
        }
    }

    private fun setListeners() = with(binding) {
        var typingMessage = false
        etMessage.doOnTextChanged { _, _, _, _ ->
            if (typingMessage.not()) {
                typingMessage = true
                viewModel.sendTyping(threadId)
                withDelay(TYPING_TIMEOUT) {
                    typingMessage = false
                }
            }
        }
    }


    private fun showTypingIndicator() {
        baseHandler.removeCallbacks(runnableHideTypingIndicator)
        binding.vTyping.animateVisibility(true)
        baseHandler.postDelayed(runnableHideTypingIndicator, TYPING_TIMEOUT)
    }

    private fun registerObservers() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_back -> {
                    finish()
                }

                R.id.iv_call -> {
                    showCallOptionSheet()
                }

                R.id.iv_send -> {
                    sendMessage()
                }

                R.id.iv_camera -> {
                    mediaPicker.showSinglePicker(Options(MediaPicker.Type.CAPTURE_IMAGE)) { result ->
                        if (result.successful && result.file != null) {
                            uploadImage(result.file)
                        }
                    }
                }

                R.id.iv_attachment -> {
                    mediaPicker.showSinglePicker(Options(MediaPicker.Type.PICK_IMAGE)) { result ->
                        if (result.successful && result.file != null) {
                            uploadImage(result.file)
                        }
                    }
                }
            }
        }

        viewModel.apiThreadDetail(threadId).observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.lottieHeader.isVisible = true
                }

                Status.SUCCESS -> {
                    binding.lottieHeader.isVisible = false
                    threadDetail = resource.data
                    setHeaderView()
                }

                else -> {
                    binding.lottieHeader.isVisible = false
                    showShortMessage(resource.message)
                }
            }

        }

        viewModel.obrNewMessage.observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // No loader
                }

                Status.SUCCESS -> {
                    textMessage = ""
                    resource.data?.let {
                        adapterChat.addItem(it)
                        scrollToBottom()
                    }
                }

                else -> {
                    binding.etMessage.setText(textMessage)
                    showShortMessage(resource.message)
                }
            }

        }

        viewModel.obrAllMessage.observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (adapterChat.isEmpty()) startShimmer()
                }

                Status.SUCCESS -> {
                    stopShimmer()
                    adapterChat.setItemList(resource.data.orEmpty())
                    scrollToBottom()
                }

                else -> {
                    startShimmer()
                    showShortMessage(resource.message)
                }
            }

        }

        viewModel.obrMessageTyping.observe(this) { resource ->
            if (resource.status == Status.SUCCESS) {
                showTypingIndicator()
            }
        }
    }

    private fun setHeaderView() = with(binding.header) {
        ivLogo.isVisible = false
        tvHeading.isVisible = true
        tvHeading.text = threadDetail?.name.orEmpty()
    }

    private fun showCallOptionSheet() {
        CallOptionSheet(threadId).showSheet(this)
    }

    private fun scrollToBottom() {
        baseHandler.postDelayed({
            val position = adapterChat.itemCount - 1
            if (position >= 0) {
                binding.rvOne.scrollToPosition(position)
            }
        }, 100)

    }

    private fun uploadImage(file: File) {
        viewModelMedia.attach(
            lifecycleOwner = this,
            container = binding.container,
            label = "Image Uploading",
            mediaFile = MediaFile(
                mediaType = MediaFile.TYPE.IMAGE, localUrl = file.path
            )
        ) { mediaFile ->
            mediaFile.remoteUrl?.let { url ->
                val request = JSONObject()
                request.put("thread_id", threadId)
                request.put("url", url)
                request.put("type", Enums.CHAT_MESSAGE_IMAGE)
                viewModel.sendMessage(request)
            }
        }
    }

    private fun sendMessage() {
        textMessage = binding.etMessage.text.toString()
        if (textMessage.isEmpty()) return
        val request = JSONObject()
        request.put("thread_id", threadId)
        request.put("message", textMessage)
        request.put("type", Enums.CHAT_MESSAGE_TEXT)
        binding.etMessage.text = null
        viewModel.sendMessage(request)
    }

    private val runnableHideTypingIndicator = Runnable {
        binding.vTyping.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        viewModel.apiChatMessageList(threadId)
    }

    override fun observeInternetChanges(): Boolean {
        return false
    }

    companion object {
        private const val VIEW_TYPE_UNKNOWN = 1
        private const val VIEW_TYPE_TEXT = 2
        private const val VIEW_TYPE_MEDIA = 3
        private const val TYPING_TIMEOUT = 2000L
        fun newInstance(context: Context, threadId: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("threadId", threadId)
            }
        }
    }
}
