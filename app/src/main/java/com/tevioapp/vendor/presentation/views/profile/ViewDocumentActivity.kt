package com.tevioapp.vendor.presentation.views.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.common.FileInfo
import com.tevioapp.vendor.databinding.ActivityViewDocumentBinding
import com.tevioapp.vendor.databinding.ViewDocInfoBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.MediaUtils
import com.tevioapp.vendor.utility.extensions.findParcelDataList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewDocumentActivity : BaseActivity<ActivityViewDocumentBinding>() {
    private val viewModel: ProfileViewModel by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_view_document
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        binding.header.apply {
            ivLogo.isVisible = false
            tvHeading.isVisible = true
            tvHeading.text = intent.getStringExtra("title")
        }
        val fileInfoList = intent.findParcelDataList("data", FileInfo::class.java)
        binding.main.removeAllViews()
        fileInfoList?.forEach { file ->
            ViewDocInfoBinding.inflate(layoutInflater, binding.main, true).apply {
                filePath = file.url
                title = file.title
            }
        }
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_back -> finish()
                R.id.iv_download -> {
                    MediaUtils.downloadMultipleFiles(this, fileInfoList.orEmpty())
                }
            }
        }
    }

    companion object {
        fun newInstance(context: Context, title: String, file: ArrayList<FileInfo>): Intent {
            return Intent(context, ViewDocumentActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("title", title)
                putParcelableArrayListExtra("data", file)
            }
        }
    }

}



