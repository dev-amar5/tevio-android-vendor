package com.tevioapp.vendor.presentation.common.base
/*

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.attendence.sys.BR
import com.attendence.sys.databinding.ViewProgressSheetBinding

class ProgressSheet(val callback: BaseCallback) : BottomSheetDialogFragment() {
    var binding: ViewProgressSheetBinding? = null

    companion object {
        const val TAG = "ProgressSheet"

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ViewProgressSheetBinding.inflate(inflater)
        binding!!.setVariable(BR.callback, callback)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callback.onBind(binding)
        isCancelable=false
        //  dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun onResume() {
        super.onResume()
    }

    interface BaseCallback {
        fun onClick(view: View?)
        fun onBind(bind: ViewProgressSheetBinding?)
    }

*/
