package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tevioapp.vendor.R
import com.tevioapp.vendor.presentation.common.base.adapter.ViewPager2Adapter


class CustomIndicatorTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {

    private var viewPager: ViewPager2? = null
    private var previousIndex = 0
    private var isTabInteractionEnabled: Boolean = true

    private var selectedColor: Int = ContextCompat.getColor(context, R.color.white_fix)
    private var unSelectedColor: Int = ContextCompat.getColor(context, R.color.gray)

    init {
        // Read XML attributes if available
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout, 0, 0).apply {
            try {
                selectedColor = getColor(R.styleable.CustomTabLayout_selectedColor, selectedColor)
                unSelectedColor = getColor(R.styleable.CustomTabLayout_unSelectedColor, unSelectedColor)
            } finally {
                recycle()
            }
        }

        tabRippleColor = null
        post { setupTabsAlignmentAndMode() }

        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) = updateCustomTabView(tab, true)
            override fun onTabUnselected(tab: Tab?) = updateCustomTabView(tab, false)
            override fun onTabReselected(tab: Tab?) = updateCustomTabView(tab, true)
        })
    }

    private fun setupTabsAlignmentAndMode() {
        tabGravity = GRAVITY_FILL
        tabMode = MODE_FIXED
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (isTabInteractionEnabled) super.onInterceptTouchEvent(ev) else true
    }

    fun setTabInteractionEnabled(enabled: Boolean) {
        isTabInteractionEnabled = enabled
        (getChildAt(0) as? ViewGroup)?.let { vg ->
            for (i in 0 until vg.childCount) {
                vg.getChildAt(i).isEnabled = enabled
            }
        }
    }

    fun setSelectedColor(@ColorInt color: Int) {
        selectedColor = color
        refreshTabColors()
    }

    fun setUnSelectedColor(@ColorInt color: Int) {
        unSelectedColor = color
        refreshTabColors()
    }

    private fun refreshTabColors() {
        for (i in 0 until tabCount) {
            val tab = getTabAt(i)
            val isSelected = tab?.isSelected ?: false
            updateCustomTabView(tab, isSelected)
        }
    }

    fun setupWithViewPager(viewPager: ViewPager2, adapter: ViewPager2Adapter) {
        this.viewPager = viewPager
        viewPager.adapter = adapter

        TabLayoutMediator(this, viewPager) { tab, position ->
            tab.customView = createTabView(position)
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position < previousIndex) {
                    getTabAt(previousIndex)?.let { updateCustomTabView(it, false) }
                }
                getTabAt(position)?.select()
                previousIndex = position
            }
        })

        post {
            getTabAt(0)?.select()
            viewPager.setCurrentItem(0, false)
            previousIndex = 0
        }
    }

    protected open fun createTabView(position: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.tab_with_round_corners, null)
    }

    private fun updateCustomTabView(tab: Tab?, isSelected: Boolean) {
        tab?.customView?.findViewById<ImageView>(R.id.iv_tab)?.apply {
            val tintColor = if (isSelected) selectedColor else unSelectedColor
            imageTintList = ColorStateList.valueOf(tintColor)
        }
    }

    override fun selectTab(tab: Tab?) {
        super.selectTab(tab)
        invalidate()
    }
}



