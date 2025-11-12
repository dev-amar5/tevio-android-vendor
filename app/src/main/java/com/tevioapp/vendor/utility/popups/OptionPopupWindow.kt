package com.tevioapp.vendor.utility.popups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.RowPopupListBinding
import com.tevioapp.vendor.databinding.ViewPopupOptionsBinding
import com.tevioapp.vendor.presentation.common.base.adapter.QuickAdapter

/**
 * Base Popup Menu
 */
class OptionPopupWindow<M>(
    private val context: Context,
    private val dataList: List<M>,
    private val getTitle: (bean: M) -> String,
    private val isSelected: (bean: M) -> Boolean,
    private val onOptionClick: (bean: M, position: Int) -> Unit
) : RelativePopupWindow(context) {
    private var anchor: View? = null
    private val binding = ViewPopupOptionsBinding.inflate(LayoutInflater.from(context))

    init {
        contentView = binding.root
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(null)
        setAdapter()
    }


    private fun setAdapter() {
        val list = mutableListOf<String>()
        dataList.forEach {
            list.add(getTitle.invoke(it))
        }
        QuickAdapter<String>(context, getBinding = { parent, _ ->
            RowPopupListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        }, onItemClick = { _, _, _, position ->
            dismiss()
            onOptionClick.invoke(dataList[position], position)
        }, onBindView = { bindingInner, bean, position ->
            if (bindingInner is RowPopupListBinding) {
                bindingInner.tvOption.text =bean
                if(isSelected.invoke(dataList[position])){
                    bindingInner.tvOption.setTextColor(ContextCompat.getColor(context, R.color.orange))
                }else{
                    bindingInner.tvOption.setTextColor(ContextCompat.getColor(context, R.color.black))
                }

            }
        }).apply {
            setItemList(list)
            addToRecyclerView(binding.recyclerViewMenu)
        }
    }

    fun showBelowLeft(anchor: View) {
        if (anchor.tag != null) {
            return
        }
        this.anchor = anchor
        showOnAnchor(
            anchor = anchor,
            vertPos = VerticalPosition.BELOW,
            horizontalPosition = HorizontalPosition.ALIGN_RIGHT,
            y = R.dimen._10sdp,
            asDropDown = false
        )

    }
    fun showBelowCenter(anchor: View) {
        if (anchor.tag != null) {
            return
        }
        this.anchor = anchor
        showOnAnchor(
            anchor = anchor,
            vertPos = VerticalPosition.BELOW,
            horizontalPosition = HorizontalPosition.CENTER,
            y = R.dimen._10sdp,
            asDropDown = false
        )

    }

    fun showBelow(anchor: View, @DimenRes marginHorizontal: Int = R.dimen._20sdp) {
        if (anchor.tag != null) {
            return
        }
        this.anchor = anchor
        val context = anchor.context
        val horizontalMargin = try {
            context.resources.getDimensionPixelSize(marginHorizontal)
        } catch (e: Exception) {
            0
        }
        val parentWidth = (anchor.parent as? View)?.width ?: anchor.rootView.width
        val popupWidth = parentWidth - (2 * horizontalMargin)
        this.width = popupWidth
        showOnAnchor(
            anchor = anchor,
            vertPos = VerticalPosition.BELOW,
            horizontalPosition = HorizontalPosition.CENTER,
            y = R.dimen._minus10sdp,
            asDropDown = true
        )
    }

    fun showToLeftBelow(anchor: View) {
        if (anchor.tag != null) {
            return
        }
        this.anchor = anchor
        showOnAnchor(
            anchor = anchor,
            vertPos = VerticalPosition.BELOW,
            horizontalPosition = HorizontalPosition.ALIGN_RIGHT,
            y = R.dimen._minus10sdp,
            asDropDown = true
        )
    }

    fun showToRightTop(anchor: View) {
        if (anchor.tag != null) {
            return
        }
        this.anchor = anchor
        showOnAnchor(
            anchor = anchor,
            vertPos = VerticalPosition.ABOVE,
            horizontalPosition = HorizontalPosition.ALIGN_LEFT,
            y = R.dimen._minus10sdp,
            asDropDown = false
        )
    }


    fun showAsDropMenu(anchor: View) {
        if (anchor.tag != null) {
            return
        }
        this.anchor = anchor
        val context = anchor.context
        this.width = anchor.width
        val yOff = try {
            context.resources.getDimensionPixelSize(R.dimen._10sdp)
        } catch (e: Exception) {
            0
        }
        showAsDropDown(
            anchor, 0, yOff
        )
    }

    override fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        super.setOnDismissListener(onDismissListener)
        anchor?.tag = null
    }


}
