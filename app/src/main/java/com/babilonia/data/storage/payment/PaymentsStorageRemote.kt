package com.babilonia.data.storage.payment

import com.babilonia.data.datasource.PaymentsDataSourceRemote
import com.babilonia.data.network.model.CreateOrderResponse
import com.babilonia.data.network.model.CreatePaymentIntentResponse
import com.babilonia.data.network.model.DoPaymentResponse
import com.babilonia.data.network.model.json.AdPlanJson
import com.babilonia.data.network.model.json.OrderRequest
import com.babilonia.data.network.model.json.PaymentIntentRequest
import com.babilonia.data.network.model.json.DoPaymentRequest
import com.babilonia.data.network.service.PaymentService
import io.reactivex.Single
import javax.inject.Inject

class PaymentsStorageRemote @Inject constructor(
    private val paymentService: PaymentService
) : PaymentsDataSourceRemote {

    override fun createPaymentIntent(request: String, listingId: Long, productKey: String?, publisherRole: String, clientId: Long
    ): Single<CreatePaymentIntentResponse> {
        return paymentService.createPaymentIntent(PaymentIntentRequest(request, listingId, productKey))
            .map { it.data }
    }

    override fun createNewOrder(
        request: String,
        paymentMethod: String,
        paymentId: String?,
        productKey: String?,
        listingId: Long,
        publisherRole: String,
        clientId: Long
    ): Single<CreateOrderResponse> {
        return paymentService.createNewOrder(OrderRequest(request, paymentMethod, paymentId, productKey, listingId, publisherRole, clientId))
            .map { it.data }
    }

    override fun doPayment(
        deviceSessionId: String?,
        paymentType: String,
        cardNumber: String,
        orderId: Long?,
        documentType: String,
        cardCvv: String,
        cardExpiration: String,
        cardName: String
    ): Single<DoPaymentResponse> {
        return paymentService.doPayment(DoPaymentRequest(deviceSessionId, paymentType, cardNumber, orderId, documentType, cardCvv, cardExpiration, cardName))
            .map { it.data }
    }

    override fun getPaymentPlans(): Single<List<AdPlanJson>> {
        return paymentService.getAdPlans().map { it.data.records }
    }
}