package com.babilonia.data.mapper

import com.babilonia.data.network.model.CreatePaymentIntentResponse
import com.babilonia.domain.model.payment.PaymentIntent
import javax.inject.Inject

class PaymentIntentMapper @Inject constructor() {
    fun mapRemoteToDomain(response: CreatePaymentIntentResponse): PaymentIntent {
        with (response) {
            return PaymentIntent(
                state = state,
                orderId = order,
                deviceSessionId = deviceSessionId,
                paymentIntentId = paymentIntentId
            )
        }
    }
}