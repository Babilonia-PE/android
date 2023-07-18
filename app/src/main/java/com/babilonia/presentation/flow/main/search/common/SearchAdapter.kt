package com.babilonia.presentation.flow.main.search.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.Place
import com.babilonia.domain.model.RecentSearch


// Created by Anton Yatsenko on 17.07.2019.
private const val MY_LOCATION = 0
private const val PLACE = 1
private const val PLACE_NOT_FOUND = 2
private const val HISTORY_HEADER = 3
private const val HISTORY_ITEM = 4

private const val MODE_PLACES = 0
private const val MODE_LOC_NOT_FOUND = 1
private const val MODE_HISTORY = 2

class SearchAdapter : BaseAdapter(), Filterable {

    private val places = arrayListOf<Place>()
    private val history = arrayListOf<RecentSearch>()
    private var notFoundLocationName = EmptyConstants.EMPTY_STRING

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint == null || constraint.count() < 3) {
                    filterResults.values = history
                    filterResults.count = history.size
                } else {
                    filterResults.values = places
                    filterResults.count = places.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        return when (getItemViewType(position)) {
            MY_LOCATION -> LayoutInflater.from(parent?.context).inflate(R.layout.list_item_my_location, parent, false)
            PLACE_NOT_FOUND -> LayoutInflater.from(parent?.context).inflate(R.layout.list_item_location_not_found, parent, false).apply {
                findViewById<TextView>(R.id.tvPlace)?.text = notFoundLocationName
                isClickable = false
            }
            PLACE -> LayoutInflater.from(parent?.context).inflate(R.layout.list_item_search_sugest, parent, false).apply {
                findViewById<TextView>(R.id.tvPlace)?.text = (places[position - 1] as Place).title
            }
            HISTORY_HEADER -> LayoutInflater.from(parent?.context).inflate(R.layout.list_header_search_history, parent, false).apply {
                isClickable = false
            }
            else -> LayoutInflater.from(parent?.context).inflate(R.layout.list_item_search_sugest, parent, false).apply {
                findViewById<TextView>(R.id.tvPlace)?.text = getHistoryText(position)
            }
        }
    }

    override fun getItem(position: Int): Any? {
        if (position == 0) return EmptyConstants.EMPTY_STRING // 'My location' item

        return when (getMode()) {
            MODE_PLACES -> places[position - 1]
            MODE_LOC_NOT_FOUND -> {
                when (position) {
                    1 -> null // 'Location not found' item
                    2 -> null // 'search history' header
                    else -> history[position - 3]
                }
            }
            else -> {
                when (position) {
                    1 -> null // 'search history' header
                    else -> history[position - 2]
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return when (getMode()) {
            MODE_PLACES -> places.size + 1 // +1 for 'My location' item
            MODE_LOC_NOT_FOUND -> {
                // +2: 1 for 'My location' item and 1 for 'not found' item
                getHistoryWithHeaderItemCount() + 2
            }
            else -> {
                // +1 for 'My location' item
                getHistoryWithHeaderItemCount() + 1
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return MY_LOCATION
        return when (getMode()) {
            MODE_PLACES -> PLACE
            MODE_LOC_NOT_FOUND -> {
                when (position) {
                    1 -> PLACE_NOT_FOUND
                    2 -> HISTORY_HEADER
                    else -> HISTORY_ITEM
                }
            }
            else -> {
                when (position) {
                    1 -> HISTORY_HEADER
                    else -> HISTORY_ITEM
                }
            }
        }
    }

    fun setPlaces(newPlaces: List<Place>) {
        notFoundLocationName = EmptyConstants.EMPTY_STRING
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
    }

    fun clearPlaces() {
        if (places.isNotEmpty()) {
            places.clear()
            notifyDataSetChanged()
        }
    }

    fun setRecentSearches(newRecentSearches: List<RecentSearch>) {
        history.clear()
        history.addAll(newRecentSearches)
        notifyDataSetChanged()
    }

    fun setNotFoundPlace(placeName: String) {
        notFoundLocationName = placeName
        places.clear()
        notifyDataSetChanged()
    }

    fun clearNotFoundPlace() {
        notFoundLocationName = EmptyConstants.EMPTY_STRING
        notifyDataSetChanged()
    }

    private fun getMode(): Int {
        return when {
            places.isNotEmpty() -> MODE_PLACES
            notFoundLocationName.isNotEmpty() -> MODE_LOC_NOT_FOUND
            else -> MODE_HISTORY
        }
    }

    private fun getHistoryText(position: Int): String {
        var realPosition = position - 2 // 1 for 'My location' and 1 for 'History' header
        if (notFoundLocationName.isNotEmpty()) {
            realPosition -= 1
        }
        return history[realPosition].queryText
    }

    /**
     * returns number of items required for history and its header
     */
    private fun getHistoryWithHeaderItemCount(): Int {
        return if (history.isEmpty()) {
            0
        } else {
            history.size + 1 // +1 for 'History' header
        }
    }
}