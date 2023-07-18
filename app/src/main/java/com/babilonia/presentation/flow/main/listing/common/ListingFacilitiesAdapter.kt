package com.babilonia.presentation.flow.main.listing.common

import com.babilonia.R
import com.babilonia.databinding.ListItemListingFacilityBinding
import com.babilonia.domain.model.Facility
import com.babilonia.presentation.base.BaseRecyclerAdapter
import com.babilonia.presentation.base.BaseViewHolder
import com.babilonia.presentation.extension.loadSvg

// Created by Anton Yatsenko on 20.06.2019.
class ListingFacilitiesAdapter : BaseRecyclerAdapter<ListItemListingFacilityBinding>() {
    private val data = mutableListOf<Facility>()
    override fun bindItem(holder: BaseViewHolder<ListItemListingFacilityBinding>, position: Int) {
        val facility = data[position]
        holder.binding.ivFacility.loadSvg(facility.icon)
        holder.binding.tvFacility.text = facility.title
    }

    override fun getLayoutId(position: Int): Int = R.layout.list_item_listing_facility

    override fun getItemCount(): Int = data.size
    fun add(newData: List<Facility>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}
