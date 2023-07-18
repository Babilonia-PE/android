package com.babilonia.data.storage.payment

import com.babilonia.data.datasource.PaymentsDataSourceRemote
import com.babilonia.data.network.error.mapErrors
import com.babilonia.data.network.error.mapNetworkErrors
import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.CreatePaymentIntentResponse
import com.babilonia.data.network.model.GetAdPlansResponse
import com.babilonia.data.network.model.GetPublishStatusResponse
import com.babilonia.data.network.service.PaymentService
import io.reactivex.Single
import javax.inject.Inject

class PaymentsDataSourceRemoteImpl @Inject constructor(
    private val paymentService: PaymentService
) : PaymentsDataSourceRemote {

    override fun getPaymentPlans(): Single<BaseResponse<GetAdPlansResponse>> {
        return paymentService.getAdPlans()
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun createPaymentIntent(
        listingId: Long,
        productKey: String,
        publisherRole: String
    ): Single<BaseResponse<CreatePaymentIntentResponse>> {
        return paymentService.createPaymentIntent(listingId, productKey, publisherRole)
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun getPublishStatus(listingId: Long): Single<BaseResponse<GetPublishStatusResponse>> {
        return paymentService.getPublishStatus(listingId)
            .mapNetworkErrors()
            .mapErrors()
    }
}