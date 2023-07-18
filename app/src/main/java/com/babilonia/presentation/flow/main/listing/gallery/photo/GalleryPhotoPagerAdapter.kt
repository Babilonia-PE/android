package com.babilonia.presentation.flow.main.listing.gallery.photo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.babilonia.R
import com.babilonia.domain.model.ListingImage
import com.babilonia.presentation.extension.withGlideFitImage

class GalleryPhotoPagerAdapter constructor(
    private val data: List<ListingImage>? = mutableListOf()
) : PagerAdapter() {
    override fun isViewFromObject(view: View, item: Any): Boolean {
        return view == item as FrameLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val listingImage = data?.get(position)
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_gallery_photo, container, false)
        val image: ImageView = view.findViewById(R.id.ivListingImage)
        image.withGlideFitImage(listingImage?.url)
        (container as ViewPager).addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        (container as ViewPager).removeView(item as FrameLayout)
    }

    override fun getCount(): Int {
        return data?.size ?: 0
    }


}