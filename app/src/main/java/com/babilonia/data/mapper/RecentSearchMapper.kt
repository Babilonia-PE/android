package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.data.network.model.json.RecentSearchJson
import com.babilonia.domain.model.RecentSearch
import javax.inject.Inject

class RecentSearchMapper @Inject constructor() {

    fun mapRemoteToDomain(recentSearches: List<RecentSearchJson>): List<RecentSearch> {
        return recentSearches.map {
            RecentSearch(
                it.queryText ?: EmptyConstants.EMPTY_STRING,
                it.googlePlaceId ?: EmptyConstants.EMPTY_STRING
            )
        }
    }
}