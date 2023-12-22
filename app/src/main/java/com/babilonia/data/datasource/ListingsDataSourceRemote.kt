package com.babilonia.data.datasource

import com.babilonia.data.network.model.BaseResponse
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
    fun uploadListingImage(path: String): Single<List<ImageJson>>
    fun getMyListings(state: String): Single<List<ListingJson>>
    fun updateListing(listingDto: ListingJson): Single<ListingJson>
    fun setListingAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable
    fun setContactAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable
    fun deleteListingAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable
    fun getFavouriteListings(): Single<List<ListingJson>>
    fun getListings(
        lat: Float?,
        lon: Float?,
        queryText: String?,
        placeId: String?,
        page: Int,
        pageSize: Int,
        radius: Int?,
        sortType: SortType,
        filtersMap: Map<String, String>,
        facilities: List<Int>,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Single<List<ListingJson>>

    fun getListingsPage(
        page: Int,
        pageSize: Int,
        sortType: SortType,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Single<BaseResponse<DataCurrentPageJson>>

    fun getListingsMetadata(
        lat: Float?,
        lon: Float?,
        radius: Int?,
        filtersMap: Map<String, String>,
        facilities: List<Int>,
        department: String?,
        province: String?,
        district: String?,
        address: String?
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

    fun getDataLocationSearched(address: String, page: Int, perPage: Int): Single<DataLocationJson>

    fun getListUbigeo(type: String, department: String?, province: String?): Single<List<String>>
}