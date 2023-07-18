package com.babilonia.presentation.flow.main.publish.mylistings.common

import android.content.Context
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.databinding.ListItemMyListingBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.enums.PublishState
import com.babilonia.presentation.base.BaseRecyclerAdapter
import com.babilonia.presentation.base.BaseViewHolder
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.extension.withGlide
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl
import com.babilonia.presentation.flow.main.search.map.common.ListingsUtilsDelegate
import com.babilonia.presentation.utils.DateFormatter

// Created by Anton Yatsenko on 21.06.2019.
class MyListingsRecyclerAdapter(private val listingNavigationListener: ListingNavigationListener) :
    BaseRecyclerAdapter<ListItemMyListingBinding>(), ListingsUtilsDelegate by ListingUtilsDelegateImpl {
    private var data = mutableListOf<Listing>()
    override fun bindItem(holder: BaseViewHolder<ListItemMyListingBinding>, position: Int) {
        val context = holder.itemView.context
        val listing = data[position]
        holder.binding.model = listing
        if (listing.isDraft) {
            holder.binding.ivListingImage.setImageResource(R.drawable.ic_listing_placeholder)
        } else {
            holder.binding.ivListingImage.withGlide(listing.images?.firstOrNull { it.id == listing.primaryImageId }?.url)
        }
        holder.binding.tvListingType.text = getListingTypeSmall(context, listing)
        setListingMode(context, holder.binding, listing)
        holder.binding.viewsCount = listing.viewsCount
        holder.binding.favoritesCount = listing.favoritesCount
        holder.binding.contactedCount = listing.contactedCount
        holder.binding.tvExpired.visibleOrGone(listing.publishState == PublishState.EXPIRED)
        holder.binding.tvListingType.backgroundTintList = getColorForListingType(listing, context)
        holder.binding.tvListingType.setCompoundDrawablesWithIntrinsicBounds(
            getListingIconByType(listing.propertyType),
            0,
            0,
            0
        )
        holder.binding.ivMore.setOnClickListener {
            listingNavigationListener.onMenuClicked(listing)
        }
        holder.itemView.setOnClickListener {
            if (listing.isDraft) {
                listingNavigationListener.onDraftClicked(listing.id)
            } else {
                listingNavigationListener.onMyListingClicked(listing.id, listing.status)
            }
        }
        holder.binding.tvListingBath.setVisibilityForBath(listing)
        holder.binding.tvListingBeds.setVisibilityForBeds(listing)

        holder.binding.tvCreatedAt.text = if (listing.isDraft ||
            listing.publishState == PublishState.NOT_PUBLISHED ||
            listing.publishState == PublishState.EXPIRED) {
            DateFormatter.toFullDate(listing.createdAt)
        } else {
            DateFormatter.toFullDate(listing.adPurchasedAt)
        }

        if (listing.publishState == PublishState.PUBLISHED ||
            listing.publishState == PublishState.UNPUBLISHED) {
            listing.adExpiresAt?.let { adExpiresAt ->
                val daysLeft = DateFormatter.daysLeft(adExpiresAt)
                holder.binding.tvDaysLeft.text =
                    context.resources.getQuantityString(R.plurals.days_left_plural, daysLeft, daysLeft)
                holder.binding.tvDaysLeft.visible()
            }

            when (listing.adPlan) {
                PaymentPlanKey.PLUS -> {
                    holder.binding.ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_plus_yellow_24)
                    holder.binding.ivPlanIcon.visible()
                }
                PaymentPlanKey.PREMIUM -> {
                    holder.binding.ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_premium_blue_24)
                    holder.binding.ivPlanIcon.visible()
                }
                else -> holder.binding.ivPlanIcon.invisible()
            }
        } else {
            holder.binding.tvDaysLeft.invisible()
            holder.binding.ivPlanIcon.invisible()
        }
    }


    override fun getLayoutId(position: Int): Int = R.layout.list_item_my_listing

    override fun getItemCount(): Int = data.size

    fun add(newData: List<Listing>) {
        val diffUtils = DiffUtil.calculateDiff(ListingsDiffUtils(data, newData))
        data = newData.toMutableList()
        diffUtils.dispatchUpdatesTo(this)
    }

    fun remove(listing: Listing) {
        val index = data.indexOf(listing)
        if (index >= 0) {
            data.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    private fun setListingMode(
        context: Context,
        binding: ListItemMyListingBinding,
        listing: Listing
    ) {
        when {
            listing.isDraft -> {
                binding.tvListingStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvListingStatus.text = context.getString(R.string.draft)
                binding.tvListingStatus.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_gray))
            }
            listing.status == Constants.HIDDEN  || listing.publishState == PublishState.EXPIRED -> {
                if (listing.publishState == PublishState.NOT_PUBLISHED) {
                    binding.tvListingStatus.text = context.getString(R.string.not_published)
                } else {
                    binding.tvListingStatus.text = context.getString(R.string.unpublished)
                }
                binding.tvListingStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvListingStatus.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_gray))
            }
            else -> {
                binding.tvListingStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                binding.tvListingStatus.text = context.getString(R.string.published)
                binding.tvListingStatus.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent))
            }
        }
    }
}