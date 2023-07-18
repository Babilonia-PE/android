package com.babilonia.presentation.flow.main.common

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.Constants
import com.babilonia.presentation.utils.RecyclerViewRealItemCounter
import io.reactivex.processors.PublishProcessor

// Created by Anton Yatsenko on 11.07.2019.
private const val PAGE_SIZE = 25
private const val VISIBLE_THRESHOLD = 5

class EndlessScrollListener(
    val layoutManager: LinearLayoutManager,
    val paginator: PublishProcessor<Int>,
    val realItemCounter: RecyclerViewRealItemCounter,
    var pageNumber: Int = 1
) : RecyclerView.OnScrollListener() {

    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private var isLoading = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        totalItemCount = layoutManager.itemCount
        lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        if (isLoading.not() && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD) &&
            realItemCounter.getRealItemCount() / PAGE_SIZE == pageNumber) {
            pageNumber++
            paginator.onNext(pageNumber)
            startLoading()
        }

    }

    fun startLoading() {
        isLoading = true
    }

    fun cancelLoading() {
        isLoading = false
    }

    fun reset() {
        pageNumber = 1
    }

    fun isFirstPage(): Boolean {
        return pageNumber == Constants.FIRST_PAGE
    }
}