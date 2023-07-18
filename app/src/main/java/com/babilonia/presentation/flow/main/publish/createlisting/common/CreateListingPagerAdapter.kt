package com.babilonia.presentation.flow.main.publish.createlisting.common

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

// Created by Anton Yatsenko on 30.05.2019.
class CreateListingPagerAdapter(fm: Fragment) : FragmentStateAdapter(fm) {
    private val fragments = mutableListOf<Fragment>()

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    fun add(fragment: Fragment) {
        fragments.add(fragment)
    }

}