package com.babilonia.presentation.flow.main.search.map.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.babilonia.R
import com.babilonia.domain.model.Listing
import com.babilonia.presentation.utils.PriceFormatter
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


object MarkerManager : ListingsUtilsDelegate by ListingUtilsDelegateImpl {
    private val session = mutableListOf<Listing>()
    private val cache = mutableListOf<Listing>()
    private var selected: Listing? = null

    fun cache(listings: List<Listing>) {
        cache.addAll(listings)
    }

    fun setSelected(listing: Listing) {
        selected = listing
    }

    fun getSelected(): Listing? {
        return selected
    }

    fun removeSelected() {
        selected = null
    }

    fun getCache(): MutableList<Listing> {
        return cache
    }

    fun clearCache() {
        cache.clear()
    }

    fun push(listing: Listing) {
        if (session.find { it.id == listing.id } == null) {
            session.add(listing)
        }
    }

    private fun containsItem(listing: Listing): Boolean {
        return session.find { it.id == listing.id } != null
    }

    fun clearSession() {
        session.clear()
    }

    @SuppressLint("SetTextI18n")
    fun createMarker(listing: Listing, context: Context): BitmapDescriptor? {
        val history = containsItem(listing)
        val view = LayoutInflater.from(context).inflate(R.layout.listing_map_pin, null) as TextView

        if (history) {
            view.setCompoundDrawablesWithIntrinsicBounds(
                getViewedListingIconByType(listing.propertyType),
                0,
                0,
                0
            )
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(
                getListingIconByType(listing.propertyType),
                0,
                0,
                0
            )
        }
        if (selected?.id == listing.id) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            } else {
                val drawableCompat = ContextCompat.getDrawable(context, getListingIconByType(listing.propertyType))
                drawableCompat?.let {
                    DrawableCompat.setTint(it, ContextCompat.getColor(context, R.color.white))
                }
                view.setCompoundDrawablesWithIntrinsicBounds(
                    drawableCompat,
                    null,
                    null,
                    null
                )
            }
            view.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        listing.price?.let { view.text = "$${PriceFormatter.format(it.toLong())}" }
        return BitmapDescriptorFactory.fromBitmap(loadBitmapFromView(view))
    }

    private fun loadBitmapFromView(v: View): Bitmap? {
        if (v.measuredHeight <= 0) {
            v.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.layout(0, 0, v.measuredWidth, v.measuredHeight)
            v.draw(c)
            return b
        }
        return null
    }
}