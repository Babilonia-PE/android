package com.babilonia.data.network.service

import com.babilonia.data.network.model.*
import com.babilonia.data.network.model.json.OrderRequest
import com.babilonia.data.network.model.json.PaymentIntentRequest
import com.babilonia.data.network.model.json.DoPaymentRequest
import io.reactivex.Single
import retrofit2.http.*

interface PaymentService {

    @POST("me/payment_intent")
    fun createPaymentIntent(@Body paymentIntentRequest: PaymentIntentRequest): Single<BaseResponse<CreatePaymentIntentResponse>>

    @POST("me/order")
    fun createNewOrder(@Body orderRequest: OrderRequest): Single<BaseResponse<CreateOrderResponse>>

    @POST("me/payment_process")
    fun doPayment(@Body doPaymentRequest: DoPaymentRequest): Single<BaseResponse<DoPaymentResponse>>

    @GET("public/ad_plans")
    fun getAdPlans(): Single<BaseResponse<GetAdPlansResponse>>
}