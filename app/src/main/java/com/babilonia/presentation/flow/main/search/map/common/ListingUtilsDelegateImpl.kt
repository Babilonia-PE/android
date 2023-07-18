package com.babilonia.presentation.flow.main.search.map.common

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.presentation.extension.pxValue
import com.babilonia.presentation.extension.safeLet
import com.babilonia.presentation.view.CustomTypefaceSpan
import java.text.NumberFormat
import java.util.*

// Created by Anton Yatsenko on 09.07.2019.
object ListingUtilsDelegateImpl : ListingsUtilsDelegate {
    override fun getListingTypeSmall(context: Context, listing: Listing): SpannableStringBuilder? {
        val typeUpperCase = getLocalizedType(context, listing.listingType)
        val localizedPropertyType = PropertyType.getLocalizedPropertyName(context.resources, listing.propertyType)
        val type = context.getString(
            R.string.property_for_sale_small,
            localizedPropertyType,
            typeUpperCase
        )
        val upperCase = type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1)
        val medium = ResourcesCompat.getFont(context, R.font.avenit_medium)
        val heavy = ResourcesCompat.getFont(context, R.font.avenir_heavy)
        val spannable = SpannableStringBuilder(upperCase)
        return safeLet(medium, heavy, { medium, heavy ->
            spannable.setSpan(
                CustomTypefaceSpan("", medium, 10f.pxValue(TypedValue.COMPLEX_UNIT_SP, context)),
                0,
                localizedPropertyType.lastIndex.plus(1),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                CustomTypefaceSpan("", heavy, 12f.pxValue(TypedValue.COMPLEX_UNIT_SP, context)),
                localizedPropertyType.lastIndex.plus(2),
                upperCase.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable
        })
    }

    override fun setPriceSubtitle(listing: Listing, context: Context): String {
        return if (listing.listingType == Constants.SALE) {
            val numberFormat = NumberFormat.getInstance()
            numberFormat.maximumFractionDigits = 2
            val pricePerMeter =
                numberFormat.format(listing.price?.toDouble()?.div(listing.area ?: EmptyConstants.EMPTY_INT)?.toInt())
            context.getString(R.string.price_per_meter, pricePerMeter)
        } else {
            context.getString(R.string.per_month)
        }
    }

    override fun TextView.setVisibilityForBath(listing: Listing) {
        visibility =
            if (listing.bathroomsCount == null || listing.bathroomsCount == 0 || listing.propertyType == "land")
                View.GONE
            else
                View.VISIBLE
    }

    override fun TextView.setVisibilityForBeds(listing: Listing) {
        visibility = if (listing.bedroomsCount == null || listing.bedroomsCount == 0 ||
            (listing.propertyType != "house" && listing.propertyType != "apartment")
        )
            View.GONE
        else
            View.VISIBLE
    }

    override fun TextView.setVisibilityForArea(listing: Listing) {
        visibility = if (listing.area == null || listing.area == 0)
            View.GONE
        else
            View.VISIBLE
    }

    override fun TextView.setVisibilityForParking(listing: Listing) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewedListingIconByType(type: String?): Int {
        return when (type) {
            Constants.APARTMENT.toLowerCase(Locale.ROOT) -> R.drawable.ic_apartment_viewed
            Constants.COMMMERCIAL.toLowerCase(Locale.ROOT) -> R.drawable.ic_commercial_viewed
            Constants.HOUSE.toLowerCase(Locale.ROOT) -> R.drawable.ic_house_viewed
            Constants.OFFICE.toLowerCase(Locale.ROOT) -> R.drawable.ic_office_viewed
            Constants.ROOM.toLowerCase(Locale.ROOT) -> R.drawable.ic_room_viewed
            else -> R.drawable.ic_land_viewed
        }
    }

    override fun getListingIconByType(type: String?): Int {
        return when (type) {
            Constants.APARTMENT.toLowerCase(Locale.ROOT) -> R.drawable.ic_apartment_small
            Constants.COMMMERCIAL.toLowerCase(Locale.ROOT) -> R.drawable.commercial_black
            Constants.HOUSE.toLowerCase(Locale.ROOT) -> R.drawable.house_black
            Constants.OFFICE.toLowerCase(Locale.ROOT) -> R.drawable.office_black
            Constants.ROOM.toLowerCase(Locale.ROOT) -> R.drawable.ic_room_black
            else -> R.drawable.land_black
        }
    }

    override fun getColorForListingType(listing: Listing, context: Context): ColorStateList {
        return if (listing.listingType == Constants.SALE) {
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.sale_color))
        } else {
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.rent_color))
        }
    }

    override fun getTextColorForListingType(listing: Listing, context: Context): Int {
        return if (listing.listingType == Constants.SALE) {
            ContextCompat.getColor(context, android.R.color.white)
        } else {
            ContextCompat.getColor(context, R.color.dark_blue_grey)
        }
    }

    override fun getLocalizedType(context: Context, unlocalizedType: String?): String {
        return if (unlocalizedType != null &&
            unlocalizedType.toLowerCase(Locale.ENGLISH) == Constants.RENT) {
            context.getString(R.string.rent)
        } else {
            context.getString(R.string.sale)
        }
    }
}