package com.babilonia.presentation.flow.main.common

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.ListItemListingPreviewBinding
import com.babilonia.databinding.VhSearchFilterBinding
import com.babilonia.databinding.VhTopListingsBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.presentation.base.BaseRecyclerAdapter
import com.babilonia.presentation.base.BaseViewHolder
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.listing.common.ListingImagesPagerAdapter
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl
import com.babilonia.presentation.flow.main.search.map.common.ListingsUtilsDelegate
import com.babilonia.presentation.utils.RecyclerViewRealItemCounter

class ListingPreviewRecyclerAdapter(
    private val listener: ListingActionsListener,
    private val onFilterClickListener: (() -> Unit)? = null
) : BaseRecyclerAdapter<ViewDataBinding>(),
    ListingsUtilsDelegate by ListingUtilsDelegateImpl,
    RecyclerViewRealItemCounter {

    private var data = mutableListOf<Listing>()
    private var topListings = mutableListOf<Listing>()
    private var currentUserId = EmptyConstants.EMPTY_LONG

    private var isTopListingsVisible = false
    private var isFilterVisible = false

    private var filterText: String = EmptyConstants.EMPTY_STRING

    private var topListingsViewHolder: TopListingsViewHolder? = null
    var topListingsRestoredState: Parcelable? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewDataBinding> {
        return when (viewType) {
            TYPE_TOP_LISTINGS -> {
                if (topListingsViewHolder == null) {
                    topListingsViewHolder = createTopListingsViewHolder(parent)
                }
                topListingsViewHolder!!
            }
            TYPE_FILTER -> {
                val viewHolder = FilterViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.vh_search_filter,
                        parent,
                        false),
                    onFilterClickListener
                )
                viewHolder.binding.addOnRebindCallback(preBindCallback)
                viewHolder
            }
            else -> super.onCreateViewHolder(parent, R.layout.list_item_listing_preview)
        }
    }

    override fun bindItem(holder: BaseViewHolder<ViewDataBinding>, position: Int) {
        when (getItemViewType(position)) {
            TYPE_TOP_LISTINGS ->
                bindTopListingsViewHolder(holder as TopListingsViewHolder)
            TYPE_FILTER -> bindFilterViewHolder(holder as FilterViewHolder)
            else -> bindItemViewHolder(holder as BaseViewHolder<ListItemListingPreviewBinding>, position)
        }
    }

    fun saveTopListingsState() = topListingsViewHolder?.saveState()

    private fun getDataItemPosition(adapterPosition: Int): Int {
        var itemPosition = adapterPosition
        if (isFilterVisible) {
            itemPosition--
        }
        if (isTopListingsVisible) {
            itemPosition--
        }
        return itemPosition
    }

    private fun bindTopListingsViewHolder(holder: TopListingsViewHolder) {
        holder.setCurrentUserId(currentUserId)
        holder.setItems(topListings)
        topListingsRestoredState?.let {
            holder.restoreState(it)
            topListingsRestoredState = null
        }
    }

    private fun bindFilterViewHolder(holder: FilterViewHolder) {
        holder.bind(filterText)
    }

    private fun bindItemViewHolder(holder: BaseViewHolder<ListItemListingPreviewBinding>, adapterPosition: Int) {
        val position = getDataItemPosition(adapterPosition)
        if (position >= data.size) return

        val context = holder.itemView.context
        val listing = data[position]
        with (holder.binding) {
            model = listing
            ivCollaps.invisible()
            clListingTypeContainer.text = getListingTypeSmall(context, listing)
            clListingTypeContainer.backgroundTintList = getColorForListingType(listing, context)
            clListingTypeContainer.setCompoundDrawablesWithIntrinsicBounds(
                getListingIconByType(listing.propertyType),
                0,
                0,
                0
            )
            vpImages.adapter =
                ListingImagesPagerAdapter(listing.images, 16) {
                    listing.id?.let { id -> listener.onPreviewClicked(id) }
                }
            pagerIndicator.attachToPager(vpImages)
            tvImagesCount.text = listing.images?.size.toString()
            tvListingBath.setVisibilityForBath(listing)
            tvListingBeds.setVisibilityForBeds(listing)
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

            holder.itemView.setOnClickListener {
                listing.id?.let { id -> listener.onPreviewClicked(id) }
            }
        }
    }

    override fun getLayoutId(position: Int): Int {
        return when (getItemViewType(position)) {
            TYPE_TOP_LISTINGS -> R.layout.vh_top_listings
            TYPE_FILTER -> R.layout.vh_search_filter
            else -> R.layout.list_item_listing_preview
        }
    }

    override fun getItemCount(): Int = data.size +
            if (isFilterVisible) { FILTER_SIZE } else { 0 } +
            if (isTopListingsVisible) { TOP_LISTINGS_SIZE } else { 0 }

    override fun getItemViewType(position: Int): Int {
        return if (isTopListingsVisible) {
            when (position) {
                0 -> TYPE_TOP_LISTINGS
                1 -> { if (isFilterVisible) TYPE_FILTER else TYPE_ITEM }
                else -> TYPE_ITEM
            }
        } else {
            if (position == 0 && isFilterVisible) {
                TYPE_FILTER
            } else {
                TYPE_ITEM
            }
        }
    }

    override fun getRealItemCount(): Int = data.size

    fun addItems(newData: List<Listing>, shouldResetList: Boolean = false) {
        if (shouldResetList) {
            data.clear()
            notifyDataSetChanged()
        }
        data = newData.toMutableList()
        notifyDataSetChanged()
    }

    fun addTopListings(newData: List<Listing>) {
        topListings.clear()
        topListings.addAll(newData)
        topListingsViewHolder?.setItems(topListings)
        isTopListingsVisible = true
        notifyTopListingsChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    fun setCurrentUserId(id: Long) {
        currentUserId = id
    }

    fun setFilterText(text: String) {
        filterText = text
        notifyFilterChanged()
    }

    fun setFilterVisible(newState: Boolean) {
        if (isFilterVisible != newState) {
            isFilterVisible = newState
            val filterPosition = if (isTopListingsVisible) 1 else 0
            if (isFilterVisible) {
                notifyItemInserted(filterPosition)
            } else {
                notifyItemRemoved(filterPosition)
            }
        }
    }

    fun setTopListingsVisible(isVisible: Boolean) {
        var newState = isVisible

        if (newState && topListings.isEmpty()) {
            newState = false
        }

        if (isTopListingsVisible != newState) {
            isTopListingsVisible = newState
            if (isTopListingsVisible) {
                notifyItemInserted(0)
            } else {
                notifyItemRemoved(0)
            }
        }
    }

    fun isTopListingsVisible() = isTopListingsVisible

    private fun notifyFilterChanged() {
        if (isFilterVisible) {
            // filter viewHolder goes after top listings if top listings are visible
            if (isTopListingsVisible) {
                notifyItemChanged(1)
            } else {
                notifyItemChanged(0)
            }
        }
    }

    private fun notifyTopListingsChanged() {
        if (isTopListingsVisible) {
            notifyItemChanged(0) // top listings are always on 0 position
        }
    }

    private fun createTopListingsViewHolder(parent: ViewGroup): TopListingsViewHolder {
        val viewHolder = TopListingsViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.vh_top_listings,
                parent,
                false),
            listener)
        viewHolder.binding.addOnRebindCallback(preBindCallback)
        return viewHolder
    }

    class TopListingsViewHolder(
        binding: VhTopListingsBinding,
        listener: ListingActionsListener
    ) : BaseViewHolder<VhTopListingsBinding>(binding) {

        private val adapter: TopListingsAdapter = TopListingsAdapter(listener)

        init {
            val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvTopListings.adapter = adapter
            binding.rvTopListings.layoutManager = layoutManager
        }

        fun setItems(items: List<Listing>) {
            adapter.setItems(items)
        }

        fun setCurrentUserId(id: Long) {
            adapter.setCurrentUserId(id)
        }

        fun saveState() = binding.rvTopListings.layoutManager?.onSaveInstanceState()

        fun restoreState(state: Parcelable) {
            binding.rvTopListings.layoutManager?.onRestoreInstanceState(state)
        }
    }

    class FilterViewHolder(binding: VhSearchFilterBinding, onClickAction: (() -> Unit)?) :
        BaseViewHolder<VhSearchFilterBinding>(binding) {

        init {
            itemView.setOnClickListener { onClickAction?.invoke() }
        }

        fun bind(newText: String) {
            binding.tvSortBy.text = newText
        }
    }

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_TOP_LISTINGS = 1
        private const val TYPE_FILTER = 2

        private const val TOP_LISTINGS_SIZE = 1
        private const val FILTER_SIZE = 1
    }
}