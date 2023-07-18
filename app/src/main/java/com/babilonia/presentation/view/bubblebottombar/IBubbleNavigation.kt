package com.babilonia.presentation.view.bubblebottombar

import android.graphics.Typeface
import com.babilonia.presentation.view.bubblebottombar.listener.BubbleNavigationChangeListener


// Created by Anton Yatsenko on 12.06.2019.
interface IBubbleNavigation {

    var currentActiveItemPosition: Int
    fun setNavigationChangeListener(navigationChangeListener: BubbleNavigationChangeListener)

    fun setTypeface(typeface: Typeface)

    fun setCurrentActiveItem(position: Int, restoreState: Boolean = false)

    fun setBadgeValue(position: Int, value: String)
}