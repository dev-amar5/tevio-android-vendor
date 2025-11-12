package com.tevioapp.vendor.presentation.common.base.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager2Adapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    constructor(activity: FragmentActivity) : this(
        activity.supportFragmentManager, activity.lifecycle
    )

    constructor(fragment: Fragment) : this(
        fragment.childFragmentManager, fragment.lifecycle
    )

    private val mFragmentList: MutableList<Fragment> = ArrayList()
    private val mFragmentTitleList = arrayListOf<String>()

    fun getTabTitle(position: Int): String {
        return mFragmentTitleList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }


    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun getFragment(position: Int): Fragment? {
        return mFragmentList.getOrNull(position)
    }

}