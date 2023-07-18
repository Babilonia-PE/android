package com.babilonia.presentation.flow.main.publish.facilities.common

import androidx.recyclerview.widget.DiffUtil
import com.babilonia.domain.model.Facility

// Created by Anton Yatsenko on 27.06.2019.
class FacilitiesDiffUtil(val data: List<Facility>, val newData: List<Facility>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (data.isEmpty()) return false
        return data[oldItemPosition].id == newData[newItemPosition].id
    }

    override fun getOldListSize(): Int = data.size

    override fun getNewListSize(): Int = newData.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (data.isEmpty()) return false
        return data[oldItemPosition] == newData[newItemPosition]
    }

}