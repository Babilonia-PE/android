package com.babilonia.data.network.service

import com.babilonia.Constants
import com.babilonia.data.network.model.*
import com.babilonia.data.network.model.json.*
import com.babilonia.domain.model.enums.SortType
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*

interface NewListingsService {
    @POST("me/validation_data")
    fun createListing(@Body data: CreateListingRequest): Single<ListingBaseResponse<ListingJson>>

    @GET("me/listing_detail")
    fun getMyListing(@Query("id") listingId: String) : Single<BaseResponse<ListingJson>>

    @GET("me/listing/listings")
    fun getMyListing(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = Constants.PER_PAGE_MAX,
        @Query("publisher_role") publisherRole: String = "all",
        @Query("listing_type") listingType: String = "all",
        @Query("property_type") propertyType: String = "all",
        @Query("state") state: String
    ): Single<BaseResponse<GetMyListingsResponse>>

    @PUT("me/validation_data")
    fun updateListingById(@Body listingRequest: CreateListingRequest): Single<ListingBaseResponse<ListingJson>>

    @GET("public/listing/listings")
    fun getListings(
        @Query("area[latitude]") lat: Float?,
        @Query("area[longitude]") lon: Float?,
        @Query("query_string") queryText: String?,
        @Query("google_places_location_id") placeId: String?,
        @Query("page") page: Int,
        @Query("per_page") limit: Int = Constants.PER_PAGE,
        @Query("area[radius]") radius: Int?,
        @Query("sort") sort: String = SortType.NEAREST.value,
        @Query("direction") order: String = SortType.NEAREST.order,
        @QueryMap filters: Map<String, String> = mutableMapOf(),
        @Query("facility_ids[]") facilities: List<Int>? = null,
        @Query("location[department]") department: String?= null,
        @Query("location[province]") province: String?= null,
        @Query("location[district]") district: String?= null,
        @Query("location[address]") address: String?= null
    ): Single<BaseResponse<GetMyListingsResponse>>

    @GET("public/listing/listings")
    fun getArListings(
        @Query("area[latitude]") lat: Float,
        @Query("area[longitude]") lon: Float,
        @Query("area[radius]") radius: Int = Constants.MAX_RADIUS,
        @QueryMap filters: Map<String, String> = mutableMapOf(),
    ): Single<BaseResponse<GetMyListingsResponse>>

    @GET("me/recent_searches")
    fun getRecentSearches(): Single<BaseResponse<GetRecentSearchesResponse>>

    @GET("public/search_locations")
    fun getDataLocationSearched(
        @Query("address") address: String?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
    ): Single<BaseResponse<DataLocationJson>>

    @POST("me/user_actions")
    fun setListingAction(@Body userActionRequest: UserActionRequest): Completable

    @POST("me/user_actions")
    fun setContactAction(@Body userActionRequest: UserActionRequest): Completable

//    @DELETE("me/user_actions")
    @HTTP(method = "DELETE", path = "me/user_actions", hasBody = true)
    fun deleteListingAction(@Body userActionRequest: UserActionRequest): Completable

    @GET("public/listing_amenities/{dataType}")
    fun getFacilitiesByType(
        @Path("dataType") dataType: String,
        @Query("data[property_type]") type: String?): Single<BaseResponse<GetFacilitiesResponse>>

    @GET("me/favourite_listings")
    fun getFavouriteListings(@Query("per_page") perPage: Int = Constants.PER_PAGE_MAX): Single<BaseResponse<GetMyListingsResponse>>

    @GET("public/last_viewed_listings")
    fun getTopListing(
        @Query("area_latitude") lat: Float,
        @Query("area_longitude") lon: Float,
        @Query("area_radius") radius: Int = Constants.MAX_RADIUS
    ) : Single<BaseResponse<GetMyListingsResponse>>

    @GET("public/ubigeos")
    fun getListUbigeo(
        @Query("type") type: String,
        @Query("department") department: String? = null,
        @Query("province") province: String? = null): Single<BaseResponse<GetListUbigeoResponse>>


    @GET("public/listing_detail")
    fun getListing(@Query("id") listingId: String) : Single<BaseResponse<ListingJson>>

    @GET("public/search_metadata")
    fun getListingsMetadata(
        @Query("area[latitude]") lat: Float?,
        @Query("area[longitude]") lon: Float?,
        @Query("area[radius]") radius: Int?,
        @QueryMap filters: Map<String, String> = mutableMapOf(),
        @Query("facility_ids[]") facilities: List<Int>? = null,
        @Query("location[department]") department: String?= null,
        @Query("location[province]") province: String?= null,
        @Query("location[district]") district: String?= null,
        @Query("location[address]") address: String?= null
    ): Observable<BaseResponse<ListingsMetadataJson>>

    @Multipart
    @POST("me/images")
    fun uploadListingImage(
        @Part image: MultipartBody.Part,
        @Part source: MultipartBody.Part,
        @Part type: MultipartBody.Part
    ): Single<BaseResponse<IdsBaseResponse<List<ImageJson>>>>
}