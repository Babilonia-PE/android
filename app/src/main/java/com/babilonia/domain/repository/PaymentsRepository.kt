package com.babilonia.domain.repository

import com.babilonia.domain.model.payment.CreateOrder
import com.babilonia.domain.model.payment.DoPayment
import com.babilonia.domain.model.payment.PaymentIntent
import com.babilonia.domain.model.payment.PaymentPlan
import io.reactivex.Single

interface PaymentsRepository {

    fun createPaymentIntent(request: String, listingId: Long, productKey: String?, publisherRole: String, clientId: Long): Single<PaymentIntent>

    fun createNewOrder(request: String, paymentMethod: String, paymentId: String?, productKey: String?, listingId: Long, publisherRole: String, clientId: Long): Single<CreateOrder>

    fun doPayment(deviceSessionId: String?, paymentType: String, cardNumber: String, orderId: Long?, documentType: String, cardCvv: String, cardExpiration: String, cardName: String): Single<DoPayment>

    fun getPaymentPlans(): Single<List<PaymentPlan>>
}