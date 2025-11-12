package com.tevioapp.vendor.utility.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.tevioapp.vendor.network.helper.MetaData

/**
 * Created by arvind on 6/5/2023.
 */
class EndlessVerticalScroller(
     val recyclerView: RecyclerView,
     val layoutManager: LayoutManager = LinearLayoutManager(recyclerView.context),
     val loadPage: (page: Int) -> Unit,
     var threshold: Int = 3
) : RecyclerView.OnScrollListener() {

    private var pageNo = 1
    private var isPagingEnded = false
    private var isLoading = false

    private val reverseLayout: Boolean = when (layoutManager) {
        is LinearLayoutManager -> layoutManager.reverseLayout
        else -> false
    }

    init {
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(this)
    }

    private fun getLastVisibleItemPosition(): Int {
        return when (val lm = layoutManager) {
            is LinearLayoutManager -> lm.findLastVisibleItemPosition().takeIf { it != RecyclerView.NO_POSITION } ?: 0
            else -> 0
        }
    }

    private fun getTotalItemCount(): Int {
        return layoutManager.itemCount
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (isPagingEnded || isLoading) return
        val lastVisiblePosition = getLastVisibleItemPosition()
        val totalItemCount = getTotalItemCount()

        val isScrollingDown = dy > 0
        val isScrollingUp = dy < 0
        val shouldLoadMore = lastVisiblePosition >= totalItemCount - threshold

        // **Only trigger pagination when scrolling DOWN**
        if (!reverseLayout && isScrollingDown && shouldLoadMore) {
            startLoadingNextPage()
        } else if (reverseLayout && isScrollingUp && shouldLoadMore) {
            startLoadingNextPage()
        }
    }

    private fun startLoadingNextPage() {
        isLoading = true
        pageNo++
        loadPage.invoke(pageNo)
    }

    fun onPageLoaded() {
        isLoading = false
    }

    fun setMetadata(metaData: MetaData?) {
        isPagingEnded = metaData?.isLastPage ?: true
    }

    fun reloadPage() {
        pageNo = 1
        isPagingEnded = false
        isLoading = false
        loadPage.invoke(pageNo)
    }

    fun getCurrentPage(): Int = pageNo
}
