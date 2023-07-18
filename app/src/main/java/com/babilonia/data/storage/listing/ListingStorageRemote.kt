package com.babilonia.data.storage.listing

import android.content.Context
import android.webkit.MimeTypeMap
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.ListingsDataSourceRemote
import com.babilonia.data.network.model.CreateListringRequest
import com.babilonia.data.network.model.json.*
import com.babilonia.data.network.service.ListingsService
import com.babilonia.domain.model.enums.SortType
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

// Created by Anton Yatsenko on 07.06.2019.
class ListingStorageRemote @Inject constructor(
    private val listingsService: ListingsService,
    private val context: Context
) :
    ListingsDataSourceRemote {
    override fun getListingsPriceRange(
        lat: Float,
        lon: Float,
        radius: Int,
        filtersMap: Map<String, String>,
        facilities: List<Int>
    ): Single<List<PriceRangeItemJson>> {
        return listingsService.getPriceRange(lat, lon, radius, filtersMap, facilities)
            .map { it.data.records }
    }

    override fun getListingsMetadata(
        lat: Float,
        lon: Float,
        radius: Int,
        filtersMap: Map<String, String>,
        facilities: List<Int>
    ): Observable<ListingsMetadataJson> {
        return listingsService.getListingsMetadata(lat, lon, radius, filtersMap, facilities)
            .map { it.data }
    }

    override fun getListings(
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
    ): Single<List<ListingJson>> {
        return listingsService.getListings(
            lat,
            lon,
            if (placeId.isNullOrEmpty()) null else queryText,
            placeId,
            page,
            limit = pageSize,
            radius = radius,
            sort = sortType.value,
            order = sortType.order,
            filters = filtersMap,
            facilities = facilities
        ).map { it.data.records }
    }

    override fun getFavouriteListings(): Single<List<ListingJson>> {
        return listingsService.getFavouriteListings().map { it.data.records }
    }

    override fun setListingAction(id: String, key: String): Completable {

        return listingsService.setListingAction(id, key)
    }

    override fun deleteListingAction(id: String, key: String): Completable {

        return listingsService.deleteListingAction(id, key)
    }

    override fun updateListing(listingDto: ListingJson): Single<ListingJson> {
        return listingsService.updateListingById(CreateListringRequest(listingDto), listingDto.id)
            .map { it.data }
    }

    override fun getMyListings(): Single<List<ListingJson>> {
        return listingsService.getMyListing().map { it.data.records }
    }

    override fun deleteListingById(id: Int): Completable {
        throw NotImplementedError()
    }

    override fun getListingById(id: Long): Single<ListingJson> {
        return listingsService.getListing(id.toString()).map { it.data }
    }

    override fun getMyListingById(id: Long): Single<ListingJson> {
        return listingsService.getMyListing(id.toString()).map { it.data }
    }

    override fun createListing(listingDto: ListingJson): Single<ListingJson> {
        return listingsService.createListing(CreateListringRequest(listingDto))
            .map { it.data }
    }

    override fun getFacilitiesByType(dataType: String, propertyType: String): Single<List<FacilityJson>> {
        return listingsService.getFacilitiesByType(
            dataType,
            if (propertyType == Constants.ALL_FACILITIES) {
                null
            } else {
                propertyType
            }
        ).map { it.data.records }
    }

    override fun uploadListingImage(path: String): Single<ImageJson> {
        val file = File(path)
        val mediaType = MediaType.parse(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(
                    file.path
                )
            ) ?: EmptyConstants.EMPTY_STRING
        )
        val body = RequestBody.create(
            mediaType, file
        )
        val fileName = "${System.currentTimeMillis()}.jpeg"
        val multipart = MultipartBody.Part.createFormData("data[photo]",fileName, body)
        return listingsService.uploadListingImage(multipart).map {
            it.data
        }
    }

    override fun getRecentSearches(): Single<List<RecentSearchJson>> {
        return listingsService.getRecentSearches().map { it.data.records }
    }

    override fun getTopListings(lat: Float, lon: Float, radius: Int): Single<List<ListingJson>> {
        return listingsService.getTopListing(lat, lon, radius).map { it.data.records }
    }
}