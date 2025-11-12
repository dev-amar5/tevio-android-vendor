package com.tevioapp.vendor.presentation.common.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.tevioapp.vendor.BR
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.utility.util.DataProvider
import com.tevioapp.vendor.utility.util.LoadingUtils
import javax.inject.Inject


abstract class BaseFragment<Binding : ViewDataBinding> : Fragment() {
    val tagName: String = this.javaClass.simpleName

    @Inject
    lateinit var sharePref: SharedPref

    @Inject
    lateinit var dataProvider: DataProvider

    lateinit var baseContext: Context
    lateinit var binding: Binding
    lateinit var parentActivity: BaseActivity<*>
    private lateinit var loadingUtils: LoadingUtils

    lateinit var baseHandler: Handler
    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseContext = context
        parentActivity = requireActivity() as BaseActivity<*>
        baseHandler = Handler(Looper.getMainLooper())
    }

    override fun onDetach() {
        baseHandler.removeCallbacksAndMessages(null)
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateView(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout: Int = getLayoutResource()
        binding = DataBindingUtil.inflate(layoutInflater, layout, container, false)
        loadingUtils = LoadingUtils(baseContext, binding)
        val viewModel = getViewModel()
        registerEvents(viewModel)
        binding.setVariable(BR.vm, viewModel)
        return binding.root
    }

    protected abstract fun getLayoutResource(): Int
    abstract fun getViewModel(): BaseViewModel
    protected abstract fun onCreateView(view: View, saveInstanceState: Bundle?)

    /**
     * register for network change events
     */
    private fun registerEvents(viewModel: BaseViewModel) {
        viewModel.obrMessage.observe(viewLifecycleOwner) {
            showShortMessage(it)
        }
    }

    fun showShortMessage(message: String?) {
        parentActivity.showShortMessage(message)
    }

    fun showLongMessage(message: String?) {
        parentActivity.showLongMessage(message)
    }

    fun startShimmer() {
        loadingUtils.startShimmer()
    }

    fun stopShimmer() {
        loadingUtils.stopShimmer()
    }

    fun showLoading() {
        loadingUtils.showLoading()
    }

    fun hideLoading() {
        loadingUtils.hideLoading()
    }

}