package com.babilonia.presentation.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.babilonia.R
import com.babilonia.ar.tag.ArTag
import com.babilonia.data.model.ar.tag.MovableArObject
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.domain.utils.ArTagType
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.setImageUrl
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.view.bottombar.getDrawableCompat
import kotlinx.android.synthetic.main.item_ar_tag_large.view.*
import kotlinx.android.synthetic.main.item_ar_tag_medium.view.tvTagType
import kotlinx.android.synthetic.main.item_ar_tag_small.view.ivTagImage
import kotlinx.android.synthetic.main.item_ar_tag_small.view.tvTagCost
import kotlinx.android.synthetic.main.item_ar_tag_small.view.tvTagDistanceCount
import kotlinx.android.synthetic.main.item_ar_tag_small.view.tvTagDistanceUnit
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

class ArTagViewFactory @Inject constructor() {

    private val numberFormat = NumberFormat.getInstance(Locale.US)

    fun inflateView(parent: ViewGroup, arObject: MovableArObject): View {
        return when (arObject.arTagSizeType) {
            ArTagType.SMALL -> inflateSmall(parent, arObject)
            ArTagType.MEDIUM -> inflateMedium(parent, arObject)
            ArTagType.LARGE -> inflateLarge(parent, arObject)
            ArTagType.PIN -> inflatePin(parent)
            ArTagType.INITIAL -> throw IllegalStateException("Can't inflate INITIAL state of ar object ${arObject.listing}")
        }
    }

    private fun inflateSmall(parent: ViewGroup, arObject: MovableArObject): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_ar_tag_small, parent, false)
            .apply { bindDataCommon(this, arObject) }
    }

    private fun inflateMedium(parent: ViewGroup, arObject: MovableArObject): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_ar_tag_medium, parent, false)
            .apply { bindDataMedium(this, arObject) }
    }

    private fun inflateLarge(parent: ViewGroup, arObject: MovableArObject): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_ar_tag_large, parent, false)
            .apply { bindDataLarge(this, arObject) }
    }

    private fun inflatePin(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_ar_tag_pin, parent, false)
            .apply {
                visibility = View.GONE
                x = ArTag.HIDDEN_POSITION_COORDS
                y = ArTag.HIDDEN_POSITION_COORDS
            }
    }

    private fun bindDataCommon(view: View, arObject: MovableArObject) {
        view.apply {
            ivTagImage.setImageUrl(
                imageUrl = arObject.listing.getPreviewImageUrl(),
                placeholder = context.getDrawableCompat(R.drawable.ic_listing_placeholder),
                cornerRadius = getCornerRadius(context, arObject.arTagSizeType)
            )

            tvTagCost.text = context.getString(R.string.price_template, numberFormat.format(arObject.listing.price))
            tvTagDistanceUnit.text = context.getString(R.string.meters)
            tvTagDistanceCount.text = arObject.distance.toInt().toString()

            //Place the tag outside the parent view and hide it
            visibility = View.GONE
            x = ArTag.HIDDEN_POSITION_COORDS
            y = ArTag.HIDDEN_POSITION_COORDS
        }
    }

    private fun bindDataMedium(view: View, arObject: MovableArObject) {
        view.apply {
            bindDataCommon(this, arObject)
            tvTagType.text = PropertyType.getLocalizedPropertyName(resources, arObject.listing.propertyType)
        }
    }

    private fun bindDataLarge(view: View, arObject: MovableArObject) {
        view.apply {
            bindDataMedium(this, arObject)

            with (arObject.listing) {
                if (bedroomsCount != null && bedroomsCount != 0) {
                    tvBedroomCount.visible()
                    tvBedroomCount.text = view.context.getString(R.string.beds_numb_small, bedroomsCount)
                } else {
                    tvBedroomCount.invisible()
                }
                if (bathroomsCount != null && bathroomsCount != 0) {
                    tvBathroomCount.visible()
                    tvBathroomCount.text = view.context.getString(R.string.bath_numb_small, bathroomsCount)
                } else {
                    tvBathroomCount.invisible()
                }

                ivDivider.visibleOrGone(tvBedroomCount.isVisible && tvBathroomCount.isVisible)
            }
        }
    }

    private fun getCornerRadius(context: Context, sizeType: ArTagType): Float {
        val radiusId = when (sizeType) {
            ArTagType.SMALL -> R.dimen.ar_tag_small_image_corner_radius
            ArTagType.MEDIUM -> R.dimen.ar_tag_medium_image_corner_radius
            ArTagType.LARGE -> R.dimen.ar_tag_large_image_corner_radius
            ArTagType.PIN -> R.dimen.ar_tag_large_image_corner_radius
            ArTagType.INITIAL -> 0
        }
        return context.resources.getDimension(radiusId)
    }
}