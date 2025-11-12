package com.tevioapp.vendor.presentation.common.compoundviews
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tevioapp.vendor.R

class StateRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var emptyView: View? = null
    private var emptyViewId: Int = View.NO_ID

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.StateRecyclerView, 0, 0)
        try {
            emptyViewId = typedArray.getResourceId(R.styleable.StateRecyclerView_emptyViewId, View.NO_ID)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (emptyViewId != View.NO_ID) {
            emptyView = (parent as? View)?.findViewById(emptyViewId)
        }
        checkEmptyState()
    }

    fun setEmptyView(view: View) {
        emptyView = view
        checkEmptyState()
    }

    private fun checkEmptyState() {
        val isEmpty = adapter?.itemCount == 0
        emptyView?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() = checkEmptyState()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkEmptyState()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkEmptyState()
        })
    }
}
