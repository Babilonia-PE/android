package com.babilonia.presentation.flow.main.publish.photos.common

import com.babilonia.domain.model.ListingImage

// Created by Anton Yatsenko on 11.06.2019.
interface ListingPhotosListener {
    fun onClick(position: Int, primary: Boolean)
    fun onSizeChanged(newList: List<ListingImage>)
}