package com.tevioapp.vendor.presentation.common.base.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tevioapp.vendor.BR
import com.tevioapp.vendor.utility.extensions.preventDoubleClick

class QuickAdapter<M>(
    val context: Context,
    private val getBinding: (parent: ViewGroup, viewType: Int) -> ViewDataBinding,
    private val onItemClick: ((adapter: QuickAdapter<M>, view: View, bean: M, position: Int) -> Unit)? = null,
    private val onBindView: ((binding: ViewDataBinding, bean: M, position: Int) -> Unit)? = null,
    private val getViewType: ((bean: M, position: Int) -> Int)? = null,
) : BaseAdapter<M>() {
    inner class Holder<S : ViewDataBinding>(var binding: S) : BaseViewHolder(binding) {
        override fun onItemViewClick(v: View) {
            v.preventDoubleClick()
            runCatching {
                onItemClick?.invoke(
                    this@QuickAdapter, v, dataList[bindingAdapterPosition], bindingAdapterPosition
                )
            }.onFailure { e ->
                e.printStackTrace()
            }

        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        onBindView?.invoke(holder.viewDataBinding, dataList[position], position)
        super.onBindViewHolder(holder, position)
    }

    override fun getItemViewType(position: Int): Int {
        getViewType?.invoke(dataList[position], position)?.let {
            return it
        }
        return super.getItemViewType(position)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding: ViewDataBinding = getBinding.invoke(parent, viewType)
        val holder = Holder(binding)
        binding.setVariable(BR.holder, holder)
        return holder
    }

    fun addToRecyclerView(
        recyclerView: RecyclerView,
        layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
    ): QuickAdapter<M> {
        recyclerView.adapter = this
        recyclerView.layoutManager = layoutManager
        return this
    }

}

