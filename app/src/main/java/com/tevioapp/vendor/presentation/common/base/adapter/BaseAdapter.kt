package com.tevioapp.vendor.presentation.common.base.adapter

import android.view.View
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.tevioapp.vendor.BR
import com.tevioapp.vendor.databinding.HolderEmptyItemBinding
import com.tevioapp.vendor.utility.event.helper.Resource
import com.tevioapp.vendor.utility.util.EndlessVerticalScroller

abstract class BaseAdapter<M> : RecyclerView.Adapter<BaseViewHolder>() {
    private var _scroller: EndlessVerticalScroller? = null
    fun getScroller(): EndlessVerticalScroller {
        if (_scroller == null) {
            throw Exception("Scroller is not initialized")
        }
        return _scroller!!
    }

    protected val dataList: MutableList<M> = ArrayList()

    fun getItemList() = dataList

    override fun getItemCount(): Int = dataList.size

    fun setItemList(newItems: List<M>?) {
        dataList.clear()
        newItems?.let { dataList.addAll(it) }
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<M>?) {
        val index = dataList.size
        newItems?.let {
            dataList.addAll(it)
            notifyItemRangeInserted(index, it.size)
        }
    }

    fun addItem(item: M) {
        dataList.add(item)
        notifyItemInserted(dataList.size - 1)
    }

    fun addItem(index: Int, item: M) {
        dataList.add(index, item)
        notifyItemInserted(index)
    }

    fun setItem(index: Int, item: M) {
        if (index in 0..<dataList.size) {
            dataList[index] = item
            notifyItemChanged(index)
        }
    }

    fun removeItem(position: Int) {
        if (position in dataList.indices) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun removeAllItem() {
        setItemList(null)
    }

    fun getAllItems(): List<M> {
        return dataList
    }

    fun getItem(position: Int): M? = dataList.getOrNull(position)


    fun addToRecyclerView(
        scroller: EndlessVerticalScroller
    ): BaseAdapter<M> {
        scroller.recyclerView.adapter = this
        this._scroller = scroller
        return this
    }


    fun isNotEmpty(): Boolean {
        return dataList.isNotEmpty()
    }

    fun isEmpty(): Boolean {
        return dataList.isEmpty()
    }

    fun setDummyData(bean: M, count: Int) {
        dataList.clear()
        for (i in 0..count) {
            dataList.add(bean)
        }
        notifyDataSetChanged()
    }

    fun notifyDataUpdated() {
        notifyItemRangeChanged(0, itemCount)
    }

    fun setPagedData(data: Resource<List<M>>) {
        val metaData = data.metaData
        if (metaData != null) {
            if (metaData.currentPage == 1) {
                setItemList(data.data)
            } else {
                addItems(data.data)
            }
            _scroller?.setMetadata(data.metaData)
        } else {
            setItemList(data.data)
            _scroller?.setMetadata(null)
        }
    }

    @CallSuper
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.viewDataBinding.setVariable(BR.holder, holder)
        holder.viewDataBinding.setVariable(BR.bean, getItem(position))
        holder.viewDataBinding.executePendingBindings()
    }
}

abstract class BaseViewHolder(val viewDataBinding: ViewDataBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    open fun onItemViewClick(v: View) {
        // Handle click
    }
}

class HolderEmpty(val binding: HolderEmptyItemBinding) : BaseViewHolder(binding)