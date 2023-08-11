package com.babilonia.data.mapper

import com.babilonia.data.network.model.CreateOrderResponse
import com.babilonia.domain.model.payment.CreateOrder
import javax.inject.Inject

class CreateOrderMapper @Inject constructor() {
    fun mapRemoteToDomain(response: CreateOrderResponse): CreateOrder {
        with (response) {
            return CreateOrder(
                state = state,
                description = description
            )
        }
    }
}