package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tevioapp.vendor.R
import com.tevioapp.vendor.presentation.common.base.adapter.ViewPager2Adapter


class CustomRoundIndicatorTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {

    private var viewPager: ViewPager2? = null
    private var interactionEnabled: Boolean = true

    init {
        tabRippleColor = null
        post { setupTabsAlignmentAndMode() }

        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                updateAllTabs()
            }

            override fun onTabUnselected(tab: Tab?) {}

            override fun onTabReselected(tab: Tab?) {}
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (interactionEnabled) super.onInterceptTouchEvent(ev) else true
    }


    private fun setupTabsAlignmentAndMode() {
        tabGravity = GRAVITY_FILL
        tabMode = MODE_FIXED
    }

    fun setupWithViewPager(viewPager: ViewPager2, adapter: ViewPager2Adapter) {
        this.viewPager = viewPager
        viewPager.adapter = adapter
        TabLayoutMediator(this, viewPager) { tab, position ->
            tab.customView = createTabView(position)
            tab.tag = position
        }.attach()

        post {
            getTabAt(0)?.select()
            viewPager.setCurrentItem(0, false)
        }
    }

    private fun createTabView(position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.tab_with_custom_count, null)

        val ivLeftView = view.findViewById<ImageView>(R.id.iv_left_view)
        val ivRightView = view.findViewById<ImageView>(R.id.iv_right_view)
        val ivBgSolid = view.findViewById<ImageView>(R.id.iv_bg_solid)
        val clMain = view.findViewById<ImageView>(R.id.cl_main)
        val tvCount = view.findViewById<TextView>(R.id.tv_count)

        tvCount.text = buildString { append((position + 1)) }

        when (position) {
            0 -> setupTabViewStyle(
                ivBgSolid, clMain, ivLeftView, ivRightView, R.color.orange, View.GONE, View.VISIBLE
            )

            1 -> setupTabViewStyle(
                ivBgSolid,
                clMain,
                ivLeftView,
                ivRightView,
                R.color.orange_alpha_12,
                View.VISIBLE,
                View.VISIBLE
            )

            2 -> setupTabViewStyle(
                ivBgSolid,
                clMain,
                ivLeftView,
                ivRightView,
                R.color.orange_alpha_12,
                View.VISIBLE,
                View.GONE
            )
        }

        return view
    }

    private fun setupTabViewStyle(
        ivBgSolid: ImageView?,
        clMain: ImageView?,
        ivLeftView: ImageView?,
        ivRightView: ImageView?,
        imageTintColor: Int,
        leftViewVisibility: Int,
        rightViewVisibility: Int
    ) {
        clMain?.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, imageTintColor))
        ivBgSolid?.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, imageTintColor))
        ivLeftView?.visibility = leftViewVisibility
        ivRightView?.visibility = rightViewVisibility
    }

    private fun updateAllTabs() {
        val selectedPosition = selectedTabPosition
        for (i in 0 until tabCount) {
            val tab = getTabAt(i)
            updateCustomTabView(tab, selectedPosition)
        }
    }

    private fun updateCustomTabView(tab: Tab?, selectedTabPosition: Int) {
        val position = tab?.tag as? Int ?: return
        val ivLeftView = tab.customView?.findViewById<ImageView>(R.id.iv_left_view)
        val ivRightView = tab.customView?.findViewById<ImageView>(R.id.iv_right_view)
        val ivBgSolid = tab.customView?.findViewById<ImageView>(R.id.iv_bg_solid)
        val clMain = tab.customView?.findViewById<ImageView>(R.id.cl_main)

        val darkOrange = ContextCompat.getColor(context, R.color.orange)
        val lightOrange = ContextCompat.getColor(context, R.color.light_orange)

        val isVisitedOrSelected = position <= selectedTabPosition
        ivBgSolid?.imageTintList =
            ColorStateList.valueOf(if (isVisitedOrSelected) darkOrange else lightOrange)
        clMain?.imageTintList =
            ColorStateList.valueOf(if (isVisitedOrSelected) darkOrange else lightOrange)

        // Connector line logic
        ivLeftView?.backgroundTintList = null
        ivRightView?.backgroundTintList = null

        when (position) {
            0 -> {
                ivRightView?.backgroundTintList =
                    ColorStateList.valueOf(if (selectedTabPosition > 0) darkOrange else lightOrange)
            }

            1 -> {
                ivLeftView?.backgroundTintList =
                    ColorStateList.valueOf(if (selectedTabPosition >= 1) darkOrange else lightOrange)
                ivRightView?.backgroundTintList =
                    ColorStateList.valueOf(if (selectedTabPosition > 1) darkOrange else lightOrange)
            }

            2 -> {
                ivLeftView?.backgroundTintList =
                    ColorStateList.valueOf(if (selectedTabPosition >= 2) darkOrange else lightOrange)
            }
        }
    }

    override fun selectTab(tab: Tab?) {
        super.selectTab(tab)
        invalidate()
    }

    fun setTabInteractionEnabled(enable: Boolean) {
        interactionEnabled = enable
    }
}
