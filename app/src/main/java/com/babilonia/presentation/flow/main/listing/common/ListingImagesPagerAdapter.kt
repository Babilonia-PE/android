package com.babilonia.presentation.flow.main.listing.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.babilonia.R
import com.babilonia.domain.model.ListingImage
import com.babilonia.presentation.extension.withGlide

// Created by Anton Yatsenko on 20.06.2019.
class ListingImagesPagerAdapter constructor(
    private val data: List<ListingImage>? = mutableListOf(),
    private val cornerRadius: Int = 1,
    private val onImageClick: () -> Unit
) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as FrameLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val listingImage = data?.get(position)
        val view = LayoutInflater.from(container.context).inflate(R.layout.list_item_viewpager_image, container, false)
        val image: ImageView = view.findViewById(R.id.ivListingImage)
        view.setOnClickListener {
            onImageClick()
        }
        image.withGlide(listingImage?.url, cornerRadius = cornerRadius)
        (container as ViewPager).addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as FrameLayout)
    }

    override fun getCount(): Int {
        return data?.size ?: 0
    }


}