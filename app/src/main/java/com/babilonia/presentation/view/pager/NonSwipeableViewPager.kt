package com.babilonia.presentation.view.pager

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager


// Created by Anton Yatsenko on 29.05.2019.
class NonSwipeableViewPager : ViewPager {

    constructor(context: Context) : super(context) {
        setNoSwipeScroller()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setNoSwipeScroller()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    //down one is added for smooth scrolling

    private fun setNoSwipeScroller() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, NoSwipeScroller(context))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    inner class NoSwipeScroller(context: Context) : Scroller(context, DecelerateInterpolator()) {

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, 350 /*1 secs*/)
        }
    }
}