package com.babilonia.presentation.utils

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

class StartSnapHelper : LinearSnapHelper() {

    var hasEmptyFooter = true // If true, will not stop snapping if the end of the list is reached

    private var verticalOrientationHelper: OrientationHelper? = null
    private var horizontalOrientationHelper: OrientationHelper? = null

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager,
                                              targetView: View
    ): IntArray? {
        val out = IntArray(2)

        out[0] = if (layoutManager.canScrollHorizontally()) {
            distanceToStart(targetView, getHorizontalHelper(layoutManager))
        } else {
            0
        }

        out[1] = if (layoutManager.canScrollVertically()) {
            distanceToStart(targetView, getVerticalHelper(layoutManager))
        } else {
            0
        }

        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager is LinearLayoutManager) {
            return if (layoutManager.canScrollHorizontally()) {
                getStartView(layoutManager, getHorizontalHelper(layoutManager))
            } else {
                getStartView(layoutManager, getVerticalHelper(layoutManager))
            }
        }
        return super.findSnapView(layoutManager)
    }

    private fun getStartView(layoutManager: RecyclerView.LayoutManager,
                             helper: OrientationHelper
    ): View? {

        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()

            val isLastItem = if (hasEmptyFooter) {
                false
            } else {
                layoutManager.findLastCompletelyVisibleItemPosition() ==
                        (layoutManager.getItemCount() - 1)
            }
            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null
            }

            val child = layoutManager.findViewByPosition(firstChild)

            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                && helper.getDecoratedEnd(child) > 0) {
                child
            } else {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                    null
                } else {
                    layoutManager.findViewByPosition(firstChild + 1)
                }
            }
        }

        return super.findSnapView(layoutManager)
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        return verticalOrientationHelper
            ?: OrientationHelper.createVerticalHelper(layoutManager).apply {
                verticalOrientationHelper = this
            }
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        return horizontalOrientationHelper
            ?: OrientationHelper.createHorizontalHelper(layoutManager).apply {
                horizontalOrientationHelper = this
            }
    }
}