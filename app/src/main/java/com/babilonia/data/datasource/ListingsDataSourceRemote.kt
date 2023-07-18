package com.babilonia.data.datasource

import com.babilonia.data.network.model.json.*
import com.babilonia.domain.model.enums.SortType
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

// Created by Anton Yatsenko on 07.06.2019.
interface ListingsDataSourceRemote {
    fun createListing(listingDto: ListingJson): Single<ListingJson>
    fun deleteListingById(id: Int): Completable
    fun getFacilitiesByType(dataType: String, propertyType: String): Single<List<FacilityJson>>
    fun getListingById(id: Long): Single<ListingJson>
    fun getMyListingById(id: Long): Single<ListingJson>
    fun uploadListingImage(path: String): Single<ImageJson>
    fun getMyListings(): Single<List<ListingJson>>
    fun updateListing(listingDto: ListingJson): Single<ListingJson>
    fun setListingAction(id: String, key: String): Completable
    fun deleteListingAction(id: String, key: String): Completable
    fun getFavouriteListings(): Single<List<ListingJson>>
    fun getListings(
        lat: Float,
        lon: Float,
        queryText: String?,
        placeId: String?,
        page: Int,
        pageSize: Int,
        radius: Int,
        sortType: SortType,
        filtersMap: Map<String, String>,
        facilities: List<Int>
    ): Single<List<ListingJson>>

    fun getListingsMetadata(
        lat: Float,
        lon: Float,
        radius: Int,
        filtersMap: Map<String, String>,
        facilities: List<Int>
    ): Observable<ListingsMetadataJson>

    fun getListingsPriceRange(
        lat: Float,
        lon: Float,
        radius: Int,
        filtersMap: Map<String, String>,
        facilities: List<Int>
    ): Single<List<PriceRangeItemJson>>

    fun getRecentSearches(): Single<List<RecentSearchJson>>

    fun getTopListings(lat: Float, lon: Float, radius: Int): Single<List<ListingJson>>
}