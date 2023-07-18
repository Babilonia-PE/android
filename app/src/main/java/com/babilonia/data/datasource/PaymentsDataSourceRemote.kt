package com.babilonia.data.datasource

import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.CreatePaymentIntentResponse
import com.babilonia.data.network.model.GetAdPlansResponse
import com.babilonia.data.network.model.GetPublishStatusResponse
import io.reactivex.Single

interface PaymentsDataSourceRemote {

    fun getPaymentPlans(): Single<BaseResponse<GetAdPlansResponse>>

    fun createPaymentIntent(listingId: Long, productKey: String, publisherRole: String):
            Single<BaseResponse<CreatePaymentIntentResponse>>

    fun getPublishStatus(listingId: Long): Single<BaseResponse<GetPublishStatusResponse>>
}