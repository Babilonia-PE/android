package com.babilonia.presentation.flow.main.publish.photos.common

import com.babilonia.R
import com.babilonia.databinding.ListItemListingPhotoBinding
import com.babilonia.domain.model.ListingImage
import com.babilonia.presentation.base.BaseRecyclerAdapter
import com.babilonia.presentation.base.BaseViewHolder
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.withGlide

// Created by Anton Yatsenko on 11.06.2019.
class ListingPhotosRecyclerAdapter : BaseRecyclerAdapter<ListItemListingPhotoBinding>() {
    private val data = mutableListOf<ListingImage>()
    var listingPhotosListener: ListingPhotosListener? = null

    init {
        setHasStableIds(true)
    }

    override fun bindItem(holder: BaseViewHolder<ListItemListingPhotoBinding>, position: Int) {
        val listingImage = data[position]
        holder.binding.ivListingImage.withGlide(listingImage.url)
        holder.binding.ivMore.setOnClickListener {
            listingPhotosListener?.onClick(position, listingImage.primary)
        }
        if (listingImage.primary) {
            holder.binding.tvListingMainImage.visible()
        } else {
            holder.binding.tvListingMainImage.invisible()
        }
    }

    override fun getLayoutId(position: Int) = R.layout.list_item_listing_photo

    override fun getItemCount(): Int = data.size
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun add(image: ListingImage) {
        if (data.isEmpty()) {
            image.primary = true
        }
        data.add(image)
        notifyItemInserted(data.size - 1)
        listingPhotosListener?.onSizeChanged(data)

    }

    fun add(images: List<ListingImage>?) {
        images?.let {
            data.addAll(it)
            notifyDataSetChanged()
            listingPhotosListener?.onSizeChanged(data)
        }
    }

    fun remove(position: Int) {
        notifyDataSetChanged()
        data.removeAt(position)
        listingPhotosListener?.onSizeChanged(data)
    }

    fun setMainPicture(position: Int) {
        for (i in data.indices) {
            data[i].primary = i == position
        }
        notifyDataSetChanged()
    }
}