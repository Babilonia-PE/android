package com.babilonia.data.network.service

import com.babilonia.Constants
import com.babilonia.data.network.model.*
import com.babilonia.data.network.model.json.ImageJson
import com.babilonia.data.network.model.json.ListingJson
import com.babilonia.data.network.model.json.ListingsMetadataJson
import com.babilonia.domain.model.enums.SortType
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*

// Created by Anton Yatsenko on 07.06.2019.
interface ListingsService {

    @POST("api/users/me/listings")
    fun createListing(@Body data: CreateListringRequest): Single<BaseResponse<ListingJson>>

    @DELETE("api/listings/{listing_id}/user_actions/{key}")
    fun deleteListingAction(@Path("listing_id") id: String, @Path("key") key: String): Completable

    @GET("api/listing_amenities/{dataType}")
    fun getFacilitiesByType(
        @Path("dataType") dataType: String,
        @Query("data[property_type]") type: String?): Single<BaseResponse<GetFacilitiesResponse>>

    @GET("api/users/me/favourite_listings")
    fun getFavouriteListings(@Query("per_page") perPage: Int = Constants.PER_PAGE_MAX): Single<BaseResponse<GetMyListingsResponse>>

    @GET("api/listings")
    fun getListings(
        @Query("area[latitude]") lat: Float,
        @Query("area[longitude]") lon: Float,
        @Query("query_string") queryText: String?,
        @Query("google_places_location_id") placeId: String?,
        @Query("page") page: Int,
        @Query("per_page") limit: Int = Constants.PER_PAGE,
        @Query("area[radius]") radius: Int = Constants.MAX_RADIUS,
        @Query("sort") sort: String = SortType.NEAREST.value,
        @Query("direction") order: String = SortType.NEAREST.order,
        @QueryMap filters: Map<String, String> = mutableMapOf(),
        @Query("facility_ids[]") facilities: List<Int>? = null
    ): Single<BaseResponse<GetMyListingsResponse>>

    @GET("api/listings")
    fun getArListings(
        @Query("area[latitude]") lat: Float,
        @Query("area[longitude]") lon: Float,
        @Query("area[radius]") radius: Int = Constants.MAX_RADIUS
    ): Single<BaseResponse<GetMyListingsResponse>>

    @GET("api/listings/search_metadata")
    fun getListingsMetadata(
        @Query("area[latitude]") lat: Float,
        @Query("area[longitude]") lon: Float,
        @Query("area[radius]") radius: Int = Constants.MAX_RADIUS,
        @QueryMap filters: Map<String, String> = mutableMapOf(),
        @Query("facility_ids[]") facilities: List<Int>? = null
    ): Observable<BaseResponse<ListingsMetadataJson>>

    @GET("api/listings/prices_histogram_slots")
    fun getPriceRange(
        @Query("area[latitude]") lat: Float,
        @Query("area[longitude]") lon: Float,
        @Query("area[radius]") radius: Int = Constants.MAX_RADIUS,
        @QueryMap filters: Map<String, String> = mutableMapOf(),
        @Query("facility_ids[]") facilities: List<Int>? = null
    ): Single<BaseResponse<PriceRangeResponse>>

    @GET("api/users/me/listings")
    fun getMyListing(@Query("per_page") perPage: Int = Constants.PER_PAGE_MAX): Single<BaseResponse<GetMyListingsResponse>>

    @POST("api/listings/{listing_id}/user_actions/{key}")
    fun setListingAction(@Path("listing_id") id: String, @Path("key") key: String): Completable

    @PUT("api/users/me/listings/{id}")
    fun updateListingById(@Body listingRequest: CreateListringRequest, @Path("id") id: Long): Single<BaseResponse<ListingJson>>

    @Multipart
    @POST("api/users/me/images")
    fun uploadListingImage(@Part image: MultipartBody.Part): Single<BaseResponse<ImageJson>>

    @GET("api/users/me/recent_searches")
    fun getRecentSearches(): Single<BaseResponse<GetRecentSearchesResponse>>

    @GET("api/listings/{id}")
    fun getListing(@Path("id") listingId: String) : Single<BaseResponse<ListingJson>>

    @GET("api/users/me/listings/{id}")
    fun getMyListing(@Path("id") listingId: String) : Single<BaseResponse<ListingJson>>

    @GET("api/last_viewed_listings")
    fun getTopListing(
        @Query("area[latitude]") lat: Float,
        @Query("area[longitude]") lon: Float,
        @Query("area[radius]") radius: Int = Constants.MAX_RADIUS
    ) : Single<BaseResponse<GetMyListingsResponse>>
}