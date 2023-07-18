package com.babilonia.presentation.flow.main.publish.mylistings.common

import androidx.recyclerview.widget.DiffUtil
import com.babilonia.domain.model.Listing

// Created by Anton Yatsenko on 21.06.2019.
class ListingsDiffUtils constructor(private val oldList: List<Listing>, private val newList: List<Listing>) :
    DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldList.isEmpty()) return false
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldList.isEmpty()) return false
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}