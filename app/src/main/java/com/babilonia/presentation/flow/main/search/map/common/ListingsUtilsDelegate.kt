package com.babilonia.presentation.flow.main.search.map.common

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.widget.TextView
import com.babilonia.domain.model.Listing

// Created by Anton Yatsenko on 09.07.2019.
interface ListingsUtilsDelegate {
    fun getColorForListingType(listing: Listing, context: Context): ColorStateList
    fun getListingIconByType(type: String?): Int
    fun getListingTypeSmall(context: Context, listing: Listing): SpannableStringBuilder?
    fun getTextColorForListingType(listing: Listing, context: Context): Int
    fun getViewedListingIconByType(type: String?): Int
    fun setPriceSubtitle(listing: Listing, context: Context): String
    fun getLocalizedType(context: Context, unlocalizedType: String?): String
    fun TextView.setVisibilityForBath(listing: Listing)
    fun TextView.setVisibilityForBeds(listing: Listing)
    fun TextView.setVisibilityForArea(listing: Listing)
    fun TextView.setVisibilityForParking(listing: Listing)
}