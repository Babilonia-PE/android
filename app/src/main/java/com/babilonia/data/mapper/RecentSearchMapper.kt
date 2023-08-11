package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.data.db.model.LocationDto
import com.babilonia.data.network.model.json.LocationJson
import com.babilonia.data.network.model.json.RecentSearchJson
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.RecentSearch
import javax.inject.Inject

class RecentSearchMapper @Inject constructor() {

    fun mapRemoteToDomain(recentSearches: List<RecentSearchJson>): List<RecentSearch> {
        val mRecentSearches = mutableListOf<RecentSearchJson>()
        for (recent in recentSearches){
            if(recent.location!=null)
                mRecentSearches.add(recent)
        }

        return mRecentSearches.map {
            RecentSearch(
                it.queryText ?: EmptyConstants.EMPTY_STRING,
                it.googlePlaceId ?: EmptyConstants.EMPTY_STRING,
                mapRemoteToDomainLocation(it.location)
            )
        }
    }

    private fun mapRemoteToDomainLocation(from: LocationJson?): Location {
        return Location().apply {
            latitude = from?.latitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
            longitude = from?.longitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
            address = from?.address?.trim()?: EmptyConstants.EMPTY_STRING
            department = from?.department?.trim()?: EmptyConstants.EMPTY_STRING
            district = from?.district?.trim()?: EmptyConstants.EMPTY_STRING
            province = from?.province?.trim()?: EmptyConstants.EMPTY_STRING
            zipCode = from?.zipCode?.trim()?: EmptyConstants.EMPTY_STRING
            country = from?.country?.trim()?: EmptyConstants.EMPTY_STRING
        }
    }
}