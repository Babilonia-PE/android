package com.babilonia.presentation.view.textpickerdialog

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

// Created by Anton Yatsenko on 22.07.2019.
class TextPickerAdapter(
    private var mContext: Context,
    internal var itemClickCallBack: ItemClickCallBack,
    titles: List<String>,
    var selectedItem: String,
    var dataList: List<TitleModel>
) : RecyclerView.Adapter<TextPickerAdapter.DialogViewHolder>() {

    internal var focusedItem = 0

    init {
        focusedItem = titles.indexOf(selectedItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        return DialogViewHolder(LayoutInflater.from(mContext).inflate(R.layout.picker_item, parent, false))
    }


    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        holder.number.text = dataList[position].value
        if (dataList[position].isHasFocus) {
            holder.number.setTextColor(ContextCompat.getColor(mContext, R.color.topaz))
            holder.number.typeface = ResourcesCompat.getFont(holder.itemView.context, R.font.avenir_heavy)
            holder.number.textSize = 18f
        } else {
            holder.number.setTextColor(ContextCompat.getColor(mContext, R.color.gunmetal))
            holder.number.typeface = ResourcesCompat.getFont(holder.itemView.context, R.font.avenit_medium)
            holder.number.textSize = 16f
        }
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
        fun onItemClicked(value: String, position: Int)
    }

    interface ValueAvailableListener {
        fun onValueAvailable(value: String, position: Int)
    }

}