package com.babilonia.presentation.flow.main.common

import com.babilonia.domain.model.Listing

// Created by Anton Yatsenko on 08.07.2019.
interface ListingActionsListener {
    fun onFavouriteClicked(isChecked: Boolean, id: Long)
    fun onPreviewClicked(listing: Listing)
}