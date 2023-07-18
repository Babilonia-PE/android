package com.babilonia.presentation.view.picker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R


// Created by Anton Yatsenko on 06.06.2019.
class NumberPickerAdapter(
    private var mContext: Context,
    internal var itemClickCallBack: ItemClickCallBack,
    private var valueAvailableListener: ValueAvailableListener,
    start: Int,
    last: Int,
    var selectedItem: Int
) : RecyclerView.Adapter<NumberPickerAdapter.DialogViewHolder>() {
    private var inflater: LayoutInflater = LayoutInflater.from(mContext)
    internal var dataList: ArrayList<IntervalModel> = ArrayList()
    internal var instance: NumberPickerAdapter

    internal var focusedItem = 0

    init {
        focusedItem = last - selectedItem
        initList(start, last)
        instance = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        return DialogViewHolder(inflater.inflate(R.layout.picker_item, parent, false))
    }

    fun findForItemToShow(value: Int) {
        var position = -1
        for (i in 0 until dataList.size) {
            if (dataList[i].value == value) {
                position = i
                break
            }
        }
        if (position != -1) {
            if (focusedItem <= -1) {
                focusedItem = position
                dataList[position].isHasFocus = true
                notifyItemChanged(position)
            } else {
                dataList[focusedItem].isHasFocus = (false)
                notifyItemChanged(focusedItem)
                focusedItem = position
                dataList[position].isHasFocus = (true)
                notifyItemChanged(position)
            }
            valueAvailableListener.onValueAvailable(dataList[position].value, position)
        }

    }

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        holder.number.text = dataList[position].value.toString()
        if (dataList[position].isHasFocus) {
            holder.number.setTextColor(ContextCompat.getColor(mContext, R.color.topaz))
            holder.number.typeface = ResourcesCompat.getFont(holder.itemView.context, R.font.avenir_heavy)
            holder.number.textSize = 24f
        } else {
            holder.number.setTextColor(ContextCompat.getColor(mContext, R.color.gunmetal))
            holder.number.typeface = ResourcesCompat.getFont(holder.itemView.context, R.font.avenit_medium)
            holder.number.textSize = 16f
        }
    }

    private fun initList(start: Int, last: Int) {
        for (i in start..last) {
            dataList.add(IntervalModel(i, false))
        }
        dataList.reverse()
        dataList[dataList.indexOf(IntervalModel(selectedItem, false))].isHasFocus = (true)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class DialogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var number: TextView = itemView.findViewById(R.id.text_number)
        private var itemParent: FrameLayout = itemView.findViewById(R.id.item_parent)

        init {
            itemParent.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (v.id == R.id.item_parent) {
                if (focusedItem <= -1) {
                    focusedItem = layoutPosition
                    dataList[layoutPosition].isHasFocus = (true)
                    notifyItemChanged(layoutPosition)
                } else {
                    dataList[focusedItem].isHasFocus = (false)
                    notifyItemChanged(focusedItem)
                    focusedItem = layoutPosition
                    dataList[layoutPosition].isHasFocus = (true)
                    notifyItemChanged(layoutPosition)
                }
                itemClickCallBack.onItemClicked(dataList[layoutPosition].value, layoutPosition)
            }
        }
    }

    interface ItemClickCallBack {
        fun onItemClicked(value: Int, position: Int)
    }

    interface ValueAvailableListener {
        fun onValueAvailable(value: Int, position: Int)
    }

}