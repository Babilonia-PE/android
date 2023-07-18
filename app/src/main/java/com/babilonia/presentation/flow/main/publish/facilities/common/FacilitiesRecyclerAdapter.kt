package com.babilonia.presentation.flow.main.publish.facilities.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.databinding.ListItemFacilityBinding
import com.babilonia.domain.model.Facility
import com.babilonia.presentation.extension.loadSvg

// Created by Anton Yatsenko on 07.06.2019.
class FacilitiesRecyclerAdapter(
    private val facilityChangeListener: FacilityChangeListener
) :
    RecyclerView.Adapter<FacilitiesRecyclerAdapter.ViewHolder>() {


    private var data = mutableListOf<Facility>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_facility, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data[position]
        holder.binding?.let { binding ->
            holder.binding?.model = model
            holder.itemView.setOnClickListener {
                holder.binding?.cbFacility?.isChecked = binding.cbFacility.isChecked.not()
            }
            binding.ivFacility.loadSvg(model.icon)
            binding.cbFacility.setOnCheckedChangeListener { _, _ ->
                model.isChecked = binding.cbFacility.isChecked
                facilityChangeListener.onChange(model)
            }
            binding.executePendingBindings()
        }


    }

    override fun getItemId(position: Int): Long {
        return data[position].id.toLong()
    }


    override fun getItemCount(): Int = data.size

    fun addAll(newData: List<Facility>) {
        val callback = FacilitiesDiffUtil(data, newData)
        val diffUtils = DiffUtil.calculateDiff(callback)
        data = newData.toMutableList()
        diffUtils.dispatchUpdatesTo(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ListItemFacilityBinding? = DataBindingUtil.bind(itemView)

    }

}