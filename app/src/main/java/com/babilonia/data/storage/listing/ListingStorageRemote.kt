package com.babilonia.data.storage.listing

import android.content.Context
import android.webkit.MimeTypeMap
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.ListingsDataSourceRemote
import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.CreateListingRequest
import com.babilonia.data.network.model.json.*
import com.babilonia.data.network.service.ListingsService
import com.babilonia.data.network.service.NewListingsService
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
    private val newListingsService: NewListingsService,
    private val context: Context
) : ListingsDataSourceRemote {

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
        lat: Float?,
        lon: Float?,
        radius: Int?,
        filtersMap: Map<String, String>,
        facilities: List<Int>,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Observable<ListingsMetadataJson> {
        return newListingsService.getListingsMetadata(lat, lon, radius, filtersMap, facilities,
            if(department.isNullOrBlank()) null else department,
            if(province.isNullOrBlank()) null else province,
            if(district.isNullOrBlank()) null else district,
            if(address.isNullOrBlank()) null else address).map { it.data }
    }

    override fun getListings(
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
    ): Single<List<ListingJson>> {
        return newListingsService.getListings(
            lat,
            lon,
            if (placeId.isNullOrEmpty()) null else queryText,
            placeId,
            page,
            limit = pageSize,
            radius,
            sort = sortType.value,
            order = sortType.order,
            filters = filtersMap,
            facilities = facilities,
            department = if(department.isNullOrBlank()) null else department,
            province = if(province.isNullOrBlank()) null else province,
            district = if(district.isNullOrBlank()) null else district,
            address = if(address.isNullOrBlank()) null else address
        ).map { it.data.records }
    }

    override fun getFavouriteListings(): Single<List<ListingJson>> {
        return newListingsService.getFavouriteListings().map { it.data.records.listings }
    }

    override fun setListingAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable {
        return newListingsService.setListingAction(
            UserActionRequest(
                id,
                key,
                ipAddress,
                userAgent,
                signProvider
            )
        )
    }

    override fun setContactAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable {
        return newListingsService.setContactAction(
            UserActionRequest(
                id,
                key,
                ipAddress,
                userAgent,
                signProvider
            )
        )
    }

    override fun deleteListingAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable {
        return newListingsService.deleteListingAction(
            UserActionRequest(
                id,
                key,
                ipAddress,
                userAgent,
                signProvider
            )
        )
    }

    override fun updateListing(listingDto: ListingJson): Single<ListingJson> {  //para publicar y despublciar
        val request = parseToRequestListingJson(listingDto)
        return newListingsService.updateListingById(request)
            .map { it.data }
    }

    private fun parseToRequestListingJson(listingDto: ListingJson): CreateListingRequest {
        val request = CreateListingRequest()
        request.id = listingDto.id
        request.ids = mutableListOf(listingDto.id.toInt())
        request.reason = listingDto.reason
        request.priceFinal = listingDto.priceFinal
        request.listingType = listingDto.listingType
        request.propertyType = listingDto.propertyType
        request.price = listingDto.price
        request.description = listingDto.description
        request.bathroomsCount = listingDto.bathroomsCount
        request.bedroomsCount = listingDto.bedroomsCount
        request.totalFloorsCount = listingDto.totalFloorsCount
        request.floorNumber = listingDto.floorNumber
        request.parkingSlotsCount = listingDto.parkingSlotsCount
        request.parkingForVisitors = listingDto.parkingForVisitors
        request.area = listingDto.area
        request.builtArea = listingDto.builtArea
        request.petFriendly = listingDto.petFriendly
        request.status = listingDto.status
        request.facilityIds = listingDto.facilityIds
        request.advancedDetailsIds = listingDto.advancedDetailsIds
        request.locationAttributes = listingDto.locationAttributes
        request.yearOfConstruction = listingDto.yearOfConstruction
        request.imageIds = listingDto.imageIds
        request.primaryImageId = listingDto.primaryImageId
        request.user = listingDto.user
        request.contactName = listingDto.contacts.first().contactName
        request.contactEmail = listingDto.contacts.first().contactEmail
        request.contactPhone = listingDto.contacts.first().contactPhone
        request.facilities = listingDto.facilities
        request.advancedDetails = listingDto.advancedDetails
        request.images = listingDto.images
        request.favourited = listingDto.favourited
        request.viewsCount = listingDto.viewsCount
        request.contactedCount = listingDto.contactedCount
        request.favoritesCount = listingDto.favoritesCount
        request.adPlan = listingDto.adPlan
        request.publishState = listingDto.publishState
        request.publisherRole = listingDto.publisherRole
        request.createdAt = listingDto.createdAt
        request.updatedAt = listingDto.updatedAt
        request.adExpiresAt = listingDto.adExpiresAt
        request.adPurchasedAt = listingDto.adPurchasedAt
        return request
    }

    override fun getMyListings(state: String): Single<List<ListingJson>> {
        return newListingsService.getMyListing(page = 1, state = state).map {
            it.data.records
        }
    }

    override fun deleteListingById(id: Int): Completable {
        throw NotImplementedError()
    }

    override fun getListingById(id: Long): Single<ListingJson> {
        return newListingsService.getListing(id.toString()).map { it.data }
    }

    override fun getMyListingById(id: Long): Single<ListingJson> {
        return newListingsService.getMyListing(id.toString()).map { it.data } // después de pagar
    }

    override fun createListing(listingDto: ListingJson): Single<ListingJson> { // cuando se crea una publicacion
        val request = parseToRequestListingJson(listingDto)
        return newListingsService.createListing(request)
            .map { it.data }
    }

    override fun getFacilitiesByType(dataType: String, propertyType: String): Single<List<FacilityJson>> {
        return newListingsService.getFacilitiesByType(
            dataType,
            if (propertyType == Constants.ALL_FACILITIES) {
                null
            } else {
                propertyType
            }
        ).map { it.data.records }
    }

    override fun uploadListingImage(path: String): Single<List<ImageJson>> {
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
        val imagePart = MultipartBody.Part.createFormData("photo[]", file.name, body)
        val sourcePart = MultipartBody.Part.createFormData("source", "android")
        val typePart = MultipartBody.Part.createFormData("type", "listing")
        return newListingsService.uploadListingImage(imagePart, sourcePart, typePart).map {
            it.data.ids
        }
    }

    override fun getRecentSearches(): Single<List<RecentSearchJson>> {
        return newListingsService.getRecentSearches().map { it.data.records }
    }

    override fun getTopListings(lat: Float, lon: Float, radius: Int): Single<List<ListingJson>> {
        return newListingsService.getTopListing(lat, lon, radius).map { it.data.records }
    }

    override fun getDataLocationSearched(address: String, page: Int, perPage: Int): Single<DataLocationJson> {
        return newListingsService.getDataLocationSearched(address, page, perPage).map { it.data }
    }

    override fun getListingsPage(
        page: Int,
        pageSize: Int,
        sortType: SortType,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Single<BaseResponse<DataCurrentPageJson>> {
        return newListingsService.getListingsPage(
        page,
        limit = pageSize,
        sort = sortType.value,
        order = sortType.order,
        department = if(department.isNullOrBlank()) null else department,
        province = if(province.isNullOrBlank()) null else province,
        district = if(district.isNullOrBlank()) null else district,
        address = if(address.isNullOrBlank()) null else address
        ).map { it }
    }

    override fun getListUbigeo(type: String, department: String?, province: String?): Single<List<String>> {
        return newListingsService.getListUbigeo(type, department, province).map { it.data.ubigeos }
    }
}