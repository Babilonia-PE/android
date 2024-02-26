package com.babilonia.presentation.flow.main.publish.mylistings.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.PublishState
import com.babilonia.presentation.extension.visibleOrGone
import kotlinx.android.synthetic.main.item_my_listing_page.view.*

class MyListingsPagerAdapter(
    private val listingNavigationListener: ListingNavigationListener,
    private val tabNames: List<String>
) : PagerAdapter() {

    private lateinit var publishedTab: ViewGroup
    private lateinit var notPublishedTab: ViewGroup

    private lateinit var publishedAdapter: MyListingsRecyclerAdapter
    private lateinit var notPublishedAdapter: MyListingsRecyclerAdapter

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val layout = inflater.inflate(R.layout.item_my_listing_page, container, false) as ViewGroup

        val adapter = MyListingsRecyclerAdapter(listingNavigationListener)

        layout.findViewById<RecyclerView>(R.id.rvMyListings)?.let { recycler ->
            recycler.layoutManager = LinearLayoutManager(container.context)
            recycler.adapter = adapter
        }

        when (position) {
            0 -> {
                publishedTab = layout
                publishedAdapter = adapter
                layout.tvEmptyTitle.text =
                    container.context.getString(R.string.my_listings_empty_published)
                layout.tvEmptyBody.text =
                    container.context.getString(R.string.my_listings_empty_published_body)
            }
            else -> {
                notPublishedTab = layout
                notPublishedAdapter = adapter
                layout.tvEmptyTitle.text =
                    container.context.getString(R.string.my_listings_empty_not_published)
            }
        }

        container.addView(layout)
        return layout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = tabNames.size

    fun add(items: List<Listing>) {
        val notPublished = items.filter { it.isDraft ||
                it.status == Constants.HIDDEN || it.publishState == PublishState.EXPIRED }.distinctBy { it.id }

        val published = items.filter { it.publishState == PublishState.PUBLISHED}.distinctBy { it.id }

        publishedAdapter.add(published)
        publishedTab.emptyGroup.visibleOrGone(published.isEmpty())

        notPublishedAdapter.add(notPublished)
        notPublishedTab.emptyGroup.visibleOrGone(notPublished.isEmpty())
    }

    fun remove(listing: Listing) {
        publishedAdapter.remove(listing)
        notPublishedAdapter.remove(listing)
    }
}