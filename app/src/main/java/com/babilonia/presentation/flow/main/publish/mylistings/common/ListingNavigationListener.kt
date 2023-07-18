package com.babilonia.presentation.flow.main.publish.mylistings.common

import com.babilonia.domain.model.Listing

// Created by Anton Yatsenko on 24.06.2019.
interface ListingNavigationListener {
    fun onDraftClicked(id: Long?)
    fun onMyListingClicked(id: Long?, status: String)
    fun onMenuClicked(listing: Listing)
}