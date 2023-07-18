package com.babilonia.presentation.flow.main.listing.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.databinding.VhGalleryItemBinding
import com.babilonia.domain.model.ListingImage
import com.babilonia.presentation.extension.withGlide

class GalleryRecyclerAdapter(private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<GalleryRecyclerAdapter.GalleryViewHolder>() {

    private val items = mutableListOf<ListingImage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.vh_gallery_item,
                parent,
                false
            ),
            onItemClick
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position], position)
        if (isLargeItem(position)) {
            holder.setHeight(R.dimen.gallery_item_height_large)
        } else {
            holder.setHeight(R.dimen.gallery_item_height_small)
        }
    }

    fun setItems(newItems: List<ListingImage>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun isLargeItem(position: Int): Boolean {
        return position % 3 == 0
    }

    class GalleryViewHolder(
        private val binding: VhGalleryItemBinding,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListingImage, position: Int) {
            binding.ivGalleryImage.withGlide(item.url)
            binding.root.setOnClickListener { onClick(position) }
        }

        fun setHeight(@DimenRes sizeDp: Int) {
            val sizePx = binding.root.resources.getDimensionPixelSize(sizeDp)
            val layoutParams = binding.rootGalleryItem.layoutParams
            if (layoutParams.height != sizePx) {
                layoutParams.height = sizePx
                binding.rootGalleryItem.layoutParams = layoutParams
            }
        }
    }
}