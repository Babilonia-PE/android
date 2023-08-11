package com.babilonia.presentation.flow.main.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.VhTopListingItemBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.withGlide
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl.getColorForListingType
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl.getListingIconByType
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl.getListingTypeSmall
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl.setPriceSubtitle
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl.setVisibilityForArea
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl.setVisibilityForBath
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl.setVisibilityForBeds
import com.babilonia.presentation.flow.main.search.map.common.ListingsUtilsDelegate

class TopListingsAdapter(private val listener: ListingActionsListener) :
    RecyclerView.Adapter<TopListingsAdapter.TopListingViewHolder>(),
    ListingsUtilsDelegate by ListingUtilsDelegateImpl {

    private val items = arrayListOf<Listing>()
    private var currentUserId = EmptyConstants.EMPTY_LONG

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopListingViewHolder {
        return TopListingViewHolder.create(parent, R.layout.vh_top_listing_item)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TopListingViewHolder, position: Int) {
        holder.bind(items[position], currentUserId, listener)
    }

    fun setItems(newItems: List<Listing>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setCurrentUserId(id: Long) {
        currentUserId = id
    }

    class TopListingViewHolder(val binding: VhTopListingItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listing: Listing, currentUserId: Long, listener: ListingActionsListener) {
            with (binding) {
                val context = itemView.context
                model = listing
                ivTopListingImage.withGlide(listing.getPreviewImageUrl(), cornerRadius = 16)
                clListingTypeContainer.text = getListingTypeSmall(context, listing)
                clListingTypeContainer.backgroundTintList = getColorForListingType(listing, context)
                clListingTypeContainer.setCompoundDrawablesWithIntrinsicBounds(
                    getListingIconByType(listing.propertyType),
                    0,
                    0,
                    0
                )
                tvListingBath.setVisibilityForBath(listing)
                tvListingBeds.setVisibilityForBeds(listing)
                tvListingArea.setVisibilityForArea(listing)
                tvSubPrice.text = setPriceSubtitle(listing, context)
                if (listing.user?.id == currentUserId) {
                    ivFavorite.invisible()
                } else {
                    ivFavorite.visible()
                    ivFavorite.setOnClickListener {
                        listing.id?.let { id -> listener.onFavouriteClicked(ivFavorite.isChecked, id) }
                    }
                }

                when (listing.adPlan) {
                    PaymentPlanKey.PLUS -> {
                        ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_plus_yellow_24)
                        ivPlanIcon.visible()
                    }
                    PaymentPlanKey.PREMIUM -> {
                        ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_premium_blue_24)
                        ivPlanIcon.visible()
                    }
                    else -> ivPlanIcon.invisible()
                }

                itemView.setOnClickListener {
                    listener.onPreviewClicked(listing)
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup, @LayoutRes layoutId: Int) =
                TopListingViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false))
        }
    }
}