package com.tevioapp.vendor.utility.rx

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.util.DefaultTextWatcher
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class RxSearch(
    viewModel: BaseViewModel,
    private val editText: EditText,
    private val onChange: (String) -> Unit
) : DefaultTextWatcher() {
    private val subject = PublishSubject.create<String>()

    init {
        editText.addTextChangedListener(this)
        viewModel.compositeDisposable.add(subscribe())
    }


    private fun subscribe(): Disposable {
        return subject.debounce(350, TimeUnit.MILLISECONDS).distinctUntilChanged().subscribeOn(
            Schedulers.io()
        ).observeOn(AndroidSchedulers.mainThread()).subscribe {
            onChange.invoke(it)
        }
    }

    fun setText(text: String?, withCallback: Boolean = false) {
        if (withCallback) editText.setText(text)
        else {
            editText.removeTextChangedListener(this)
            editText.setText(text)
            editText.addTextChangedListener(this)
        }
        try {
            text?.let {
                editText.setSelection(text.length)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getText(): String {
        return editText.text.toString()
    }

    fun clearFocus() {
        editText.clearFocus()
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        subject.onNext(p0.toString())
    }

    fun resetSearch(): Boolean {
        return if (editText.hasFocus() || editText.text.toString().isNotEmpty()) {
            try {
                val imm =
                    editText.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            editText.clearFocus()
            editText.text = null
            false
        } else true
    }

}