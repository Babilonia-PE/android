package com.babilonia.data.mapper

import com.babilonia.data.network.model.DoPaymentResponse
import com.babilonia.domain.model.payment.DoPayment
import javax.inject.Inject

class DoPaymentMapper @Inject constructor() {
    fun mapRemoteToDomain(response: DoPaymentResponse): DoPayment {
        with (response) {
            return DoPayment(
                state = state,
                description = description
            )
        }
    }
}