package com.tevioapp.vendor.presentation.common.compoundviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tevioapp.vendor.R
import com.tevioapp.vendor.presentation.common.base.adapter.ViewPager2Adapter

class CustomTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {
    private var indicatorWidth: Int
    private var viewPager: ViewPager2? = null
    private val selectedColor: Int
    private val unSelectedColor: Int
    private val indicator: Drawable

    init {
        // Load custom attributes
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout, 0, 0).apply {
            try {
                selectedColor = getColor(
                    R.styleable.CustomTabLayout_selectedColor,
                    ContextCompat.getColor(context, R.color.orange)
                )
                unSelectedColor = getColor(
                    R.styleable.CustomTabLayout_unSelectedColor,
                    ContextCompat.getColor(context, R.color.tab_color_unselected)
                )
                indicatorWidth =
                    getDimensionPixelSize(R.styleable.CustomTabLayout_indicatorWidth, dpToPx(40))
            } finally {
                recycle()
            }
        }

        indicator = ContextCompat.getDrawable(context, R.drawable.tab_indicator)!!

        // Initialize tab layout
        post { setupTabsAlignmentAndMode() }
        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) = updateCustomTabView(tab, true)
            override fun onTabUnselected(tab: Tab?) = updateCustomTabView(tab, false)
            override fun onTabReselected(tab: Tab?) = updateCustomTabView(tab, true)
        })
    }

    private fun updateCustomTabView(tab: Tab?, selected: Boolean) {
        tab?.customView?.findViewById<TextView>(R.id.tab_text)?.apply {
            setTextColor(if (selected) selectedColor else unSelectedColor)
        }
    }

    private fun setupTabsAlignmentAndMode() {
        tabGravity = GRAVITY_START
        tabMode = MODE_SCROLLABLE
        isTabIndicatorFullWidth = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        indicatorWidth = dpToPx(40) // Ensure consistent size changes
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (tabCount > 0) {
            val tab: View? = getTabAt(selectedTabPosition)?.view
            if (tab != null) {
                val left = tab.left + tab.width / 2 - indicatorWidth / 2
                val right = left + indicatorWidth
                indicator.setBounds(left, height - dpToPx(4), right, height)
                indicator.draw(canvas)
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    fun setupWithViewPager(viewPager: ViewPager2, adapter: ViewPager2Adapter) {
        this.viewPager = viewPager
        viewPager.adapter = adapter

        TabLayoutMediator(this, viewPager) { tab, position ->
            tab.customView = createTabView(adapter.getTabTitle(position))
        }.attach()

        // Ensure tab selection updates on page change
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                getTabAt(position)?.select()
            }
        })
    }

    private fun createTabView(text: String): View {
        val tabView = LayoutInflater.from(context).inflate(R.layout.tab_item, null)
        val textView = tabView.findViewById<TextView>(R.id.tab_text)
        textView.text = text
        return tabView
    }

    override fun selectTab(tab: Tab?) {
        super.selectTab(tab)
        invalidate()
    }
}
