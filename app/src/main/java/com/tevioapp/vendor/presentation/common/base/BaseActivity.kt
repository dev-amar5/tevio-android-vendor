package com.tevioapp.vendor.presentation.common.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.tevioapp.vendor.BR
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.presentation.common.base.dialog.BaseAlertDialog
import com.tevioapp.vendor.presentation.views.errors.NoInternetActivity
import com.tevioapp.vendor.utility.connection.ConnectionLiveData
import com.tevioapp.vendor.utility.extensions.clearUserAndRestart
import com.tevioapp.vendor.utility.util.DataProvider
import com.tevioapp.vendor.utility.util.LoadingUtils
import com.tevioapp.vendor.utility.util.ThemeUtil
import javax.inject.Inject

abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity() {
    lateinit var baseHandler: Handler
    lateinit var loadingUtils: LoadingUtils

    @Inject
    lateinit var connection: ConnectionLiveData

    @Inject
    lateinit var sharePref: SharedPref

    @Inject
    lateinit var dataProvider: DataProvider

    val tagName: String = this.javaClass.simpleName
    private var dialogUnAuth: BaseAlertDialog? = null
    lateinit var binding: Binding
    val app: BaseApp
        get() = application as BaseApp


    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.applySavedTheme(this)
        baseHandler = Handler(Looper.getMainLooper())
        binding = DataBindingUtil.setContentView(this, getLayoutResource())
        setupInsets()
        loadingUtils = LoadingUtils(this, binding)
        val vm = getViewModel()
        binding.setVariable(BR.vm, vm)
        onCreateView(savedInstanceState)
        registerEvents(vm)
    }


    open fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, ime.bottom)
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                bottomPadding
            )
            insets
        }
    }

    /**
     * provide layout resource id
     */
    protected abstract fun getLayoutResource(): Int


    /**
     *  provide view model for activity
     */
    protected abstract fun getViewModel(): BaseViewModel

    /**
     * start writing your logic from here
     */
    protected abstract fun onCreateView(savedInstanceState: Bundle?)

    override fun onDestroy() {
        loadingUtils.release()
        baseHandler.removeCallbacksAndMessages(null)
        dialogUnAuth?.dismiss()
        super.onDestroy()
    }


    /**
     * register for network change events
     */
    private fun registerEvents(viewModel: BaseViewModel) {
        if (observeInternetChanges()) {
            connection.observe(this) { status ->
                if (status.connected.not()) {
                    startActivity(NoInternetActivity.newInstance(this))
                }
            }
        }
        viewModel.obrMessage.observe(this) {
            showShortMessage(it)
        }
        viewModel.obrUnAuthorize.observe(this) {
            if (dialogUnAuth?.isDialogVisible() == true) return@observe
            dialogUnAuth = BaseAlertDialog(
                this,
                title = it.title,
                message = it.message,
                positive = getString(R.string.okay),
                cancelable = false,
                onPositiveButtonClick = { dialog ->
                    dialog.dismiss()
                    clearUserAndRestart()
                }).show()
        }
    }

    fun showShortMessage(message: String?) {
        showMessage(message, LENGTH_SHORT)
    }

    fun showLongMessage(message: String?) {
        showMessage(message, LENGTH_LONG)
    }

    private fun showMessage(message: String?, duration: Int) {
        if (message.isNullOrEmpty()) return
        Toast.makeText(this, message, duration).show()
    }

    fun startShimmer() {
        loadingUtils.startShimmer()
    }

    fun stopShimmer() {
        loadingUtils.stopShimmer()
    }

    /**
     * show loading bottom sheet dialog
     */
    fun showLoading(cancelable: Boolean = true) {
        loadingUtils.showLoading(cancelable)
    }

    /**
     * hide loading sheet
     */
    fun hideLoading() {
        loadingUtils.hideLoading()
    }

    open fun observeInternetChanges(): Boolean = true


}