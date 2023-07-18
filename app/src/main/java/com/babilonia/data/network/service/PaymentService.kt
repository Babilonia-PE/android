package com.babilonia.data.network.service

import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.CreatePaymentIntentResponse
import com.babilonia.data.network.model.GetAdPlansResponse
import com.babilonia.data.network.model.GetPublishStatusResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentService {

    @GET("api/ad_plans")
    fun getAdPlans(): Single<BaseResponse<GetAdPlansResponse>>

    @POST("/api/users/me/listings/{listing_id}/stripe_payment_intents")
    fun createPaymentIntent(
        @Path("listing_id") listingId: Long,
        @Query("data[product_key]") productKey: String,
        @Query("data[publisher_role]") publisherRole: String
    ): Single<BaseResponse<CreatePaymentIntentResponse>>

    @GET("/api/users/me/listings/{listing_id}/publishing_status")
    fun getPublishStatus(
        @Path("listing_id") listingId: Long
    ): Single<BaseResponse<GetPublishStatusResponse>>
}