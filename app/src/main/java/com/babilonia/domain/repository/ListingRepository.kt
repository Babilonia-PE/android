package com.babilonia.domain.repository

import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.GetAdPlansResponse
import com.babilonia.data.network.model.json.DataCurrentPageJson
import com.babilonia.domain.model.*
import com.babilonia.domain.model.enums.FacilityDataType
import com.babilonia.domain.model.enums.SortType
import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.presentation.flow.main.search.model.ListingsMetadata
import com.babilonia.presentation.view.priceview.BarEntry
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

// Created by Anton Yatsenko on 07.06.2019.
interface ListingRepository {
    fun getFacilitiesByType(dataType: FacilityDataType, propertyType: String): Flowable<List<Facility>>
    fun createListing(listingDto: Listing): Single<Listing>
    fun uploadListingImage(path: String): Single<List<ListingImage>>
    fun draftListing(listingDto: Listing): Single<Listing>
    fun getListingById(id: Long): Single<Listing>
    fun getMyListingById(id: Long): Single<Listing>
    fun getLocalListing(id: Long): Single<Listing>
    fun getDraftListingById(id: Long): Single<Listing>
    fun getMyListings(state: String): Flowable<List<Listing>>
    fun updateListing(listingDto: Listing): Single<Listing>
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
    fun getFavouriteListings(): Flowable<List<Listing>>
    fun deleteDraftById(id: Long): Completable
    fun getListings(
        lat: Float?,
        lon: Float?,
        queryText: String?,
        placeId: String?,
        page: Int,
        pageSize: Int,
        radius: Int?,
        sortType: SortType,
        filters: List<Filter>,
        facilities: List<Facility>,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Single<List<Listing>>

    fun getListingsMetadata(
        lat: Float?,
        lon: Float?,
        radius: Int?,
        filters: List<Filter>,
        facilities: List<Facility>,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Observable<ListingsMetadata>

    fun getListingsPriceRange(
        lat: Float,
        lon: Float,
        radius: Int,
        filters: List<Filter>,
        facilities: List<Facility>
    ): Single<List<BarEntry>>

    fun getRecentSearch(): Single<List<RecentSearch>>

    fun getTopListings(lat: Float, lon: Float, radius: Int): Single<List<Listing>>

    fun getDataLocationSearched(address: String, page: Int, perPage:Int): Single<DataLocation>

    fun getListUbigeo(type: String, department: String?, province: String?): Single<List<String>>

    fun getListingsPage(
        page: Int,
        pageSize: Int,
        sortType: SortType,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Single<DataCurrentPage>

}