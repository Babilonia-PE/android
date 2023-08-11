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

// Created by Anton Yatsenko on 07.06.2019.
interface ListingsService {

    @GET("api/listings/prices_histogram_slots")
    fun getPriceRange(
        @Query("area[latitude]") lat: Float,
        @Query("area[longitude]") lon: Float,
        @Query("area[radius]") radius: Int = Constants.MAX_RADIUS,
        @QueryMap filters: Map<String, String> = mutableMapOf(),
        @Query("facility_ids[]") facilities: List<Int>? = null
    ): Single<BaseResponse<PriceRangeResponse>>
}