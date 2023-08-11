package com.babilonia.data.datasource

import com.babilonia.data.network.model.CreateOrderResponse
import com.babilonia.data.network.model.CreatePaymentIntentResponse
import com.babilonia.data.network.model.DoPaymentResponse
import com.babilonia.data.network.model.json.AdPlanJson
import io.reactivex.Single

interface PaymentsDataSourceRemote {

    fun createPaymentIntent(request: String,
                            listingId: Long,
                            productKey: String?,
                            publisherRole: String,
                            clientId: Long):
            Single<CreatePaymentIntentResponse>

    fun createNewOrder(request: String,
                       paymentMethod: String,
                       paymentId: String?,
                       productKey: String?,
                       listingId: Long,
                       publisherRole: String,
                       clientId: Long): Single<CreateOrderResponse>

    fun doPayment(deviceSessionId: String?,
                  paymentType: String,
                  cardNumber: String,
                  orderId: Long?,
                  documentType: String,
                  cardCvv: String,
                  cardExpiration: String,
                  cardName: String): Single<DoPaymentResponse>

    fun getPaymentPlans(): Single<List<AdPlanJson>>
}